package com.example.pedalstop.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreHelper {

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun uploadPostData(postData: PostData, resultListener: (Boolean) -> Unit) {
        database.collection("posts").add(postData)
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "uploadPostData SUCCEEDED")
                resultListener(true)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "uploadPostData FAILED")
                resultListener(false)
            }
    }

    fun getAllPosts(resultListener: (List<PostData>) -> Unit) {
        database.collection("posts").get()
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "getAllPosts SUCCEEDED")
                val response: MutableList<PostData> = mutableListOf()
                for (postData in it) {
                    response.add(postData.toObject(PostData::class.java))
                }
                resultListener(response)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "getAllPosts FAILED")
                resultListener(mutableListOf())
            }
    }

//    fun getAllCurrentUserPosts(userUid: String, resultListener: (List<PostData>) -> Unit) {
//        database.collection("posts").whereEqualTo("ownerUid", userUid).get()
//            .addOnSuccessListener {
//                Log.d(javaClass.simpleName, "getAllCurrentUserPosts SUCCEEDED")
//                val response: MutableList<PostData> = mutableListOf()
//                for (postData in it) {
//                    response.add(postData.toObject(PostData::class.java))
//                }
//                resultListener(response)
//            }
//            .addOnFailureListener {
//                Log.d(javaClass.simpleName, "getAllCurrentUserPosts FAILED")
//                resultListener(mutableListOf())
//            }
//    }

}