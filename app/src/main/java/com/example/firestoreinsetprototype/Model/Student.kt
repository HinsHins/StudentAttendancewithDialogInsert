package com.example.firestoreinsetprototype.Model

import com.example.firestoreinsetprototype.Model.ModelName

class Student(
    override var id:String="",
    override var name:String="",
    //var email:String="",
    var programmeId:String="",
    var programmeName : String = ""
    //var country:String=""
) : ModelName