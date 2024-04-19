package com.example.pedalstop.data

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference

class StorageHelper {

    private val photoStorage: StorageReference =
        FirebaseStorage.getInstance().reference.child("images")

    fun uploadPostImage(image: Uri, uuid: String, resultListener:(Boolean)->Unit) {
        val uuidRef = photoStorage.child(uuid)
        val metadata = StorageMetadata.Builder().setContentType("image/jpg").build()
        val uploadTask = uuidRef.putFile(image, metadata)

        uploadTask
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "Upload SUCCEEDED $uuid")
                resultListener(true)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "Upload FAILED $uuid")
                resultListener(false)
            }
    }

    fun deleteImage(pictureUUID: String) {
        photoStorage.child(pictureUUID).delete()
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "Delete SUCCEEDED $pictureUUID")
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "Delete FAILED $pictureUUID")
            }
    }
}