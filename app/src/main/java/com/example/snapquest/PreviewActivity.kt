package com.example.snapquest

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Base64
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import okhttp3.*
import okhttp3.MediaType.Companion.get
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit


class PreviewActivity : AppCompatActivity() {
    private lateinit var timerView: TextView
    private var timer: CountDownTimer? = null
    private lateinit var clickImage: ImageView
    private lateinit var retake: ImageButton
    private lateinit var verify: ImageButton
    private val db = FirebaseFirestore.getInstance()
    private val apiKey = "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"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.preview)

        timerView = findViewById(R.id.timer)
        clickImage = findViewById(R.id.imageClicked)
        retake = findViewById(R.id.retake)
        verify = findViewById(R.id.verify)

        // Load the image from shared preferences
        loadCapturedImage()

        // Retake photo
        retake.setOnClickListener {
            val intent = Intent(this, ClickPhotoActivity::class.java)
            startActivity(intent)
        }

        // Verify photo
        verify.setOnClickListener {
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
        val sharedPreferences = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val encodedImage = sharedPreferences.getString("captured_image", null)

        if (encodedImage != null) {
            val bitmapBytes = Base64.decode(encodedImage, Base64.DEFAULT)

            CoroutineScope(Dispatchers.IO).launch {
                // Assuming you have a function to make API call
                val response = callChatGPTApi(prompt, bitmapBytes)

                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    val output = jsonResponse.getString("output")

                    // Verify if the response matches the required output
                    if (output.contains(requiredOutput, ignoreCase = true)) {
                        // Redirect to PassedActivity
                        val intent = Intent(this@PreviewActivity, PassedActivity::class.java)
                        startActivity(intent)
                    } else {
                        // Redirect to FailedActivity
                        val intent = Intent(this@PreviewActivity, FailedActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@PreviewActivity, "Failed to get a response from the API", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "No image found", Toast.LENGTH_SHORT).show()
        }
    }

    // Dummy function to send the request to the ChatGPT API, replace with actual implementation
    private fun callChatGPTApi(prompt: String, imageBytes: ByteArray): String? {
        // test
        return TODO("Provide the return value")
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel() // Cancel the timer if the activity is destroyed
    }
}
