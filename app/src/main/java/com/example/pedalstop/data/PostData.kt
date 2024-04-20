package com.example.pedalstop.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

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
    var ratingSum : Double = -1.0,
    var reviewIDs : List<String> = listOf<String>(),
    @ServerTimestamp val timeStamp: Timestamp? = null,
    @DocumentId var firestoreID: String = ""
)