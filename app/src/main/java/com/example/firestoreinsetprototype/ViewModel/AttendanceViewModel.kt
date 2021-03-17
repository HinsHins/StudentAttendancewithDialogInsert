package com.example.firestoreinsetprototype.ViewModel

import java.util.*

data class AttendanceViewModel(val studentId : String,
                               val studentName : String,
                               val time : Date? = null
)