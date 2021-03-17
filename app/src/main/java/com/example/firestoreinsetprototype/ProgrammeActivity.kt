package com.example.firestoreinsetprototype

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreinsetprototype.Adaptor.ProgrammeRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath
import com.example.firestoreinsetprototype.Extension.hideKeyboard
import com.example.firestoreinsetprototype.Model.Model
import com.example.firestoreinsetprototype.Model.Programme
import com.example.firestoreinsetprototype.Util.realTimeUpdate
import com.example.firestoreinsetprototype.Util.retrieveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_programme.*

class ProgrammeActivity : AppCompatActivity() {

    private val programmesPath = FirestoreCollectionPath.PROGRAMMES_PATH

    private val programmes = ArrayList<Programme>()
    private val fb = FirebaseFirestore.getInstance()
    lateinit var programmeAdapter : ProgrammeRecyclerViewAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programme)

        val linearLayoutManager = LinearLayoutManager(this)
        programme_recyclerView.layoutManager = linearLayoutManager
        programme_recyclerView.adapter = ProgrammeRecyclerViewAdaptor(programmes)
        programmeAdapter = (programme_recyclerView.adapter as ProgrammeRecyclerViewAdaptor)
        programmeAdapter.onItemClickListener = object : ProgrammeRecyclerViewAdaptor.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                Log.d("onClick", "$position")
            }

            override fun onLongClick(v: View, position: Int) : Boolean {
                Log.d("OnItemLongClickListener", "long pressed at $position")
                presentDeleteAlert(programmes[position])
                return true
            }
        }
//        student_list_view.setOnItemLongClickListener {  parent, view, position, id ->
//            Log.d("OnItemLongClickListener", "long pressed at $position and id $id and the view $view")
//            presentDeleteAlert(students[position])
//            return@setOnItemLongClickListener(true)
//        }

        programme_insert.setOnClickListener {
            var id = programme_id_et.text.toString().trim()
            var name = programme_name_et.text.toString().trim()

            if (id != "" && name != "" ) {
                var programme =
                    Programme(
                        id,
                        name
                    )
                Log.d("Programme", "$programme")
                hideKeyboard()
                clearInputs()
                writeProgramme(programme)
                Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT)
                    .show()
            } else
                Toast.makeText(
                    this,
                    "Please fill all fields before insert",
                    Toast.LENGTH_SHORT
                ).show()
        }

        retrieveProgramme()
    }

    override fun onStop() {
        super.onStop()
        clearInputs()
        programmeAdapter.clear()
    }

    private fun clearInputs(){
        programme_id_et.text.clear()
        programme_name_et.text.clear()
    }

    private fun retrieveProgramme() {
        val programmeCollection = fb.collection(programmesPath)
        programmeCollection.retrieveData(programmes as ArrayList<Model>,
            Programme::class.java) {
            programmeAdapter.notifyDataSetChanged()
        }
    }

    private fun writeProgramme(programme: Programme) {
        val programmeCollection = fb.collection(programmesPath)
        programmeCollection.realTimeUpdate(programmes as ArrayList<Model>,
            Programme::class.java) {
            programmeAdapter.notifyDataSetChanged()
        }
        programmeCollection.document(programme.id)
            .set(programme)
            .addOnSuccessListener {
                Log.d("", "Programme successfully written!")
            }
            .addOnFailureListener {e->
                Log.w("", "Error writing document", e)
            }
    }


    private fun presentDeleteAlert(programme: Programme){
        val dialog =  AlertDialog.Builder(this)
        dialog.setCancelable(true).setMessage("Are you sure to delete Programme ID : ${programme.id}").setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
            deleteProgramme(programme)
        }).setNegativeButton("No", DialogInterface.OnClickListener{ dialog , _ ->
            dialog.cancel()
        }).show()
    }

    private fun deleteProgramme(programme: Programme) {
        val programmeCollection = fb.collection(programmesPath)
        programmeCollection.realTimeUpdate(programmes as ArrayList<Model>,
            Programme::class.java) {
            programmeAdapter.notifyDataSetChanged()
        }
        programmeCollection.document(programme.id.toString()).delete().addOnSuccessListener {
            Log.d("", "Programme successfully deleted!")
        }
            .addOnFailureListener {e->
                Log.w("", "Error deleting document", e)
            }
    }

}