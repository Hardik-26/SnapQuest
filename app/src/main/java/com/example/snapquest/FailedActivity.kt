package com.example.snapquest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.enableEdgeToEdge

class FailedActivity : AppCompatActivity() {

    private lateinit var continueBtn: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_failed)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        continueBtn = findViewById(R.id.cunt_btn_F)
        continueBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Retrieve the username from SharedPreferences
        val sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        if (username != null) {
            Log.e("FailedActivity", "Username: $username")
            deductPoints(username)
        } else {
            Log.e("FailedActivity", "No username found in SharedPreferences")
        }
    }

    private fun deductPoints(username: String) {
        // Reference to the Firestore collection and document
        val userRef = db.collection("Users").document(username)

        // Retrieve the current points
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentPoints = document.getLong("points") ?: 0
                    val newPoints = currentPoints - 10

                    // Update the points in Firestore
                    userRef.update("points", newPoints)
                        .addOnSuccessListener {
                            Log.d("FailedActivity", "Points successfully updated.")
                            Toast.makeText(this, "10 points deducted!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("FailedActivity", "Error updating points", e)
                            Toast.makeText(this, "Failed to update points.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("FailedActivity", "User document does not exist.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FailedActivity", "Error retrieving user document", e)
            }
    }
}
