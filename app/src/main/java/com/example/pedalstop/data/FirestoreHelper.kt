package com.example.pedalstop.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreHelper {

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun uploadPostData(postData: PostData, resultListener: (Boolean) -> Unit) {
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

    fun getAllPosts(resultListener: (List<PostData>) -> Unit) {
        database.collection("posts").get()
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "get SUCCEEDED")
                val response: MutableList<PostData> = mutableListOf()
                for (postData in it) {
                    response.add(postData.toObject(PostData::class.java))
                }
                resultListener(response)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "get FAILED")
                resultListener(mutableListOf())
            }
    }
}