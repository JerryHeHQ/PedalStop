package com.example.pedalstop.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

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

    data class FavoritesList(
        var list: List<String> = listOf<String>(),
    )

    private fun createFavorites(userUid: String, resultListener: (Boolean) -> Unit) {
        database.collection("favorites").document(userUid).set(FavoritesList())
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "createFavorites SUCCEEDED")
                resultListener(true)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "createFavorites FAILED")
                resultListener(false)
            }
    }

    fun getFavorites(userUid: String, resultListener: (List<String>, Boolean) -> Unit) {
        database.collection("favorites").document(userUid).get()
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "getFavorites SUCCEEDED")
                val favoritesList = it.toObject(FavoritesList::class.java)
                if (favoritesList == null) {
                    createFavorites(userUid) { it1 ->
                        resultListener(listOf<String>(), it1)
                    }
                } else {
                    resultListener(favoritesList.list, true)
                }
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "getFavorites FAILED")
                resultListener(listOf<String>(), false)
            }
    }

    private fun updateFavorites(userUid: String, list: List<String>,
                                resultListener: (Boolean) -> Unit) {
        database.collection("favorites").document(userUid).set(FavoritesList(list))
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "updateFavorites SUCCEEDED")
                resultListener(true)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "updateFavorites FAILED")
                resultListener(false)
            }
    }

    fun togglePostFavorite(userUid: String, postID: String, resultListener: (List<String>, Boolean) -> Unit) {
        getFavorites(userUid) { list, success ->
            if (success) {
                val listCopy = list.toMutableList()
                if (listCopy.contains(postID)) {
                    listCopy.remove(postID)
                } else {
                    listCopy.add(postID)
                }
                updateFavorites(userUid, listCopy) {
                    Log.d(javaClass.simpleName, "togglePostFavorite ${if (it) {"SUCCEEDED"} else {"FAILED"}}")
                    resultListener(listCopy, it)
                }
            } else {
                Log.d(javaClass.simpleName, "togglePostFavorite FAILED")
                resultListener(listOf(), false)
            }
        }
    }

    fun addReview(reviewerName: String, reviewerUid: String, postID: String, rating: Double,
                  description: String, resultListener: (ReviewData, Boolean) -> Unit) {

        val reviewData = ReviewData(reviewerName, reviewerUid, rating, description, )

        database.runTransaction {
            it.update(database.collection("posts").document(postID),"reviews", FieldValue.arrayUnion(reviewData))
            it.update(database.collection("posts").document(postID), "ratingSum", FieldValue.increment(rating))
            true
        }
            .addOnSuccessListener {
            Log.d(javaClass.simpleName, "addReview SUCCEEDED")
            resultListener(reviewData, true)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "addReview FAILED")
                resultListener(reviewData, false)
            }
    }

}