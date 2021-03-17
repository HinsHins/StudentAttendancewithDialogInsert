package com.example.firestoreinsetprototype

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath
import com.example.firestoreinsetprototype.Extension.hideKeyboard
import com.example.firestoreinsetprototype.Model.Model
import com.example.firestoreinsetprototype.Model.User
import com.example.firestoreinsetprototype.Util.retrieveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val userPath = FirestoreCollectionPath.USER_PATH
    private val users = ArrayList<User>()
    private var fb = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    lateinit var nextScreen : Intent
    var progressDialog : AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        btn_login.setOnClickListener {
            it.isEnabled = false
            hideKeyboard()
            val email = et_email.text.toString().trim()
            val password = et_password.text.toString().trim()
            if(email=="") {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                it.isEnabled = true
            }else if (password == "") {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                it.isEnabled = true
            }else{
                showProgressBar()
                signIn(email, password)
            }
        }

        hideProgressBar()

        tv_forgotPwd.setOnClickListener{ view ->
            showRecoveryAlert()
        }

        retrieveUsers()

//        register_button.setOnClickListener {
////            val intent = Intent(this,RegisterActivity::class.java)
////            startActivity(intent)
////        }



    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        Log.d("user", "$user")
    }

    fun showRecoveryAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Account Recovery")
        dialog.setMessage("Please enter your email")
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(input)
        dialog.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, _ ->
            val email = input.text.toString().trim()
            if(email!= ""){
                showProgressDialog()
                beginRecovery(email)
            }else {
                Toast.makeText(this, "please enter an email", Toast.LENGTH_SHORT).show()
            }
        }).setNegativeButton("No", DialogInterface.OnClickListener{ dialog, _ ->
            dialog.cancel()
        }).show()
    }

    fun showProgressDialog() {
        val builder = AlertDialog.Builder(this)
        val loadingView = layoutInflater.inflate(R.layout.progress_dialog,null)
        val tv_message = loadingView.findViewById<TextView>(R.id.message)
        tv_message.text = "Sending..."
        builder?.setView(loadingView)
        builder?.setCancelable(false)
        progressDialog = builder.create()
        progressDialog?.show()
//        Handler(Looper.getMainLooper()).postDelayed({
//            dialog.dismiss()
//        },5000)
    }

    private fun signIn(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Success", "signInWithEmail:success")
                    val user = auth.currentUser
                    val currentUser = users.filter {
                        user?.uid == it.id
                    }.first()
                    if(currentUser.role == "admin"){
                        nextScreen = Intent(this, MainActivity::class.java)
                        nextScreen.putExtra("email",currentUser.email)
                    }else{
                        nextScreen = Intent(this, ProfileActivity::class.java)
                        nextScreen.putExtra("email",currentUser.email)
                    }

                    startActivity(nextScreen)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Fail", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        this, "Incorrect password or email",
                        Toast.LENGTH_SHORT
                    ).show()

                }
                hideProgressBar()
                btn_login.isEnabled = true
            }
    }

    fun beginRecovery(email: String) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            progressDialog?.dismiss()
            if(task.isSuccessful) {
                Toast.makeText(this, "email has been sent", Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(this, "Failed to send recovery", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            progressDialog?.dismiss()
            Toast.makeText(this, "${it.toString()}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun retrieveUsers(){
        val userCollection = fb.collection(FirestoreCollectionPath.USER_PATH)
        userCollection.retrieveData(users as ArrayList<Model>, User::class.java) {

        }
    }

    private fun hideProgressBar() {
        login_progressBar.visibility = View.INVISIBLE
        btn_login.text = "Login"
    }
    private fun showProgressBar() {
        login_progressBar.visibility = View.VISIBLE
        btn_login.text = ""
    }
}