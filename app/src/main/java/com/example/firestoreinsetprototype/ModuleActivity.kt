package com.example.firestoreinsetprototype

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreinsetprototype.Adaptor.ModuleRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath
import com.example.firestoreinsetprototype.Extension.dateFormat
import com.example.firestoreinsetprototype.Extension.hideKeyboard
import com.example.firestoreinsetprototype.Extension.timeFormat
import com.example.firestoreinsetprototype.Model.*
import com.example.firestoreinsetprototype.Util.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.synthetic.main.activity_module.*
import java.util.*
import kotlin.collections.ArrayList


class ModuleActivity : AppCompatActivity() {

    private val modulesPath = FirestoreCollectionPath.MODULES_PATH
    private val lecturersPath = FirestoreCollectionPath.LECTURERS_PATH
    private val programmesPath = FirestoreCollectionPath.PROGRAMMES_PATH
    private val timetablesPath = FirestoreCollectionPath.TIMETABLES_PATH
    private val attendancesPath = FirestoreCollectionPath.ATTENDANCES_PATH

    private var modules = ArrayList<Module>()
    private val lecturers = ArrayList<Lecturer>()
    private val programmes = ArrayList<Programme>()
//    private val lecturersString =ArrayList<String>()
    private var selectedLecturer: Lecturer? = null
//    private val programmesString =ArrayList<String>()
    private var selectedProgramme: Programme? = null

    @ServerTimestamp
    var fdate = Date()
    var ftime = Date()
    var fyear:Int = 0
    var fmonth:Int = 0
    var fday:Int = 0
    var fhour:Int = 0
    var fminute:Int = 0


    var fb = FirebaseFirestore.getInstance()
    lateinit var moduleAdapter: ModuleRecyclerViewAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module)

        val linearLayoutManager = LinearLayoutManager(this)
        module_recyclerView.layoutManager = linearLayoutManager
        module_recyclerView.adapter = ModuleRecyclerViewAdaptor(modules)
        moduleAdapter = (module_recyclerView.adapter as ModuleRecyclerViewAdaptor)
        moduleAdapter.onItemClickListener =
            object : ModuleRecyclerViewAdaptor.OnItemClickListener {
                override fun onClick(v: View, position: Int) {
                    Log.d("onClick", "$position")
                }

                override fun onLongClick(v: View, position: Int): Boolean {
                    Log.d("OnItemLongClickListener", "long pressed at $position")
                    presentDeleteAlert(modules[position])
                    return true
                }
            }

        module_insert.setOnClickListener {
            var id = module_id_et.text.toString().trim()
            var name = module_name_et.text.toString().trim()
            var numOfWeek = number_of_week_et.text.toString().toInt()
            fdate = Date(DateUtil.toYearFrom(datePickerYear = fyear),fmonth,fday)
            ftime= Date(0,0,0,fhour,fminute)
          //  var year = module_year_et.text.toString().trim()
           // var level = module_level_et.text.toString().trim()
           // var credit = module_credit_et.text.toString().trim()
            var lecturer = selectedLecturer
            var programme = selectedProgramme

            //if (id != "" && name != "" && year != "" && level != "" && credit != "" && lecturer != null) {
            if (id != "" && name != "" && lecturer != null && programme != null) {
                //var module = Module(id.toInt(),name, year.toInt(), level.toInt(), credit.toInt(),lecturer)
                var module =
                    Module(
                        id, name, Timestamp(fdate), lecturer.id.toString(), lecturer.name,
                        programme.id.toString(), programme.name, numOfWeek, Timestamp(ftime)
                    )


                Log.d("Module", "$module")
                hideKeyboard()
                clearInput()
                writeModule(module)
            } else
                Toast.makeText(this, "Please fill all fields before insert", Toast.LENGTH_SHORT).show()
        }

        retrieveModules()
        retrieveLecturers()
        retrieveProgrammes()

        class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                // Use the current date as the default date in the picker
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)


                // Create a new instance of DatePickerDialog and return it
                return DatePickerDialog(this@ModuleActivity, this, year, month, day)
            }

            override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
                // Do something with the date chosen by the user
                fyear = year
                fmonth = month
                fday = day
                Log.d("Year", "$fyear")
                Log.d("Month", "$fmonth")
                Log.d("Day", "$fday")
                var selectedDate = findViewById<TextView>(R.id.selectedDate_tv)
                val date = Date(DateUtil.toYearFrom(datePickerYear = fyear),fmonth,fday)
