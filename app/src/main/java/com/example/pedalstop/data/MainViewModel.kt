package com.example.pedalstop.data

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.pedalstop.User
import com.example.pedalstop.invalidUser
import java.util.UUID

class MainViewModel : ViewModel() {
    private val storageHelper = StorageHelper()
    private val firestoreHelper = FirestoreHelper()
    private var currentAuthUser = invalidUser

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

            val postMetadata = PostMetadata(
                currentAuthUser.name,
                currentAuthUser.uid,
                imageUUID,
                latitude,
                longitude,
                shape,
                mounting,
                description
            )

            firestoreHelper.uploadPostMetadata(postMetadata) {
                resultListener(it)
            }

        }

    }

}