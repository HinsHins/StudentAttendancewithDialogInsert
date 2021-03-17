package com.example.firestoreinsetprototype.Extension

import java.text.SimpleDateFormat
import java.util.*

fun Date.toDateOnly() : Date{
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar[Calendar.HOUR_OF_DAY] = 0
    calendar[Calendar.MINUTE] = 0
    calendar[Calendar.SECOND] = 0
    calendar[Calendar.MILLISECOND] = 0
    return calendar.time
}

fun Date.toTimeOnly() : Date{
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar[Calendar.YEAR] = 0
    calendar[Calendar.MONTH] = 0
    calendar[Calendar.DAY_OF_MONTH] = 0
    return calendar.time
}
fun Date.dateFormat() : String {
    return SimpleDateFormat("dd/M/yyyy").format(this)
}

fun Date.timeFormat() : String {
    return SimpleDateFormat("HH:mm").format(this)
}

fun Date.dayFormat() : String {
    return SimpleDateFormat("EEEE").format(this)
}