//                selectedDate.text = fday.toString() + "-" + (fmonth+1).toString() + "-" + fyear.toString()
                selectedDate.text = date.dateFormat()
            }
        }

        class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                // Use the current time as the default values for the picker
                val c = Calendar.getInstance()
                val hour = c.get(Calendar.HOUR_OF_DAY)
                val minute = c.get(Calendar.MINUTE)

                // Create a new instance of TimePickerDialog and return it
                return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
            }

            override fun onTimeSet(view: TimePicker, hour: Int, minute: Int) {
                // Do something with the time chosen by the user
                fhour = hour
                fminute = minute
                Log.d("Hour", "$fhour")
                Log.d("Minute", "$fminute")
                val timeDate = Date(0,0,0,fhour,fminute)
                var selectedTime = findViewById<TextView>(R.id.selectedTime_tv)

                selectedTime.text = timeDate.timeFormat()
            }
        }

        startDate_button.setOnClickListener {
            DatePickerFragment()
                .show(supportFragmentManager, "datePicker")
        }

        startTime_button.setOnClickListener{
            TimePickerFragment()
                .show(supportFragmentManager, "timePicker")
        }

    }


    private fun retrieveModules() {
        val moduleCollection = fb.collection(modulesPath)
        moduleCollection.retrieveData(modules as ArrayList<Model>, Module::class.java) {
            moduleAdapter.notifyDataSetChanged()
        }
//        val moduleCollection = fb.collection(modulesPath)
//        moduleCollection.retrieveData(moduleDocuments.modulesRef) {
//            moduleDocuments.updateModules()
//            moduleAdapter.notifyDataSetChanged()
//        }
    }


    private fun retrieveLecturers() {
        val lecturersCollection = fb.collection(lecturersPath)
        lecturersCollection.retrieveData(lecturers as ArrayList<Model>, Lecturer::class.java) {
            var lecturers = lecturers as ArrayList<Lecturer>
            val lecturersString = lecturers.map { it.name } as ArrayList<String>
            val lectureSpinner: Spinner = findViewById(R.id.lecturerSpinner)
            val lecturerAdapter =  SpinnerUtil.setupSpinner(this,lectureSpinner,lecturersString){
                selectedLecturer = lecturers[it]
            }
            lecturerAdapter.notifyDataSetChanged()
        }
    }

    private fun retrieveProgrammes() {
        val programmesCollection = fb.collection(programmesPath)
        programmesCollection.retrieveData(programmes as ArrayList<Model>, Programme::class.java) {
            var programmes = programmes as ArrayList<Programme>
            val programmesString = programmes.map { it.name } as ArrayList<String>
            val programmeSpinner: Spinner = findViewById(R.id.programmeSpinner)
            val programmeAdapter = SpinnerUtil.setupSpinner(this,programmeSpinner,programmesString) {
                selectedProgramme = programmes[it]
            }
            programmeAdapter.notifyDataSetChanged()
        }
    }

    private fun writeModule(module: Module) {
        val moduleCollection = fb.collection(modulesPath)
        moduleCollection.realTimeUpdate(modules as ArrayList<Model>,Module::class.java) {
            moduleAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT).show()
        }
//        moduleCollection.realTimeUpdate(moduleDocuments.modulesRef){
//            moduleDocuments.updateModules()
//            moduleAdapter.notifyDataSetChanged()
//        }
//        moduleCollection.realTimeUpdate(moduleDocuments.modules as ArrayList<Model>,Module::class.java) {
//            moduleDocuments.modules = moduleDocuments.modulesRef.map { it.toObject(Module::class.java) } as ArrayList<Module>
//            moduleAdapter.notifyDataSetChanged()
//            Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT).show()
//        }
        moduleCollection.document(module.id)
            .set(module)
            .addOnSuccessListener {
                generateTimetable(module)
                Log.d("", "Module successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("", "Error writing document", e)
            }
    }

    override fun onStop() {
        super.onStop()
        clearInput()
        moduleAdapter.clear()
    }

    private fun clearInput() {
        module_id_et.text.clear()
        module_name_et.text.clear()
        //module_year_et.text.clear()
        //module_level_et.text.clear()
        //module_credit_et.text.clear()
    }

    private fun presentDeleteAlert(module: Module) {
        val dialog =  AlertDialog.Builder(this)
        dialog.setCancelable(true).setMessage("Are you sure to delete module ID : ${module.id}").setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
            deleteModule(module)
        }).setNegativeButton("No", DialogInterface.OnClickListener{ dialog, _ ->
            dialog.cancel()
        }).show()
    }

    private fun deleteModule(module: Module) {
        val moduleCollection = fb.collection(modulesPath)
        moduleCollection.realTimeUpdate(modules as ArrayList<Model>,Module::class.java) {
            moduleAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT).show()
        }
//        moduleCollection.realTimeUpdate(moduleDocuments.modulesRef){
//            moduleDocuments.updateModules()
//            moduleAdapter.notifyDataSetChanged()
//        }
        moduleCollection.document(module.id.toString()).delete()
            .addOnSuccessListener {
                Log.d("", "Module successfully deleted! ")
                deleteOtherRelatedData(module)
            }
            .addOnFailureListener {e ->
                Log.w("", "Error deleting document",e )
            }
    }

    private fun deleteOtherRelatedData(module: Module) {
        val timetableCollection = fb.collection(timetablesPath)
        val attendanceCollection = fb.collection(attendancesPath)
        var timetables = ArrayList<Timetable>()
        var attendances = ArrayList<Attendance>()
        timetableCollection.retrieveDataWithMatch("moduleId",module.id,timetables as ArrayList<Model>, Timetable::class.java) {
            timetables.forEach { timetable ->
                attendanceCollection.retrieveDataWithMatch("timetableId",timetable.id,attendances as ArrayList<Model> , Attendance::class.java) {
                    attendances.forEach { attendance ->
                        //delete attendance
                        attendanceCollection.document(attendance.id).delete()
                    }
                }
                //delete module
                timetableCollection.document(timetable.id).delete()
            }
        }
    }

    private fun generateTimetable(module: Module) {
        val timetableCollection = fb.collection(timetablesPath)

        var timetables = ArrayList<Timetable>()
        var cumulativeDate = module.startDate?.toDate() ?: Date()
        for (week in 1 until (module.numOfWeek + 1)) {
            val timestampDate = Timestamp(cumulativeDate)
            timetables.add(
                Timetable(
                    UUID.randomUUID().toString(),
                    module.id,
                    module.lecturerId,
                    timestampDate,
                    module.startTime,
                    ArrayList(),
                    week
                )
            )
            cumulativeDate = DateUtil.addDays(cumulativeDate,7)
        }

        val batch = fb.batch()
        timetables.forEach {
            val document = timetableCollection.document(it.id)
            batch.set(document,it)
        }
        batch.commit().addOnCompleteListener {
            Log.d("Timetables batch set", "Timetables successfully written!")
        }
    }


}