package com.example.snapquest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class LoginActivity : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var signupLink: TextView

    private val db = FirebaseFirestore.getInstance()
    private val PREFERENCES_NAME = "login_prefs"
    private val PREF_USERNAME = "username"
    private val PREF_SESSION_EXPIRES = "session_expires"
    private val SESSION_DURATION_DAYS = 30L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)

        loginButton = findViewById(R.id.login_button)
        signupLink = findViewById(R.id.signupLink)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)

        // Check if there is an active session
        if (isSessionActive()) {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
            finish()  // Close the login activity
        }

        loginButton.setOnClickListener {
            login()
        }

        signupLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun login() {
        val name = username.text.toString().trim()
        val pass = password.text.toString().trim()

        if (name.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Fill all Fields!", Toast.LENGTH_LONG).show()
            return
        }

        db.collection("Users").document(name).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val storedPassword = document.getString("password")

                    if (storedPassword == pass) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        saveSession(name)
                        val intent = Intent(this, TaskActivity::class.java)
                        startActivity(intent)
                        finish()  // Close the login activity
                    } else {
                        Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Username does not exist", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error logging in: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to check if the session is active
    private fun isSessionActive(): Boolean {
        val sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(PREF_USERNAME, null)
        val sessionExpires = sharedPreferences.getLong(PREF_SESSION_EXPIRES, 0)

        if (username != null && sessionExpires > System.currentTimeMillis()) {
            return true
        }
        return false
    }

    // Function to save the session in SharedPreferences
    private fun saveSession(username: String) {
        val sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, SESSION_DURATION_DAYS.toInt())
        val sessionExpires = calendar.timeInMillis

        editor.putString(PREF_USERNAME, username)
        editor.putLong(PREF_SESSION_EXPIRES, sessionExpires)
        editor.apply()
    }

    // Function to clear the session (use this when logging out)
    private fun clearSession() {
        val sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
