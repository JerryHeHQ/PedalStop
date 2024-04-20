package com.example.pedalstop.data

import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pedalstop.User
import com.example.pedalstop.invalidUser
import kotlinx.coroutines.launch
import java.util.UUID

//data class LatLng(val latitude: Double?, val longitude: Double?)

class MainViewModel : ViewModel() {
    private var currentAuthUser = invalidUser
    private val storageHelper = StorageHelper()
    private val firestoreHelper = FirestoreHelper()
//    private val userLocation = MutableLiveData<LatLng>().apply {
//        value = LatLng(null, null)
//    }

    private var allPosts = MutableLiveData<List<PostData>>().apply {
        viewModelScope.launch {
            firestoreHelper.getAllPosts() {
                value = it
                Log.d("BRUH", it.toString())
            }
        }
    }

//    var searchPosts = MediatorLiveData<List<PostData>>().apply {
//
//    }

    fun setCurrentAuthUser(user: User) {
        currentAuthUser = user
    }

    fun getCurrentAuthUser(): User {
        return currentAuthUser
    }

    fun observeAllPosts(): MutableLiveData<List<PostData>> {
        return allPosts
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

//    fun setUserLocation(location: LatLng) {
//        userLocation.postValue(location)
//    }
//
//    private fun getDistance(postLatitude: Double, postLongitude: Double): Double {
//        val results = floatArrayOf()
//        Location.distanceBetween(
//            userLocation.value!!.latitude!!,
//            userLocation.value!!.longitude!!,
//            postLatitude,
//            postLongitude,
//            results)
//        return results[0].toDouble()
//    }

    fun glideFetch(uuid: String, imageView: ImageView, width: Int, height: Int) {
        Glide.fetch(
            storageHelper.uuid2StorageReference(uuid),
            imageView,
            width,
            height)
    }
}