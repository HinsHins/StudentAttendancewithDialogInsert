package com.example.firestoreinsetprototype

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreinsetprototype.Adaptor.TimetableRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath
import com.example.firestoreinsetprototype.Document.TimetableDocMapper
import com.example.firestoreinsetprototype.Extension.dateFormat
import com.example.firestoreinsetprototype.Extension.dayFormat
import com.example.firestoreinsetprototype.Extension.toDateOnly
import com.example.firestoreinsetprototype.Model.*
import com.example.firestoreinsetprototype.Util.DateUtil
import com.example.firestoreinsetprototype.Util.retrieveData
import com.example.firestoreinsetprototype.Util.retrieveDataWithMatch
import com.example.firestoreinsetprototype.ViewModel.TimetableViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ProfileActivity : AppCompatActivity() {

    private val timetablePath = FirestoreCollectionPath.TIMETABLES_PATH
    private val lecturersPath = FirestoreCollectionPath.LECTURERS_PATH
    private val modulesPath = FirestoreCollectionPath.MODULES_PATH


//    private val attendances = ArrayList<Attendance>()
    private val lecturers = ArrayList<Lecturer>()
    private val modules = ArrayList<Module>()
//    private var modulesIds = ArrayList<String>()


//    private var timetables = ArrayList<Timetable>()
    private val timetableDocMapper = TimetableDocMapper(ArrayList<Timetable>(),ArrayList<TimetableViewModel>())
    private val fb = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var currentUserEmail = ""
    private var currentLecturer = Lecturer()
    private var storage = FirebaseStorage.getInstance()

    lateinit var timetableAdapter: TimetableRecyclerViewAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        currentUserEmail = intent.getStringExtra("email").toString()

        val linearLayoutManager = LinearLayoutManager(this)
        timetable_recyclerView.layoutManager = linearLayoutManager
        timetable_recyclerView.adapter = TimetableRecyclerViewAdaptor(timetableDocMapper.timetableViewModels)
        timetableAdapter = (timetable_recyclerView.adapter as TimetableRecyclerViewAdaptor)
        timetableAdapter.onItemClickListener =
            object : TimetableRecyclerViewAdaptor.OnItemClickListener {
                override fun onClick(v: View, position: Int) {
                    Log.d("onClick", "$position")
                    toNextScreen(timetableDocMapper.timetables[position])
                }

                override fun onLongClick(v: View, position: Int): Boolean {
                    return true
                }
            }
        toRegisterPage.setOnClickListener {
            auth.signOut()
            finish()
        }

        analytic_button.setOnClickListener {
            var intent = Intent(this, MonitoringActivity::class.java)
            startActivity(intent)
        }

        today_tv.text = Date().dateFormat()
        today_week_tv.text = Date().dayFormat()
        retrieveModules()
    }


    private fun retrieveLecturers() {
        val lecturersCollection = fb.collection(lecturersPath)
        lecturersCollection.retrieveData(lecturers as ArrayList<Model>, Lecturer::class.java) {
            var lecturers = lecturers as ArrayList<Lecturer>
            currentLecturer = lecturers.filter {
                it.email == currentUserEmail
            }.first()
            if(currentLecturer.hasPhoto) {
                retrieveProfilePic()
            }
            profileUserName.text = currentLecturer.name
            profileRolerName.text = currentLecturer.position
            retrieveTimetables()
        }
    }
    private fun retrieveProfilePic(){
       val storageRef = storage.reference
       val imageRef = storageRef.child(currentLecturer.id)
        val localFile = File.createTempFile("images", "png")
        imageRef.getFile(localFile).addOnSuccessListener {
            val uri = Uri.fromFile(localFile)
            avatar_image.setImageURI(uri)
        }
    }

    private fun retrieveModules() {
        val moduleCollection = fb.collection(modulesPath)
        moduleCollection.retrieveData(modules as ArrayList<Model>, Module::class.java) {
            retrieveLecturers()
        }
    }

    private fun retrieveTimetables() {
        val timetableCollection = fb.collection(timetablePath)
        timetableCollection.retrieveDataWithMatch("lecturerId",currentLecturer.id,timetableDocMapper.timetables as ArrayList<Model>,
            Timetable::class.java) {
            var timetables = timetableDocMapper.timetables as ArrayList<Timetable>
            var today = Date().toDateOnly()
//            var todayAdded7Days = DateUtil.addDays(today,7).toDateOnly()
            Log.d("today", "today: $today")
//            val sevenDaysAdded = Calendar.getInstance()
//            val timetableDate = Calendar.getInstance()
//            sevenDaysAdded.time = todayAdded7Days
//            var todayTimetables = timetables.filter { it.date?.toDate()?.toDateOnly() == today }
            timetableDocMapper.timetables = timetables.filter {
//                timetableDate.time = it.date?.toDate()?.toDateOnly()
                return@filter it.date?.toDate()?.toDateOnly() == today
            } as ArrayList<Timetable>

            //number of lesson
            number_of_lesson_tv.text =  timetableDocMapper.timetables.size.toString()

            Log.d("Targeted Timetables", timetableDocMapper.timetables.toString())
            timetableDocMapper.updateViewModel(modules)
//            modulesIds = attendances.filter {
//                Log.d("it date", "it date: ${it.date?.toDate()?.toDateOnly()}")
//                it.date?.toDate()?.toDateOnly() == today  }
//                .filter {
//                currentLecturer.id == it.lecturerId
//            }.distinctBy {
//                it.moduleId
//            }.map {
//                it.moduleId
//            } as ArrayList<String>
//            var filteredModules =
//                findMatchedModules(modules, modulesIds)

//            Log.d("Targeted Modules", filteredModules.toString())
//            timetables = filteredModules.map { Timetable(it.name,it.startDate!!.toDate(),it.startTime!!.toDate()) } as ArrayList<Timetable>
            timetableAdapter.notifyDataSetChanged()
            timetable_progressBar.visibility = View.GONE
        }
    }
    fun toNextScreen(timetable: Timetable) {
        var intent = Intent(this, AttendanceActivity::class.java)
        intent.putExtra("timetableId",timetable.id)
        startActivity(intent)
    }

//    fun findMatchedModules(modules: ArrayList<AttendanceActivityModule>, ids: ArrayList<String>): ArrayList<Module> {
//        var result = ArrayList<Module>()
//        for (module in modules) {
//            for (id in ids) {
//                if (id == module.id) {
//                    result.add(module)
//                }
//            }
//        }
//        return result
//    }
}