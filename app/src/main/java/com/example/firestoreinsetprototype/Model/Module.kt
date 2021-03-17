package com.example.firestoreinsetprototype.Model

import com.example.firestoreinsetprototype.Model.ModelName
import com.google.firebase.Timestamp

class Module(
    override var id:String="",
    override var name:String="",
   // var year:Int=0,
   // var level:Int=0,
    //var credit:Int=0,
    var startDate:Timestamp?=null,
    var lecturerId:String="",
    var lecturerName:String="",
    var programmeId:String="",
    var programmeName:String="",
    var numOfWeek : Int = 0,
    var startTime:Timestamp?=null
) : ModelName