package com.example.snapquest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayInputStream

class ProfileActivity : AppCompatActivity() {
    private lateinit var name: TextView
    private lateinit var points: TextView
    private lateinit var lastClickedImage: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val PREFERENCES_NAME = "login_prefs"
    private val PREF_USERNAME = "username"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile)

        name = findViewById(R.id.name)
        points = findViewById(R.id.points)
        lastClickedImage = findViewById(R.id.lastClickedImage)

        // Retrieve the username from SharedPreferences
        val sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(PREF_USERNAME, null)

        if (username != null) {
            // Fetch user data from Firestore
            loadUserData(username)
        } else {
            Toast.makeText(this, "No user session found", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to load user data from Firestore
    private fun loadUserData(username: String) {
        db.collection("Users").document(username).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Retrieve and set username and points
                    val userName = document.getString("username")
                    val userPoints = document.getLong("points") ?: 0
                    val imageBase64 = document.getString("Image")

                    name.text = userName
                    points.text = userPoints.toString()

                    // Decode the base64 image and set it to ImageView
                    if (!imageBase64.isNullOrEmpty()) {
                        val bitmap = decodeBase64ToBitmap(imageBase64)
                        lastClickedImage.setImageBitmap(bitmap)
                    } else {
                        Toast.makeText(this, "No image found for user", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to decode a base64 string into a Bitmap
    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            val inputStream = ByteArrayInputStream(decodedBytes)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, "Error decoding image", Toast.LENGTH_SHORT).show()
            null
        }
    }
}