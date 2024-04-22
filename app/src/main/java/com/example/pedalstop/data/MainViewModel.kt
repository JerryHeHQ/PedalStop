package com.example.pedalstop.data

import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pedalstop.R
import com.example.pedalstop.User
import com.example.pedalstop.invalidUser
import kotlinx.coroutines.launch
import java.util.UUID

data class LatLng(val latitude: Double?, val longitude: Double?)

class MainViewModel : ViewModel() {
    private var currentAuthUser = invalidUser
    private val storageHelper = StorageHelper()
    private val firestoreHelper = FirestoreHelper()
    private val userLocation = MutableLiveData<LatLng>().apply {
        value = LatLng(null, null)
    }
    private val searchLocation = MutableLiveData<LatLng>().apply {
        value = LatLng(null, null)
    }
    private val shapesTag = MutableLiveData<String>().apply {
        value = "Shapes"
    }
    private val mountingsTag = MutableLiveData<String>().apply {
        value = "Mountings"
    }

    private var allPosts = MutableLiveData<List<PostData>>().apply {
        viewModelScope.launch {
            firestoreHelper.getAllPosts() {
                value = it
                Log.d("BRUH", it.toString())
            }
        }
    }

    var visiblePosts = MediatorLiveData<List<PostData>>().apply {
        addSource(allPosts) { value = sortAndFilterPosts(it) }
        addSource(searchLocation) { value = allPosts.value?.let { it1 -> sortAndFilterPosts(it1) } }
        addSource(userLocation) { value = allPosts.value?.let { it1 -> sortAndFilterPosts(it1) } }
        addSource(shapesTag) { value = allPosts.value?.let { it1 -> sortAndFilterPosts(it1) } }
        addSource(mountingsTag) { value = allPosts.value?.let { it1 -> sortAndFilterPosts(it1) } }
    }

    fun refetchAllPosts() {
        viewModelScope.launch {
            firestoreHelper.getAllPosts() {
                allPosts.value = it
                Log.d("BRUH", it.toString())
            }
        }
    }

    private fun sortAndFilterPosts(posts: List<PostData>): List<PostData> {
        val copiedPosts = posts.toMutableList()
        copiedPosts.removeAll {
            var removePost = false
            if (!shapesTag.value.equals("Shapes")) {
                Log.d("BRUH", shapesTag.value.toString())
                Log.d("BRUH", it.shape)
                if (it.shape != shapesTag.value) {
                    removePost = true
                }
            }
            if (!mountingsTag.value.equals("Mountings")) {
                if (it.mounting != mountingsTag.value) {
                    removePost = true
                }
            }
            Log.d("BRUH", removePost.toString())
            removePost
        }
        copiedPosts.sortBy {
            getDistance(it.latitude, it.longitude)
        }
        Log.d("BRUH", copiedPosts.toList().toString())
        return copiedPosts.toList()
    }

    fun setCurrentAuthUser(user: User) {
        currentAuthUser = user
    }

    fun getCurrentAuthUser(): User {
        return currentAuthUser
    }

    fun addPost(imageUri: Uri,
                latitude: Double,
                longitude: Double,
                shape: String,
                mounting: String,
                description: String,
                resultListener: (Boolean) -> Unit) {

        val imageUUID = UUID.randomUUID().toString()
        storageHelper.uploadPostImage(imageUri, imageUUID) { postImageUploaded ->
            if (!postImageUploaded) {
                resultListener(false)
                return@uploadPostImage
            }

            val postData = PostData(
                currentAuthUser.name,
                currentAuthUser.uid,
                imageUUID,
                latitude,
                longitude,
                shape,
                mounting,
                description
            )

            firestoreHelper.uploadPostData(postData) {
                resultListener(it)
            }
        }
    }

    fun setUserLocation(location: LatLng) {
        userLocation.value = location
        Log.d("BRUH", userLocation.value.toString())
    }

    fun observeUserLocation(): MutableLiveData<LatLng> {
        return userLocation
    }

    fun setSearchLocation(location: LatLng) {
        searchLocation.value = location
        Log.d("BRUH", searchLocation.value.toString())
    }

    fun getDistance(postLatitude: Double, postLongitude: Double): Double {
        val location = if (locationIsValid(searchLocation.value)) {
            searchLocation.value
        } else if (locationIsValid(userLocation.value)) {
            userLocation.value
        } else {
            return 0.0
        }

        val results = FloatArray(1)
        Location.distanceBetween(
            location!!.latitude!!,
            location.longitude!!,
            postLatitude,
            postLongitude,
            results)
        return results[0].toDouble()
    }

    private fun locationIsValid(location: LatLng?): Boolean {
        return !(location?.latitude == null || location.longitude == null)
    }

    fun setShapesTag(shape: String) {
        shapesTag.value = shape
    }

    fun setMountingsTag(mounting: String) {
        mountingsTag.value = mounting
    }

    fun glideFetch(uuid: String, imageView: ImageView, width: Int, height: Int) {
        Glide.fetch(
            storageHelper.uuid2StorageReference(uuid),
            imageView,
            width,
            height)
    }
}