package com.example.firestoreinsetprototype.Document

import com.example.firestoreinsetprototype.Model.Attendance
import com.example.firestoreinsetprototype.Model.Module
import com.example.firestoreinsetprototype.Model.Student
import com.example.firestoreinsetprototype.Model.Timetable
import com.example.firestoreinsetprototype.ViewModel.AttendanceViewModel
import com.example.firestoreinsetprototype.ViewModel.TimetableViewModel
import java.util.*
import kotlin.collections.ArrayList

class AttendanceDocMapper(var attendances : ArrayList<Attendance>, val attendanceViewModels: ArrayList<AttendanceViewModel>,var oirgnalAttendances: ArrayList<Attendance>){



    fun updateViewModel(students : ArrayList<Student>) {
        attendanceViewModels.clear()
        val viewModels = attendanceToViewModel(students)
        viewModels.forEach{
            this.attendanceViewModels.add(it)
        }
    }

    private fun attendanceToViewModel(students : ArrayList<Student>) : ArrayList<AttendanceViewModel>{
        var attendanceViewModels = ArrayList<AttendanceViewModel>()
        for (attendance in attendances) {
            val student = findStudent(students,attendance.studentId)
            val time = attendance.time?.toDate()
            if(student != null) {
                val viewModel = AttendanceViewModel(student.id,student.name,time)
                attendanceViewModels.add(viewModel)
            }
        }
        return attendanceViewModels
    }

    fun findStudent(students: ArrayList<Student>, studentId: String) : Student?{
        for (student in students) {
            if(student.id == studentId){
                return student
            }
        }
        return null
    }

}