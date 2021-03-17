package com.example.firestoreinsetprototype.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

class Timetable(
    override var id: String = "",
    var moduleId: String= "",
    var lecturerId: String = "",
    var date: Timestamp?=null,
    var time: Timestamp?=null,
    var presentedStudents: ArrayList<String> = ArrayList(),
    var week : Int =0
): Model