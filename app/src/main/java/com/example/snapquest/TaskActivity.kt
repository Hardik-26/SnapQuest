package com.example.snapquest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskActivity : AppCompatActivity() {
    private lateinit var task: TextView
    private lateinit var click: Button
    private lateinit var leaderboard: ImageButton
    private lateinit var profile: ImageButton
    private lateinit var generateTask: Button
    private lateinit var timerView: TextView

    private val db = FirebaseFirestore.getInstance()
    private val sharedPreferences by lazy {
        getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
    }

    private var timer: CountDownTimer? = null
    private val TASK_DURATION_MILLIS = 5 * 60 * 1000L // 5 minutes in milliseconds


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.task)

        task = findViewById(R.id.task)
        click = findViewById(R.id.click)
        leaderboard = findViewById(R.id.leaderboard)
        profile = findViewById(R.id.profile)
        generateTask = findViewById(R.id.generateTask)
        timerView = findViewById(R.id.timer)

        // Hide the "Click" button initially
        click.isEnabled = false
        click.visibility = View.INVISIBLE

        leaderboard.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }

        profile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        click.setOnClickListener{
            val intent = Intent(this, ClickPhotoActivity::class.java)
            startActivity(intent)
        }

        generateTask.setOnClickListener {
            if (canGenerateTask()) {
                generateNewTask()
            } else {
                Toast.makeText(this, "You can only generate one task per day!", Toast.LENGTH_SHORT).show()
            }
        }

        // Restore timer state if it was running
        val remainingTime = sharedPreferences.getLong("timer_remaining", 0L)
        if (remainingTime > 0) {
            startTimer(remainingTime)
        }
    }

    private fun generateNewTask() {
        // Fetch a random document from Firestore's "Tasks" collection

        val randomTaskId = (1..7).random().toString()
        sharedPreferences.edit().putString("taskId", randomTaskId).apply()


        db.collection("Tasks").document(randomTaskId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val taskText = document.getString("task")
                    task.text = taskText

                    // Enable the "Click" button after generating a task
                    click.isEnabled = true
                    click.visibility = View.VISIBLE

                    // Save the current time to track daily task generation
                    saveTaskGenerationTime()

                    // Start the timer
                    startTimer(TASK_DURATION_MILLIS)
                } else {
                    Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading task: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startTimer(durationMillis: Long) {
        timer?.cancel() // Cancel any previous timer if running

        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                timerView.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

                // Save remaining time to SharedPreferences to persist the timer state
                sharedPreferences.edit().putLong("timer_remaining", millisUntilFinished).apply()
            }

            override fun onFinish() {
                sharedPreferences.edit().remove("timer_remaining").apply() // Clear timer on finish
                val intent = Intent(this@TaskActivity, FailedActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.start()
    }

    private fun saveTaskGenerationTime() {
        val currentDate = getCurrentDate()
        sharedPreferences.edit().putString("last_task_date", currentDate).apply()
    }

    private fun canGenerateTask(): Boolean {
        val lastTaskDate = sharedPreferences.getString("last_task_date", null)
        val currentDate = getCurrentDate()

        return lastTaskDate == null || lastTaskDate != currentDate
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel() // Cancel the timer when the activity is destroyed
    }
}