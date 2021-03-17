package com.example.firestoreinsetprototype

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreinsetprototype.Adaptor.LecturerRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath.USER_PATH
import com.example.firestoreinsetprototype.Extension.hideKeyboard
import com.example.firestoreinsetprototype.Model.*
import com.example.firestoreinsetprototype.Util.SpinnerUtil
import com.example.firestoreinsetprototype.Util.realTimeUpdate
import com.example.firestoreinsetprototype.Util.retrieveData
import com.example.firestoreinsetprototype.Util.retrieveDataWithMatch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_lecturer.*
import java.io.File

class LecturerActivity : AppCompatActivity() {

    //for write
    private val lecturersPath = FirestoreCollectionPath.LECTURERS_PATH
    //for delete
    private val modulesPath = FirestoreCollectionPath.MODULES_PATH
    private val timetablesPath = FirestoreCollectionPath.TIMETABLES_PATH
    private val attendancesPath = FirestoreCollectionPath.ATTENDANCES_PATH
    private val lecturers = ArrayList<Lecturer>()
    private val users = ArrayList<User>()
    private var selectedUser: User? = null
    private var fb = FirebaseFirestore.getInstance()
    private var storage = FirebaseStorage.getInstance()
    lateinit var lecturerAdapter: LecturerRecyclerViewAdaptor
    companion object val PICK_IMAGE = 1
    private var profileImageURL : Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lecturer)

        val linearLayoutManager = LinearLayoutManager(this)
        lecturer_recyclerview.layoutManager = linearLayoutManager
        lecturer_recyclerview.adapter = LecturerRecyclerViewAdaptor(lecturers)
        lecturerAdapter = (lecturer_recyclerview.adapter as LecturerRecyclerViewAdaptor)
        lecturerAdapter.onItemClickListener =
            object : LecturerRecyclerViewAdaptor.OnItemClickListener {
                override fun onClick(v: View, position: Int) {
                    Log.d("onClick", "$position")
                }

                override fun onLongClick(v: View, position: Int): Boolean {
                    Log.d("OnItemLongClickListener", "long pressed at $position")
                    presentDeleteAlert(lecturers[position])
                    return true
                }
            }

        lecturer_insert.setOnClickListener {
            var id = lecturer_id_et.text.toString().trim()
            var name = lecturer_name_et.text.toString().trim()
            var position = position_et.text.toString().trim()
            var department = lecturer_department_et.text.toString().trim()
            var email = selectedUser?.email

            if (id != "" && name != "" && email != null && position != "" && department != "") {
                var lecturer =
                    Lecturer(
                        id,
                        name,
                        email,
                        position,
                        department,
                        profileImageURL != null
                    )
                Log.d("Lecturer", "$lecturer")
                hideKeyboard()
                clearInputs()
               if(!(lecturers.any { email == it.email })) {
                   writeLecturer(lecturer)
                   Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT).show()
               }else {
                   Toast.makeText(this, "This user is already a lecturer", Toast.LENGTH_SHORT).show()
               }
            }else
                Toast.makeText(this, "Please fill all fields before insert", Toast.LENGTH_SHORT).show()
        }
        btn_upload_image.setOnClickListener {
            openGallery()
        }
        retrieveLecturers()
        retrieveUsers()
    }

    override fun onStop() {
        super.onStop()
        clearInputs()
        lecturerAdapter.clear()
    }

    private fun openGallery(){
        var gallery = Intent()
        gallery.type = "image/*"
        gallery.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(gallery,"Select Picture"),PICK_IMAGE)
    }

    private fun presentDeleteAlert(lecturer: Lecturer) {
        val dialog =  AlertDialog.Builder(this)
        dialog.setCancelable(true).setMessage("Are you sure to delete Lecturer ID : ${lecturer.id}").setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
            deleteLecturer(lecturer)
        }).setNegativeButton("No", DialogInterface.OnClickListener{ dialog, _ ->
            dialog.cancel()
        }).show()
    }

    private fun deleteLecturer(lecturer: Lecturer) {
        val lecturerCollection = fb.collection(lecturersPath)
        lecturerCollection.realTimeUpdate(lecturers as ArrayList<Model>, Lecturer::class.java) {
            lecturerAdapter.notifyDataSetChanged()
        }
        lecturerCollection.document(lecturer.id.toString()).delete().addOnSuccessListener {
            Log.d("", "Lecturer successfully deleted! ")
            deleteOtherRelatedData(lecturer)
        }
            .addOnFailureListener { e->
                Log.w("", "Error deleting document",e )
            }
    }
    private fun deleteOtherRelatedData(lecturer : Lecturer){
        val moduleColleciton = fb.collection(modulesPath)
        val timetableCollection = fb.collection(timetablesPath)
        val attendanceCollection = fb.collection(attendancesPath)
        var modules = ArrayList<Module>()
        var timetables = ArrayList<Timetable>()
        var attendances = ArrayList<Attendance>()
        moduleColleciton.retrieveDataWithMatch("lecturerId",lecturer.id,modules as ArrayList<Model>,Module::class.java) {
            modules.forEach{module ->
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
                //delete module
                moduleColleciton.document(module.id).delete()
            }
        }
    }

    private fun retrieveLecturers() {
        val lecturersCollection = fb.collection(lecturersPath)
        lecturersCollection.retrieveData(lecturers as ArrayList<Model>, Lecturer::class.java) {
            lecturerAdapter.notifyDataSetChanged()
        }
    }


    private fun writeLecturer(lecturer: Lecturer) {
        val lecturerCollection = fb.collection(lecturersPath)
        lecturerCollection.realTimeUpdate(lecturers as ArrayList<Model>, Lecturer::class.java) {
            lecturerAdapter.notifyDataSetChanged()
        }
        lecturerCollection.document(lecturer.id.toString())
            .set(lecturer)
            .addOnSuccessListener {
                Log.d("", "Lecturer successfully written!")
            }
            .addOnFailureListener { e->
                Log.w("", "Error writing document",e )
            }
        //write image
        profileImageURL?.let {
            uploadImage(lecturer.id,it)
        }
    }

    private fun retrieveUsers(){
        val userCollection = fb.collection(USER_PATH)
        userCollection.retrieveDataWithMatch("role","lecturer",users as ArrayList<Model>, User::class.java) {
            var users = users as ArrayList<User>
            val usersString = (users.map { it.email } as ArrayList<String>)
            val emailSpinner: Spinner = findViewById(R.id.email_Spinner)
            val userAdapter =  SpinnerUtil.setupSpinner(
                this,
                emailSpinner,
                usersString
            ){
                selectedUser = users[it]
            }
            userAdapter.notifyDataSetChanged()
        }
    }

    private fun clearInputs() {
        lecturer_id_et.text.clear()
        lecturer_name_et.text.clear()
        position_et.text.clear()
        lecturer_department_et.text.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageURL = data?.data
            avatar_image.setImageURI(imageURL)
            profileImageURL = imageURL
            Log.d("imageURL", "onActivityResult: $imageURL")
        }
    }

    private fun uploadImage(fileName : String, imageURL : Uri) {
        var storageRef = storage.reference
        val imageRef = storageRef.child(fileName)
//        var file = Uri.fromFile(File(""))
        Log.d("pathName ", "uploadImage: ${imageRef.path}")
        val uploadTask = imageRef.putFile(imageURL)
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            Log.d("uploadImage", "uploadImage: $taskSnapshot")
        }
    }


}
