package com.example.firestoreinsetprototype

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreinsetprototype.Adaptor.StudentRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath
import com.example.firestoreinsetprototype.Extension.hideKeyboard
import com.example.firestoreinsetprototype.Model.*
import com.example.firestoreinsetprototype.Util.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.android.synthetic.main.activity_student.*
import kotlinx.android.synthetic.main.student_dialog_layout.*
import java.util.*
import kotlin.collections.ArrayList

class StudentActivity : AppCompatActivity() {

    //MARK:- Firestore Path Constants
    private val studentsPath = FirestoreCollectionPath.STUDENTS_PATH
    private val modulesPath = FirestoreCollectionPath.MODULES_PATH
    private val programmesPath = FirestoreCollectionPath.PROGRAMMES_PATH
    private val attendancesPath = FirestoreCollectionPath.ATTENDANCES_PATH
    private val timetablePath = FirestoreCollectionPath.TIMETABLES_PATH

    private val students = ArrayList<Student>()
    private val programmes = ArrayList<Programme>()
    private val modules = ArrayList<Module>()

//    private val timetablesRef = ArrayList<QueryDocumentSnapshot>()
    private val timetables = ArrayList<Timetable>()

//    private val programmesString =ArrayList<String>()
    private var selectedProgramme: Programme? = null

//    private val attendances = ArrayList<Attendance>()
    private val fb = FirebaseFirestore.getInstance()
    lateinit var studentAdapter : StudentRecyclerViewAdaptor

    //implement after insertion work
    //lateinit var attendanceAdapter: AttendanceRecyclerViewAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        val linearLayoutManager = LinearLayoutManager(this)
        student_recyclerView.layoutManager = linearLayoutManager
        student_recyclerView.adapter = StudentRecyclerViewAdaptor(students)
        studentAdapter = (student_recyclerView.adapter as StudentRecyclerViewAdaptor)
        studentAdapter.onItemClickListener = object : StudentRecyclerViewAdaptor.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                Log.d("onClick", "$position")
            }

            override fun onLongClick(v: View, position: Int) : Boolean {
                Log.d("OnItemLongClickListener", "long pressed at $position")
                presentDeleteAlert(students[position])
                return true
            }
        }
//        student_list_view.setOnItemLongClickListener {  parent, view, position, id ->
//            Log.d("OnItemLongClickListener", "long pressed at $position and id $id and the view $view")
//            presentDeleteAlert(students[position])
//            return@setOnItemLongClickListener(true)
//        }

        student_insert.setOnClickListener {
            studentInputDialog(it)
        }
