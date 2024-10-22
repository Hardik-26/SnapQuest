package com.example.snapquest

import android.app.VoiceInteractor.Prompt
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.io.BufferedReader
import java.io.InputStreamReader
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask


class PreviewActivity : AppCompatActivity() {
    private lateinit var timerView: TextView
    private var timer: CountDownTimer? = null
    private lateinit var clickImage: ImageView
    private lateinit var retake: ImageButton
    private lateinit var verify: ImageButton
    private val db = FirebaseFirestore.getInstance()
    var apiKey: String? = "Test"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.preview)

        timerView = findViewById(R.id.timer)
        clickImage = findViewById(R.id.imageClicked)
        retake = findViewById(R.id.retake)
        verify = findViewById(R.id.verify)

        db.collection("Key").document("key").get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    apiKey = document.getString("ApiKey")
                } else {
                    Toast.makeText(this, "No API Key found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error logging in: ${e.message}", Toast.LENGTH_SHORT).show()
            }



        // Load the image from shared preferences
        loadCapturedImage()
        // Retake photo
        retake.setOnClickListener {
            val intent = Intent(this, ClickPhotoActivity::class.java)
            startActivity(intent)
        }

        // Verify photo
        verify.setOnClickListener {
            stopGlobalTimer()
            verifyPhoto()
        }
        val sharedPreferences = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val remainingTime = sharedPreferences.getLong("timer_remaining", 0L)
        if (remainingTime > 0) {
            startTimer(remainingTime)
        }
    }

    private fun startTimer(durationMillis: Long) {
        val sharedPreferences = getSharedPreferences("task_prefs", MODE_PRIVATE)
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
                val intent = Intent(this@PreviewActivity, FailedActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.start()
    }


    // Load the captured image from SharedPreferences
    private fun loadCapturedImage() {
        val sharedPreferences = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val encodedImage = sharedPreferences.getString("captured_image", null)
        encodedImage?.let{
            val imageUri=Uri.parse(it)
            clickImage.setImageURI(imageUri)
        }
    }

    // Function to verify the photo by sending it to ChatGPT API
    private fun verifyPhoto() {
        // Fetch the task's prompt and required output from Firestore
        val sharedPreferences = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val taskId = sharedPreferences.getString("taskId", null)

        if (taskId == null) {
            Toast.makeText(this, "No task found", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Tasks").document(taskId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val prompt = document.getString("prompt") ?: ""
                val requiredOutput = document.getString("requiredOutput") ?: ""

                // Send the image and prompt to ChatGPT API
                sendToChatGPT(prompt, requiredOutput)
            } else {
                Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error fetching task: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendToChatGPT(prompt: String, requiredOutput: String) {
        val response= callChatGPTApi(prompt)
        Log.e("Chat_Response_final",response.toString())
        if (response != null) {
            if(response.contains("Yes") || response.contains("Yes.") || response.contains("yes") || response.contains("yes.")){
                val intent = Intent(this@PreviewActivity, PassedActivity::class.java)
                val sharedPreferences = getSharedPreferences("task_prefs", MODE_PRIVATE)
                val time = sharedPreferences.getLong("timer_remaining", 0)
                intent.putExtra("time", time)
                startActivity(intent)
                finish()
            }
            else{
                val intent = Intent(this@PreviewActivity, FailedActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }



    fun callChatGPTApi(prompt: String): String? {

        val sharedPreferences = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val image = sharedPreferences.getString("EncodedImageForGpt", null)

        val curlCommand = arrayOf(
            "curl", "-X", "POST",
            "https://api.openai.com/v1/chat/completions",
            "-H", "Authorization: Bearer $apiKey",
            "-H", "Content-Type: application/json",
            "--max-time", "180",
            "--connect-timeout", "120",
            "-d", """
        {
            "model": "gpt-4o",
            "messages": [
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "text",
                            "text": "$prompt"
                        },
                        {
                            "type": "image_url",
                            "image_url": {
                                "url": "$image"
                            }
                        }
                    ]
                }
            ]
        }
        """.trimIndent()
        )
        Log.e("Chat_Curl", curlCommand.toString())
        try {
            // Create a process builder to execute the curl command
            val processBuilder = ProcessBuilder(*curlCommand)
            val process = processBuilder.start()
            // Capture and print the response
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            var count=0
            while (reader.readLine().also { line = it } != null) {
                if(count==10){
                    Log.e("Chat_response_Line", line.toString())
                    break
                }
                count += 1
            }
            // Wait for the process to finish
            val exitCode = process.waitFor()
            Log.e("Chat_response", "Exit code: $exitCode")
            // Return the response as a string
            return line.toString()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "Null"
    }

    private fun stopGlobalTimer() {
        val sharedPreferences = getSharedPreferences("task_prefs", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("is_timer_stopped", true).apply()
        // Cancel the local timer if running
        timer?.cancel()
    }


    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel() // Cancel the timer if the activity is destroyed
    }
}
