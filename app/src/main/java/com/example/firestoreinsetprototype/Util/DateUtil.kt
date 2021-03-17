package com.example.firestoreinsetprototype.Util

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {

    const val TIMESTAMP_YEAR_DIFFERENCE = 1900
    const val TIMESTAMP_MONTH_DIFFERENCE = 1

    fun toYearFrom(datePickerYear : Int) : Int {
        return datePickerYear - TIMESTAMP_YEAR_DIFFERENCE
    }
    fun toYearWith(timestampYear : Int) : Int {
        return timestampYear + TIMESTAMP_YEAR_DIFFERENCE
    }

    fun toMonthFrom(datePickerMonth : Int) : Int {
        return datePickerMonth - TIMESTAMP_MONTH_DIFFERENCE
    }

    fun toMonthWith(timestampMonth : Int) : Int {
        return timestampMonth + TIMESTAMP_MONTH_DIFFERENCE
    }

    fun addDays(timestamp : com.google.firebase.Timestamp, days : Int) : Date {
        val date = timestamp.toDate()
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DATE, days)
        return calendar.time
    }

    fun addDays(date : Date, days : Int) : Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DATE, days)
        return calendar.time
    }

}