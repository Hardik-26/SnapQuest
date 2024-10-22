package com.example.snapquest

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class PassedActivity : AppCompatActivity() {

    private lateinit var continueBtn: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var pointsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_passed)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        continueBtn = findViewById(R.id.cunt_btn)
        pointsText = findViewById(R.id.points)

        continueBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Retrieve the username from SharedPreferences
        val sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE)
        val sharedPreferences2 = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)

        if (username != null) {
            Log.e("PassedActivity", "Username: $username")
            adjustPointsBasedOnTimeRemaining(username, sharedPreferences2)
        } else {
            Log.e("PassedActivity", "No username found in SharedPreferences")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun adjustPointsBasedOnTimeRemaining(username: String, sharedPreferences: SharedPreferences) {
        // Retrieve time remaining and total allowed time from SharedPreferences
        val timeRemaining = (intent.getLongExtra("time",0))/1000
        Log.e("PassedActivity", "Time Remaining: $timeRemaining")
        val totalAllowedTime = 300

        // Ensure valid values are present for both times
        if (totalAllowedTime > 0 && timeRemaining >= 0) {
            // Calculate points based on time remaining. Linear scaling between 10 and 50.
            val pointsToAdd = calculatePointsBasedOnTime(timeRemaining, totalAllowedTime)
            pointsText.text = "Points\n+$pointsToAdd"


            // Update points in Firestore
            updateUserPoints(username, pointsToAdd)
        } else {
            Log.e("PassedActivity", "Invalid time data from SharedPreferences")
        }
    }

    private fun calculatePointsBasedOnTime(timeRemaining: Long, totalAllowedTime: Int): Int {
        val minPoints = 10
        val maxPoints = 50

        // Calculate percentage of time remaining
        val timeRatio = timeRemaining.toFloat() / totalAllowedTime.toFloat()

        // Linearly scale points between minPoints and maxPoints based on time remaining
        return (minPoints + (maxPoints - minPoints) * timeRatio).toInt()
    }

    private fun updateUserPoints(username: String, pointsToAdd: Int) {
        // Reference to the Firestore collection and document
        Log.e("PassedActivity_update", "Updating points for user: $username")
        val userRef = db.collection("Users").document(username)
        Log.e("PassedActivity_update", "User reference: $userRef")

        // Retrieve the current points
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentPoints = document.getLong("points") ?: 0
                    val newPoints = currentPoints + pointsToAdd

                    // Update the points in Firestore
                    userRef.update("points", newPoints)
                        .addOnSuccessListener {
                            Log.e("PassedActivity", "Points successfully updated.")
                            Toast.makeText(this, "$pointsToAdd points added!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("PassedActivity", "Error updating points", e)
                            Toast.makeText(this, "Failed to update points.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("PassedActivity", "User document does not exist.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("PassedActivity", "Error retrieving user document", e)
            }
    }
}
