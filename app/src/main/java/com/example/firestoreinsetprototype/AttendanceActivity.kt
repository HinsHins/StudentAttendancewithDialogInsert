package com.example.firestoreinsetprototype

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreinsetprototype.Adaptor.AttendanceRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Adaptor.StudentRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath
import com.example.firestoreinsetprototype.Document.AttendanceDocMapper
import com.example.firestoreinsetprototype.Document.TimetableDocMapper
import com.example.firestoreinsetprototype.Extension.timeFormat
import com.example.firestoreinsetprototype.Model.*
import com.example.firestoreinsetprototype.Util.SpinnerUtil
import com.example.firestoreinsetprototype.Util.realTimeUpdate
import com.example.firestoreinsetprototype.Util.retrieveData
import com.example.firestoreinsetprototype.Util.retrieveDataWithMatch
import com.example.firestoreinsetprototype.ViewModel.AttendanceViewModel
import com.example.firestoreinsetprototype.ViewModel.TimetableViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_attendance.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_student.*
import java.util.*
import kotlin.collections.ArrayList

class AttendanceActivity : AppCompatActivity() {
    private val studentsPath = FirestoreCollectionPath.STUDENTS_PATH
    private val attendancesPath = FirestoreCollectionPath.ATTENDANCES_PATH
    private val timetablePath = FirestoreCollectionPath.TIMETABLES_PATH
//    private val attendances = ArrayList<Attendance>()
    private val auth = FirebaseAuth.getInstance()
    private val fb = FirebaseFirestore.getInstance()
    var currentTimetableId : String = ""
    var currentTimetable : Timetable? = null
    lateinit var attendanceAdapter : AttendanceRecyclerViewAdaptor
    private val students = ArrayList<Student>()
    private val attendanceDocMapper = AttendanceDocMapper(ArrayList<Attendance>(),ArrayList<AttendanceViewModel>(), ArrayList<Attendance>())
    private var selectedFilterProperty = ""
    private var isAscendingChecked : Boolean? = null

