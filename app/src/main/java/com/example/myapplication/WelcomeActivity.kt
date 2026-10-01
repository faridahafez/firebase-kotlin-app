package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser //fetches currently logged in user from firebase

        //if unauthenticated user tries to access welcome page directly,
        // they are redirected to sign in and execution is stopped
        if (currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        val tvWelcomeTitle = findViewById<TextView>(R.id.tvWelcomeTitle)
        val btnGoToProfile = findViewById<Button>(R.id.btnGoToProfile)
        val btnSignOut = findViewById<Button>(R.id.btnSignOutWelcome)

        //custom greeting
        tvWelcomeTitle.text = "  Welcome to My App!"

        //listen for taps on profile management button
        btnGoToProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        //listen for taps on sign out button
        btnSignOut.setOnClickListener {
            //clears auth session state
            auth.signOut()
            //redirects back to sign in
            startActivity(Intent(this, SignInActivity::class.java))
            finish()  //destroys activity
        }
    }
}