//        student_insert.setOnClickListener {
//            var id = studentId_et.text.toString().trim()
//            var name = studentName_et.text.toString().trim()
////            var email = semail_et.text.toString().trim()
//            var programme = selectedProgramme
////            var country = scountry_et.text.toString().trim()
//
//            if (id != "" && name != "" && programme != null ) {
//                var student =
//                    Student(
//                        id,
//                        name,
//                        programme.id,
//                        programme.name
//                    )
//                Log.d("Student", "$student")
//                hideKeyboard()
//                clearInputs()
//                writeStudent(student)
//            } else
//                Toast.makeText(
//                    this@StudentActivity,
//                    "Please fill all fields before insert",
//                    Toast.LENGTH_SHORT
//                ).show()
//        }
        retrieveStudents()
        retrieveModules()
        retrieveTimetables()
    }

    override fun onStop() {
        super.onStop()
        clearInputs()
        studentAdapter.clear()
    }

    private fun clearInputs(){
//        studentId_et.text.clear()
//        studentName_et.text.clear()
//        semail_et.text.clear()
//        sprogramme_et.text.clear()
//        scountry_et.text.clear()
    }


    //MARK: - Data retrieve
    private fun retrieveProgrammes(spinner: Spinner){
        val programmeCollection = fb.collection(programmesPath)
        programmeCollection.retrieveData(programmes as ArrayList<Model>, Programme::class.java) {
            var programmes = programmes as ArrayList<Programme>
            val programmesString = programmes.map { it.name } as ArrayList<String>
//            val programmeSpinner: Spinner = findViewById(R.id.sprogramme_spinner)
            val programmeAdapter =  SpinnerUtil.setupSpinner(
                this,
                spinner,
                programmesString
            ){
                selectedProgramme = programmes[it]
            }
            programmeAdapter.notifyDataSetChanged()
        }
    }


    private fun retrieveModules() {
        val moduleCollection = fb.collection(modulesPath)
        moduleCollection.retrieveData(modules as ArrayList<Model>, Module::class.java) {

        }
    }

    private fun retrieveStudents() {
        val studentCollection = fb.collection(studentsPath)
        studentCollection.retrieveData(students as ArrayList<Model>, Student::class.java){
            studentAdapter.notifyDataSetChanged()
        }
    }


    private fun retrieveTimetables() {
        val timetablePathCollection = fb.collection(timetablePath)
        timetablePathCollection.retrieveData(timetables as ArrayList<Model>,Timetable::class.java)
    }

    private fun writeStudent(student: Student) {
        val studentCollection = fb.collection(studentsPath)
        studentCollection.realTimeUpdate(students as ArrayList<Model>, Student::class.java){
            studentAdapter.notifyDataSetChanged()
            Toast.makeText(this@StudentActivity, "Insert successful", Toast.LENGTH_SHORT)
                .show()
        }
        studentCollection.document(student.id.toString())
            .set(student)
            .addOnSuccessListener {
                generateAttendance(student)
                Log.d("", "Student successfully written!")
            }
            .addOnFailureListener { e->
                Log.w("", "Error writing document", e)
            }
    }


    private fun presentDeleteAlert(student: Student){
        val dialog =  AlertDialog.Builder(this)
        dialog.setCancelable(true).setMessage("Are you sure to delete Student ID : ${student.id}").setPositiveButton(
            "Yes",
            DialogInterface.OnClickListener { _, _ ->
                deleteStudent(student)
            }).setNegativeButton("No", DialogInterface.OnClickListener { dialog, _ ->
            dialog.cancel()
        }).show()
    }

    private fun deleteStudent(student: Student) {
        val studentCollection = fb.collection(studentsPath)
        studentCollection.realTimeUpdate(students as ArrayList<Model>, Student::class.java){
            studentAdapter.notifyDataSetChanged()
            Toast.makeText(this@StudentActivity, "Delete successful", Toast.LENGTH_SHORT)
                .show()
        }
        studentCollection.document(student.id).delete().addOnSuccessListener {
            Log.d("", "Student successfully deleted!")
            //delete attendance as well
            deleteOtherRelatedData(student)
        }
            .addOnFailureListener { e->
                Log.w("", "Error deleting document", e)
            }
    }

    private fun deleteOtherRelatedData(student : Student){
        val timetableCollection = fb.collection(timetablePath)
        val attendanceCollection = fb.collection(attendancesPath)
        attendanceCollection.whereEqualTo("studentId",student.id).get().addOnSuccessListener { querySnapshot ->
            querySnapshot.documents.forEach {
                it.reference.delete()
            }
        }

        var timetables = ArrayList<Timetable>()
        timetableCollection.retrieveDataWithContains("presentedStudents",student.id,timetables as ArrayList<Model>,Timetable::class.java) {
            timetables.forEach {
                var timetable = it as Timetable
                timetable.presentedStudents.remove(student.id)
                timetableCollection.document(timetable.id).update("presentedStudents",timetable.presentedStudents)
            }
        }
    }

    private fun generateAttendance(student: Student) {
        val attendanceCollection = fb.collection(attendancesPath)
        var attendances = ArrayList<Attendance>()

//        //1.find the programme object of the student
        val programme = findProgramme(student.programmeId)

        //2.get all related modules
        val modules = getAllModules(programme)

        //3.find the num of week on each of module and iterate them by the num of week
        timetables.forEach {
//            for (module in modules) {
//                if(it.moduleId == module.id) {
            if(findMatchedModule(modules,it)) {
                val timetableId = it.id
                val attendance = Attendance(UUID.randomUUID().toString(), student.id, false, null, timetableId, "lecture")
                attendances.add(attendance)
            }

//                }
//            }
//            var cumulativeDate = it.startDate?.toDate() ?: Date()
//            for (week in 1 until (it.numOfWeek + 1)) {
//                //construct Attendance
////                if (week == 1) {
////                        cumulativeDate = it.startDate?.toDate() ?: Date()
////                }
////               val date = (if (week == 1) it.startDate?.toDate() else DateUtil.addDays(it.startDate ?: Timestamp(Date()),7) ) ?: Date()
//                val timestampDate = Timestamp(cumulativeDate)
////                attendances.add(Attendance(UUID.randomUUID().toString(),student.id, false,timestampDate,null,it.lecturerId,it.id,it.name,"lecture",week))
//                cumulativeDate = DateUtil.addDays(cumulativeDate,7)
//            }
        }

        val batch = fb.batch()
        attendances.forEach {
            val document = attendanceCollection.document(it.id)
            batch.set(document,it)
        }
        batch.commit().addOnCompleteListener {
            Log.d("Attendance batch set", "Attendances successfully written!")
        }
    }


    private fun findProgramme(id: String) : Programme {
        return programmes.first { it.id == id }
    }
    private fun getAllModules(programme: Programme) : ArrayList<Module> {
       return modules.filter { it.programmeId == programme.id } as ArrayList<Module>
    }

    private fun findMatchedModule(modules : ArrayList<Module>, timetable: Timetable) : Boolean {
        modules.forEach {
            if(it.id == timetable.moduleId){
                return true
            }
        }
        return false
    }

    fun studentInputDialog(view: View) {
        val builder = android.app.AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("Create New Student")
        val dialogLayout = inflater.inflate(R.layout.student_dialog_layout, null)
        val studentId_et  = dialogLayout.findViewById<EditText>(R.id.studentId_et)
        val studentName_et  = dialogLayout.findViewById<EditText>(R.id.studentName_et)
        val studentProgrammeSpinner = dialogLayout.findViewById<Spinner>(R.id.sprogramme_spinner)
        retrieveProgrammes(studentProgrammeSpinner)

        builder.setView(dialogLayout)
        builder.setPositiveButton("Cancel") {dialog, whichButton ->
            dialog.dismiss()
            programmes.clear()
            studentProgrammeSpinner.adapter = null
        }

        builder.setNegativeButton("Insert"){ dialogInterface, i ->
            var id = studentId_et.text.toString().trim()
            var name = studentName_et.text.toString().trim()
//            var email = semail_et.text.toString().trim()
            var programme = selectedProgramme
//            var country = scountry_et.text.toString().trim()

            if (id != "" && name != "" && programme != null ) {
                var student =
                    Student(
                        id,
                        name,
                        programme.id,
                        programme.name
                    )
                Log.d("Student", "$student")
                hideKeyboard()
                clearInputs()
                writeStudent(student)
            } else
                Toast.makeText(
                    this@StudentActivity,
                    "Please fill all fields before insert",
                    Toast.LENGTH_SHORT
                ).show()
//            Toast.makeText(this, "Student ID:  " + studentId_et.text.toString() + "Student Name: " + studentName_et.text.toString(), Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }

}