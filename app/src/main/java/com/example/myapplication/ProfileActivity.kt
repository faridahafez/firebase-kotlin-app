package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference //points to user's node in firebase realtime database

    private var currentBirthDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser //fetches active user from firebase auth

        //if unauthenticated user tries to access profile management directly,
        // they are redirected to sign in and execution is stopped
        if (currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }


        val tvEmail = findViewById<TextView>(R.id.tvUserEmail)
        val tvBirthDate = findViewById<TextView>(R.id.tvUserBirthDate)
        val etName = findViewById<EditText>(R.id.etProfileName)
        val etPhone = findViewById<EditText>(R.id.etProfilePhone)
        val etPassword = findViewById<EditText>(R.id.etProfilePassword)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        val btnSignOut = findViewById<Button>(R.id.btnSignOut)

        //sets email label using stored data
        tvEmail.text = "Email: ${currentUser.email}"

        val uid = currentUser.uid

        //points to child node of signed in user
        dbRef = FirebaseDatabase.getInstance("https://app-ms1-default-rtdb.firebaseio.com").getReference("Users").child(uid)

        // live listener triggered when data changes in firebase or screen
        dbRef.addValueEventListener(object : ValueEventListener {

            //called when data arrives from database
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    //converts json into user data object
                    val user = snapshot.getValue(User::class.java)
                    currentBirthDate = user?.birthDate ?: ""
                    tvBirthDate.text = "Birth Date: ${currentBirthDate.ifEmpty { "Not set" }}"

                    // only populates inputs if the user hasn't started typing yet
                    if (etName.text.isEmpty()) etName.setText(user?.name)
                    if (etPhone.text.isEmpty()) etPhone.setText(user?.phone)
                }
            }

            //database read operation fails
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })

        // save changes made
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newPhone = etPhone.text.toString().trim()
            val newPassword = etPassword.text.toString().trim()

            //reject empty entries
            if (newName.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(this, "Name and Phone cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // update/overwrite node in realtime database
            val updatedUser = User(name = newName, birthDate = currentBirthDate, phone = newPhone)
            dbRef.setValue(updatedUser).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile updated in database!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Database update failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // validate & update password in firebase auth if provided
            if (newPassword.isNotEmpty()) {
                if (newPassword.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                } else {
                    currentUser.updatePassword(newPassword).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                            etPassword.setText("")
                        } else {
                            Toast.makeText(this, "Password update failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        btnSignOut.setOnClickListener {
            //clears authenticated session state
            auth.signOut()
            //closes profile management, redirects to sign in
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
}