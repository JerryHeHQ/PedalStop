package com.example.pedalstop.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreHelper {

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun uploadPostMetadata (postData: PostData, resultListener: (Boolean) -> Unit) {
        database.collection("posts").add(postData)
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