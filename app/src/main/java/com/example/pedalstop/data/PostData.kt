package com.example.pedalstop.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PostData(
    var ownerName: String = "",
    var ownerUid: String = "",
    var imageUUID : String = "",
    var latitude : Double = 0.0,
    var longitude : Double = 0.0,
    var shape : String = "",
    var mounting : String = "",
    var description : String = "",
    var favoritedBy : List<String> = listOf<String>(),
    var ratingSum : Double = 0.0,
    var reviews : List<ReviewData> = listOf<ReviewData>(),
    @ServerTimestamp val timeStamp: Timestamp? = null,
    @DocumentId var firestoreID: String = ""
)

data class ReviewData(
    var reviewerName: String = "",
    var reviewerUid: String = "",
    var rating : Double = 0.0,
    var description : String = "",
    val timeStamp: Date = Date()
)