package com.example.firestoreinsetprototype

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_welcome.text = "Logged in as ${auth.currentUser?.email} "
        cardStudent.setOnClickListener {
            val intent = Intent(this, StudentActivity::class.java)
            startActivity(intent)
        }

        cardLecturer.setOnClickListener {
            val intent = Intent(this, LecturerActivity::class.java)
            startActivity(intent)
        }

        cardModule.setOnClickListener{
            val intent = Intent(this,ModuleActivity::class.java)
            startActivity(intent)
        }

        cardLogout.setOnClickListener {
            val intent = Intent(this,ProgrammeActivity::class.java)
            startActivity(intent)
        }

        toRegisterPage.setOnClickListener {
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}