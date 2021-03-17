package com.example.firestoreinsetprototype.Model

class Lecturer (
    override var id:String="",
    override var name:String="",
    var email:String="",
    var position:String="",
    var department:String="",
    var hasPhoto : Boolean = false
) : ModelName