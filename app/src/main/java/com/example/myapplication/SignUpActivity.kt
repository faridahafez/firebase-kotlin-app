package com.example.myapplication

//screen transition tool
import android.content.Intent
import android.os.Bundle
//UI widgets
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity // activity base class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() { //inheriting

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance() //instance to connect to firebase backend

        //binding variables to visual fields using resource IDs
        val etName = findViewById<EditText>(R.id.etNameSignUp)
        val etBirthDate = findViewById<EditText>(R.id.etBirthDateSignUp)
        val etPhone = findViewById<EditText>(R.id.etPhoneSignUp)
        val etEmail = findViewById<EditText>(R.id.etEmailSignUp)
        val etPassword = findViewById<EditText>(R.id.etPasswordSignUp)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvGoToSignIn = findViewById<TextView>(R.id.tvGoToSignIn)

        //event listener triggered upon tapping sign up button
        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val birthDate = etBirthDate.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            //if a field is empty, execution is stopped early
            if (name.isEmpty() || birthDate.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password) //sends info to firebase auth to register user
                //listens for firebase response
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) { //successful registration
                        val uid = auth.currentUser?.uid ?: "" //fetching unique user ID
                        val user = User(name = name, birthDate = birthDate, phone = phone)

                        // save details to firebase realtime database
                        FirebaseDatabase.getInstance("https://app-ms1-default-rtdb.firebaseio.com").getReference("Users")

                        //points to root node, creates sub node uid, writes user object into node
                        FirebaseDatabase.getInstance().getReference("Users")
                            .child(uid)
                            .setValue(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, WelcomeActivity::class.java)) //opens welcome page upon success
                                    finish() //destroys activity
                                } else {
                                    Toast.makeText(this, "Database Error: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        //listens for taps on having an account, opens sign in and closes sign up
        tvGoToSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
}