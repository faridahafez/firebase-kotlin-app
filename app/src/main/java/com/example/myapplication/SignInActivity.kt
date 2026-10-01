package com.example.myapplication

//screen transition tool
import android.content.Intent
import android.os.Bundle
//UI widgets
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
//activity base class
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() { //inheriting

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance() //firebase auth instance

        // check if user is already logged in
        if (auth.currentUser != null) {
            startActivity(Intent(this, WelcomeActivity::class.java)) //go to welcome if logged in
            finish() //destroy activity
            return //exit early
        }

        setContentView(R.layout.activity_sign_in)

        //binding variables to visual fields using resource IDs
        val etEmail = findViewById<EditText>(R.id.etEmailSignIn)
        val etPassword = findViewById<EditText>(R.id.etPasswordSignIn)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val tvGoToSignUp = findViewById<TextView>(R.id.tvGoToSignUp)

        //listens for sign in button click
        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            //stops execution if any field is empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //sends email and password to firebase auth for verification
            auth.signInWithEmailAndPassword(email, password)
                //listens for authentication results
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) { //credentials are correct
                        Toast.makeText(this, "Sign In Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, WelcomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        //listens for taps on no account , opens sign up and closes sign in
        tvGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }
}