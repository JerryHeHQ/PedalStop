package com.example.pedalstop

import android.app.ActionBar.LayoutParams
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.pedalstop.data.MainViewModel
import com.example.pedalstop.databinding.FragmentAddBinding
import com.google.android.material.snackbar.Snackbar


class AddFragment : Fragment() {
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private var selectedImageURI: Uri? = null
    private var isOldLocation: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("BRUH", viewModel.getCurrentAuthUser().name)

        val shapes = resources.getStringArray(R.array.shapes)
        val shapesAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, shapes)
        binding.shapeAutoCompleteTextView.setAdapter(shapesAdapter)

        val mountings = resources.getStringArray(R.array.mountings)
        val mountingsAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, mountings)
        binding.mountingAutoCompleteTextView.setAdapter(mountingsAdapter)

        viewModel.observeUserLocation().observe(viewLifecycleOwner) {
            // Requests the user's current location again and inputs it into the LatLng fields
            if (isOldLocation) {
                isOldLocation = false
            } else {
                binding.latitudeTextInputLayout.editText?.setText(it.latitude.toString())
                binding.longitudeTextInputLayout.editText?.setText(it.longitude.toString())
            }
        }
        (requireActivity() as MainActivity).requestSingleLocationUpdate()

        binding.cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if (data != null) {
                    selectedImageURI = data.data
                    binding.takePictureImageView.setImageURI(selectedImageURI)
                    binding.takePictureImageView.layoutParams.width = LayoutParams.MATCH_PARENT
                    binding.takePictureImageView.layoutParams.height = LayoutParams.WRAP_CONTENT
                }
            }
        }

        binding.takePictureImageView.setOnClickListener {
            val intent = Intent()
            intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT)
            resultLauncher.launch(intent)
        }

        binding.submitButton.setOnClickListener {
            var hasError = false

            val latitude = binding.latitudeTextInputEditText.text.toString()
            val longitude = binding.longitudeTextInputEditText.text.toString()
            val shape = binding.shapeAutoCompleteTextView.text.toString()
            val mounting = binding.mountingAutoCompleteTextView.text.toString()
            val description = binding.descriptionTextInputEditText.text.toString()

            if (latitude.isEmpty()) {
                hasError = true
                binding.latitudeTextInputLayout.isErrorEnabled = true
                binding.latitudeTextInputLayout.error = "Latitude cannot be empty"
            } else if (latitude.toDouble() > 180 || latitude.toDouble() < -180) {
                hasError = true
                binding.latitudeTextInputLayout.isErrorEnabled = true
                binding.latitudeTextInputLayout.error = "Latitude is not valid"
            } else {
                binding.latitudeTextInputLayout.isErrorEnabled = false
                binding.latitudeTextInputLayout.error = null
            }

            if (longitude.isEmpty()) {
                hasError = true
                binding.longitudeTextInputLayout.isErrorEnabled = true
                binding.longitudeTextInputLayout.error = "Longitude cannot be empty"
            } else if (latitude.toDouble() > 180 || latitude.toDouble() < -180) {
                hasError = true
                binding.longitudeTextInputLayout.isErrorEnabled = true
                binding.longitudeTextInputLayout.error = "Longitude is not valid"
            } else {
                binding.longitudeTextInputLayout.isErrorEnabled = false
                binding.longitudeTextInputLayout.error = null
            }

            if (shape.isEmpty()) {
                hasError = true
                binding.shapeTextInputLayout.isErrorEnabled = true
                binding.shapeTextInputLayout.error = "A shape must be selected"
            } else {
                binding.shapeTextInputLayout.isErrorEnabled = false
                binding.shapeTextInputLayout.error = null
            }

            if (mounting.isEmpty()) {
                hasError = true
                binding.mountingTextInputLayout.isErrorEnabled = true
                binding.mountingTextInputLayout.error = "A mounting type must be selected"
            } else {
                binding.mountingTextInputLayout.isErrorEnabled = false
                binding.mountingTextInputLayout.error = null
            }

            if (description.isEmpty()) {
                hasError = true
                binding.descriptionTextInputLayout.isErrorEnabled = true
                binding.descriptionTextInputLayout.error = "A description must be given"
            } else {
                binding.descriptionTextInputLayout.isErrorEnabled = false
                binding.descriptionTextInputLayout.error = null
            }

            if (selectedImageURI == null) {
                hasError = true
                Toast.makeText(activity, "Please provide an image", Toast.LENGTH_LONG).show()
            }

            if (!hasError) {
                viewModel.isLoading.value = true

                viewModel.addPost(
                    selectedImageURI!!,
                    latitude.toDouble(),
                    longitude.toDouble(),
                    shape,
                    mounting,
                    description,
                ) {
                    viewModel.isLoading.value = false
                    val toastMessage = if (it) {
                        "Post successfully added"
                    } else {
                        "Error adding post"
                    }
                    Toast.makeText(activity, toastMessage, Toast.LENGTH_LONG).show()
                    if (it) {
                        viewModel.refetchAllPosts()
                        parentFragmentManager.popBackStack()
                    }
                }
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}