package com.example.pedalstop.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreHelper {

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun uploadPostMetadata (postMetadata: PostMetadata, resultListener: (Boolean) -> Unit) {
        database.collection("posts").add(postMetadata)
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "Upload SUCCEEDED")
                resultListener(true)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "Upload FAILED")
                resultListener(false)
            }
    }
}