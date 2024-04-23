package com.example.pedalstop

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pedalstop.data.MainViewModel
import com.example.pedalstop.databinding.ActivityMainBinding
import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.example.pedalstop.data.LatLng
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration : AppBarConfiguration
    private lateinit var authUser : AuthUser
    private val viewModel : MainViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var geocoder: Geocoder

    private fun initMenu() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menuLogout -> {
                        authUser.logout()
                        true
                    }
                    else -> false
                }
            }
        })
    }

    fun requestSingleLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation.let { location ->
                        val latitude = location.latitude
                        val longitude = location.longitude
                        Log.d("BRUH", "requestSingleLocationUpdate")
                        Log.d("BRUH", latitude.toString())
                        Log.d("BRUH", longitude.toString())
                        viewModel.setUserLocation(LatLng(latitude, longitude))
                    }
                    // Remove location updates after receiving the first location
                    fusedLocationClient.removeLocationUpdates(this)
                }
            },
            Looper.getMainLooper()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestSingleLocationUpdate()
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private fun processAddresses(addresses: List<Address>) {
        if (addresses.isNotEmpty()) {
            val latitude = addresses[0].latitude
            val longitude = addresses[0].longitude
            viewModel.setSearchLocation(LatLng(latitude, longitude))
            Log.d("BRUH", "processingAddresses")
            Log.d("BRUH", latitude.toString())
            Log.d("BRUH", longitude.toString())
        }
    }

    private fun getGeocodingAddress(locationName: String) {
        Log.d("BRUH", locationName)
        if (Build.VERSION.SDK_INT >= 33) {
            geocoder.getFromLocationName(locationName, 1) {
                MainScope().launch(Dispatchers.Main) {
                    processAddresses(it)
                }
            }
        } else {
            MainScope().launch(Dispatchers.Main) {
                geocoder.getFromLocationName(locationName, 1)?.let { it1 ->
                    processAddresses(it1)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initMenu()

        viewModel.isLoading.observe(this) {
            binding.progressBar.visibility = if (it) { View.VISIBLE } else { View.GONE }
        }

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_search,
                R.id.navigation_saved,
                R.id.navigation_posts,
                R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.addPostImageButton.setOnClickListener {
            Log.d("BRUH", viewModel.getCurrentAuthUser().name)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.coverFrameLayout, AddFragment())
                .addToBackStack(null)
                .commit()
        }

        viewModel.currentPost.observe(this) {
            if (it != null) {
                Log.d("BRUH", it.toString())
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.coverFrameLayout, OnePostFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                Log.d("BRUH", "null")
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // Update location every 10 seconds
        }
        requestSingleLocationUpdate()

        val shapes = resources.getStringArray(R.array.shapesWithAll)
        val shapesAdapter = ArrayAdapter(this, R.layout.dropdown_item, shapes)
        binding.shapeTagAutoCompleteTextView.setAdapter(shapesAdapter)
        binding.shapeTagAutoCompleteTextView.setText(shapes[0], false)
        binding.shapeTagAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            viewModel.setShapesTag(shapes[position])
        }

        val mountings = resources.getStringArray(R.array.mountingsWithAll)
        val mountingsAdapter = ArrayAdapter(this, R.layout.dropdown_item, mountings)
        binding.mountingTagAutoCompleteTextView.setAdapter(mountingsAdapter)
        binding.mountingTagAutoCompleteTextView.setText(mountings[0], false)
        binding.mountingTagAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            viewModel.setMountingsTag(mountings[position])
        }

        geocoder = Geocoder(this, Locale.US)
        binding.locationTextInputLayout.setEndIconOnClickListener {
            val locationName = binding.locationTextInputEditText.text.toString()
            if (locationName.isBlank()) {
                viewModel.setSearchLocation(LatLng(null, null))
            } else {
                getGeocodingAddress(locationName)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        authUser = AuthUser(activityResultRegistry)
        lifecycle.addObserver(authUser)
        authUser.observeUser().observe(this) {
            viewModel.setCurrentAuthUser(it)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}