package com.example.firestoreinsetprototype

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath
import com.example.firestoreinsetprototype.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val usersPath = FirestoreCollectionPath.USER_PATH
    private val fb = FirebaseFirestore.getInstance()
    private var selectedRole = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button.setOnClickListener {
            val email = email_et.text.toString().trim()
            val password = password_et.text.toString().trim()
            val confirmPassword = confirmPassword_et.text.toString().trim()

            if(email=="")
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            else if(password=="")
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            else if(password!=confirmPassword)
                Toast.makeText(this, "Password does not match ,please try again", Toast.LENGTH_SHORT).show()
            else{
                registerAccount(email,password)
            }

        }
    }

    fun registerAccount(email:String,password:String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Success", "createUserWithEmail:success")
                    val user = auth.currentUser

                    if(user != null && user.email != null ) {
                        val myUser =
                            User(
                                user.uid,
                                user.email ?: "",
                                selectedRole
                            )
                        writeUser(myUser)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Fail", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }

                // ...
            }
    }

    private fun writeUser(user: User) {
        val programmeCollection = fb.collection(usersPath)

        programmeCollection.document(user.id)
            .set(user)
            .addOnSuccessListener {
                Log.d("", "User successfully written!")
                val i = Intent(this,MainActivity::class.java)
                startActivity(i)
                Toast.makeText(this, "${user.role} added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Log.w("", "Error writing document", e)
            }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.admin_Radio ->
                    if (checked) {
                        selectedRole = "admin"
                    }
                R.id.lecturer_Radio ->
                    if (checked) {
                        selectedRole = "lecturer"
                    }
            }
        }
    }
}