    var sortingDialog : AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)
        currentTimetableId = intent.getStringExtra("timetableId").toString()
        Log.d("currentTimetableId", "currentTimetableId: ${currentTimetableId}")

        val linearLayoutManager = LinearLayoutManager(this)
        attendance_recyclerView.layoutManager = linearLayoutManager
        attendance_recyclerView.adapter = AttendanceRecyclerViewAdaptor(attendanceDocMapper.attendanceViewModels)
        attendanceAdapter = (attendance_recyclerView.adapter as AttendanceRecyclerViewAdaptor)
        attendanceAdapter.onItemClickListener = object : AttendanceRecyclerViewAdaptor.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                Log.d("onClick", "$position")
            }

            override fun onLongClick(v: View, position: Int) : Boolean {
                Log.d("OnItemLongClickListener", "long pressed at $position")
                return true
            }
        }
        attendanceAdapter.onCheckboxClickListener = object :  AttendanceRecyclerViewAdaptor.OnCheckBoxClickListener {
            override fun onClick(v: View, position: Int) {
                Log.d("onClick", "$position")
                if (v !is CheckBox) {  return }
                val checked: Boolean = v.isChecked
                updateAttendance(checked,attendanceDocMapper.attendances[position])
            }

        }
        var textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("onTextChanged", "onTextChanged: $s")
                if(s.toString().trim() == "") {
                    resetAttendances()
                    return
                }
                resetAttendances()
                filterStudentAttendacne(s.toString())
            }
        }
        search_bar_et.addTextChangedListener(textWatcher)
        btn_sort.setOnClickListener {
            showSortingDialog()
        }
        retrieveTimetable()
        retrieveStudents()
        initFilterSpinner()
    }

    private fun initFilterSpinner() {
        val filterSpinner: Spinner = findViewById(R.id.filterSpinner)
        val filterAdapter =  SpinnerUtil.setupSpinner(this,filterSpinner,Attendance.propertyNames){
            selectedFilterProperty = Attendance.propertyNames[it]
        }
        filterAdapter.notifyDataSetChanged()
    }

    private fun retrieveTimetable(){
        val timetablePathCollection = fb.collection(timetablePath)
        timetablePathCollection.document(currentTimetableId).get().addOnCompleteListener {
            if(it.isSuccessful) {
                currentTimetable = it.result?.toObject(Timetable::class.java)
            }
        }
    }

    private fun retrieveAttendance() {
        val attendanceCollection = fb.collection(attendancesPath)
        attendanceCollection.retrieveDataWithMatch("timetableId",currentTimetableId,attendanceDocMapper.attendances as ArrayList<Model>,
            Attendance::class.java) {
            Log.d("attendances", "retrieveAttendance: ${attendanceDocMapper.attendances.map { it.studentId }}")
            attendanceDocMapper.oirgnalAttendances = attendanceDocMapper.attendances
            attendanceDocMapper.updateViewModel(students)
            attendanceAdapter.notifyDataSetChanged()
        }
    }

    private fun retrieveStudents() {
        val studentCollection = fb.collection(studentsPath)
        studentCollection.retrieveData(students as ArrayList<Model>, Student::class.java){
            retrieveAttendance()
        }
    }

    fun updateAttendance(isPresent : Boolean,attendance: Attendance){
        val attendanceRef = fb.collection(attendancesPath).document(attendance.id)
        var currentTime : Timestamp? = if(isPresent) Timestamp(Date()) else null
//        if(isPresent){
           attendanceRef.update("time",currentTime).addOnSuccessListener {
               val index = attendanceDocMapper.attendances.indexOf(attendance)
               attendanceDocMapper.attendances[index].time = currentTime
               attendanceDocMapper.updateViewModel(students)
               attendanceAdapter.notifyDataSetChanged()
           }
        attendanceRef.update("status", isPresent)
        updateTimetable(currentTimetableId,attendance.studentId,isPresent)
//        }
    }

    fun resetAttendances(){
        updateAttendances(attendanceDocMapper.oirgnalAttendances)
    }

    fun updateAttendances(attendances: ArrayList<Attendance>) {
        attendanceDocMapper.attendances = attendances
        attendanceDocMapper.updateViewModel(this.students)
        attendanceAdapter.notifyDataSetChanged()
    }

    fun filterStudentAttendacne(targetString : String) {
        var students = ArrayList<Student>()
        var attendances = ArrayList<Attendance>()
        when(selectedFilterProperty){
            "id" -> students = this.students.filter { it.id.contains(targetString,ignoreCase = true)} as ArrayList<Student>
            "name" -> students = this.students.filter { it.name.contains(targetString,ignoreCase = true)} as ArrayList<Student>
            "present" -> attendances = attendanceDocMapper.attendances.filter { if(targetString.equals("No",true) )  it.time == null else it.time?.toDate()?.timeFormat()?.contains(targetString) ?: false } as ArrayList<Attendance>
        }
        if(students.isNotEmpty()) {
            var studentIds = students.map { it.id } as ArrayList<String>
            attendances = attendanceDocMapper.attendances.filter { findAttendance(studentIds,it) } as ArrayList<Attendance>
        }
//        var studentIds = students.map { it.id } as ArrayList<String>
//        var attendances = attendanceDocMapper.attendances.filter { findAttendance(studentIds,it) } as ArrayList<Attendance>
//        attendanceDocMapper.attendances = attendances
        Log.d("attendances", "filterStudentAttendacne: ${attendances.map { it.studentId }}")
        updateAttendances(attendances)

        Log.d("filterStudentAttendacne", "filterStudentAttendacne: ${attendances.map { it.studentId }}")
//        attendanceDocMapper.findStudent(students,)≠=+
    }

    private fun findAttendance(studentIds : ArrayList<String>, attendance: Attendance) : Boolean {
        return studentIds.filter { it == attendance.studentId }.isNotEmpty()
    }

    private fun updateTimetable(timetableId: String, studentId : String, isPresent: Boolean) {
        if(currentTimetable == null) { return }
        val timetablePathCollection = fb.collection(timetablePath)
        var presentedStudents = currentTimetable!!.presentedStudents
        if(presentedStudents.contains(studentId)) {
            if(!isPresent) { presentedStudents.remove(studentId) }
        }else {
            if(isPresent) presentedStudents.add(studentId) else presentedStudents.remove(studentId)
        }
        timetablePathCollection.document(timetableId).update("presentedStudents",presentedStudents)
    }

    fun showSortingDialog() {
        val builder = AlertDialog.Builder(this)
        val sortingView = layoutInflater.inflate(R.layout.sorting_dialog,null)
        val rb_ascending = sortingView.findViewById<RadioButton>(R.id.rb_ascending)
        val rb_descending = sortingView.findViewById<RadioButton>(R.id.rb_descending)
        if(isAscendingChecked != null) {
            if(isAscendingChecked as Boolean) rb_ascending.isChecked = true else rb_descending.isChecked = true
        }
        var onSortingChanged = object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(v !is RadioButton) { return }
                    // Is the button now checked?
                val checked = v.isChecked
                    // Check which radio button was clicked
                when (v.getId()) {
                    R.id.rb_ascending ->
                        if (checked) {
                                // Pirates are the best
                            attendanceSortBy(true)
                            isAscendingChecked = true
                        }
                    R.id.rb_descending ->
                        if (checked) {
                                // Ninjas rule
                            attendanceSortBy(false)
                            isAscendingChecked = false
                        }
                }
            }
        }
        rb_ascending.setOnClickListener(onSortingChanged)
        rb_descending.setOnClickListener(onSortingChanged)
        builder?.setView(sortingView)
        sortingDialog = builder.create()
        sortingDialog?.show()
//        Handler(Looper.getMainLooper()).postDelayed({
//            dialog.dismiss()
//        },5000)
    }

    fun attendanceSortBy(isAscending : Boolean) {
        when(selectedFilterProperty){
            "id" -> if(isAscending) attendanceDocMapper.attendances.sortBy { it.studentId } else attendanceDocMapper.attendances.sortByDescending { it.studentId }
            "name" -> if(isAscending) attendanceDocMapper.attendances.sortBy { attendanceDocMapper.findStudent(students,it.studentId)?.name } else attendanceDocMapper.attendances.sortByDescending { attendanceDocMapper.findStudent(students,it.studentId)?.name }
            "present" -> if(isAscending) attendanceDocMapper.attendances.sortBy { it.time } else attendanceDocMapper.attendances.sortByDescending { it.time }
        }
        Log.d("attendanceSortBy", "attendanceSortBy: ${attendanceDocMapper.attendances.map { it.studentId }}")
        updateAttendances(attendanceDocMapper.attendances)
        attendanceAdapter.notifyDataSetChanged()
    }


}

