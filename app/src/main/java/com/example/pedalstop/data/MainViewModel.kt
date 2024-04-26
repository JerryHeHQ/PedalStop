package com.example.pedalstop.data

import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pedalstop.AuthUser
import com.example.pedalstop.MainActivity
import com.example.pedalstop.R
import com.example.pedalstop.User
import com.example.pedalstop.invalidUser
import kotlinx.coroutines.launch
import java.util.UUID

data class LatLng(val latitude: Double?, val longitude: Double?)

class MainViewModel : ViewModel() {
    val isLoading = MutableLiveData(true)
    private var currentAuthUser = MutableLiveData<User>().apply {
        value = invalidUser
    }
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
            }
        }
    }

    private var favoritesList = MediatorLiveData<List<String>>().apply {
        addSource(currentAuthUser) {
            if (currentAuthUser.value != null && currentAuthUser.value != invalidUser) {
                firestoreHelper.getFavorites(currentAuthUser.value!!.uid) { list, success ->
                    value = if (success) { list } else { listOf() }
                }
            }
        }
    }

    var visiblePosts = MediatorLiveData<List<PostData>>().apply {
        addSource(allPosts) { value = sortAndFilterPosts(it) }
        addSource(searchLocation) { value = allPosts.value?.let { it1 -> sortAndFilterPosts(it1) } }
        addSource(userLocation) { value = allPosts.value?.let { it1 -> sortAndFilterPosts(it1) } }
        addSource(shapesTag) { value = allPosts.value?.let { it1 -> sortAndFilterPosts(it1) } }
        addSource(mountingsTag) { value = allPosts.value?.let { it1 -> sortAndFilterPosts(it1) } }
        addSource(favoritesList) { value = value?.toList() }
    }

    var currentPost = MutableLiveData<PostData>().apply {
        value = null
    }

    fun refetchAllPosts() {
        viewModelScope.launch {
            firestoreHelper.getAllPosts() {
                allPosts.value = it
            }
        }
    }

    private fun sortAndFilterPosts(posts: List<PostData>): List<PostData> {
        val copiedPosts = posts.toMutableList()
        copiedPosts.removeAll {
            var removePost = false
            if (!shapesTag.value.equals("Shapes")) {
                if (it.shape != shapesTag.value) {
                    removePost = true
                }
            }
            if (!mountingsTag.value.equals("Mountings")) {
                if (it.mounting != mountingsTag.value) {
                    removePost = true
                }
            }
            removePost
        }
        copiedPosts.sortBy {
            getDistance(it.latitude, it.longitude)
        }
        isLoading.value = false
        return copiedPosts.toList()
    }

    fun isFavorited(postID: String): Boolean {
        return favoritesList.value?.contains(postID) ?: false
    }

    fun togglePostFavorite(post: PostData, resultListener: (Boolean) -> Unit) {
        isLoading.value = true
        firestoreHelper.togglePostFavorite(getCurrentAuthUser().uid, post.firestoreID) { list, success ->
            if (success) {
                favoritesList.value = list
            }
            isLoading.value = false
            resultListener(success)
        }
    }

    fun setCurrentAuthUser(user: User) {
        currentAuthUser.value = user
    }

    fun getCurrentAuthUser(): User {
        return currentAuthUser.value ?: invalidUser
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
                getCurrentAuthUser().name,
                getCurrentAuthUser().uid,
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
    }

    fun observeUserLocation(): MutableLiveData<LatLng> {
        return userLocation
    }

    fun setSearchLocation(location: LatLng) {
        searchLocation.value = location
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

    fun addReview(rating: Double, description: String, resultListener: (Boolean) -> Unit) {
        firestoreHelper.addReview(getCurrentAuthUser().name, getCurrentAuthUser().uid,
            currentPost.value!!.firestoreID, rating, description) { reviewData, success ->
            if (success) {
                val copyAllPosts = allPosts.value!!.toMutableList()
                val currentPostIndex = copyAllPosts.indexOfFirst { postData ->
                    postData.firestoreID == currentPost.value!!.firestoreID
                }
                copyAllPosts[currentPostIndex].ratingSum += rating
                val newReviewsList = copyAllPosts[currentPostIndex].reviews.toMutableList()
                newReviewsList.add(reviewData)
                copyAllPosts[currentPostIndex].reviews = newReviewsList.toList()
                allPosts.value = copyAllPosts
                currentPost.value = copyAllPosts[currentPostIndex]
            }
            resultListener(success)
        }
    }

    fun glideFetch(uuid: String, imageView: ImageView, width: Int, height: Int) {
        Glide.fetch(
            storageHelper.uuid2StorageReference(uuid),
            imageView,
            width,
            height)
    }
}