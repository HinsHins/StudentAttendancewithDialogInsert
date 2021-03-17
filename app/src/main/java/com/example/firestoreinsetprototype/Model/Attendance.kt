package com.example.firestoreinsetprototype.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.util.ArrayList

class Attendance (
    override var id:String="",
    var studentId:String="",
    var status:Boolean=false,
    var time: Timestamp? = null,
    var timetableId: String = "",
    var lesson_type:String=""
): Model {
    companion object {
        val propertyNames = arrayListOf<String>("id","name","present")
    }
}

