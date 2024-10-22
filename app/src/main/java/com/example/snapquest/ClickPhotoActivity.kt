package com.example.snapquest

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Base64
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.InputStream

class ClickPhotoActivity : AppCompatActivity() {
    private lateinit var timerView: TextView
    private lateinit var cameraPreview: PreviewView
    private lateinit var clickButton: ImageButton
    private lateinit var imageCapture: ImageCapture
    private val db = FirebaseFirestore.getInstance()
    private var timer: CountDownTimer? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private val sharedPreferences by lazy {
        getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.click_photo)

        // Check if the Camera permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            // If not, request the permission
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
//      else {
//          Permission already granted, you can proceed with camera usage
//          startCamera()
//        }

        timerView = findViewById(R.id.timer)
        cameraPreview = findViewById(R.id.cameraPreview)
        clickButton = findViewById(R.id.click)

        startCamera()

        // Restore the timer and continue countdown
        val remainingTime = sharedPreferences.getLong("timer_remaining", 0L)
        if (remainingTime > 0) {
            startTimer(remainingTime)
        }

        clickButton.setOnClickListener {
            takePhoto()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with camera
                startCamera()
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
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
                val intent = Intent(this@ClickPhotoActivity, FailedActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.start()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Build the preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(cameraPreview.surfaceProvider)
            }

            // Select the camera (back camera)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Build the image capture use case
            imageCapture = ImageCapture.Builder().build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to the camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (exc: Exception) {
                // Log or handle any errors
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues()
        ).build()

        imageCapture.takePicture(
            outputOptions,
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    // Handle the error
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // You can retrieve the image path via outputFileResults.savedUri
                    val image=outputFileResults.savedUri
                    if (image != null) {
                        uploadImageToFirebase(image)
                    }

                    sharedPreferences.edit().putString("captured_image", image.toString()).apply()
                    // Navigate to the next activity, where the image can be displayed
                    val intent = Intent(this@ClickPhotoActivity, PreviewActivity::class.java)
                    startActivity(intent)
                }
            })
    }

    fun uploadImageToFirebase(uri: Uri) {
        // Get a reference to the storage service using the default Firebase App
        val storage: FirebaseStorage = FirebaseStorage.getInstance()

        // Create a storage reference
        val storageRef: StorageReference = storage.reference

        // Create a reference to "images/<image_name>" where <image_name> can be generated or assigned
        val imageRef: StorageReference = storageRef.child("${uri.lastPathSegment}")

        // Compress the image
        var bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
        val compressedImageStream = ByteArrayOutputStream()

        // Compress the bitmap and write to the output stream
        bitmap=Bitmap.createScaledBitmap(bitmap, 996,472, true)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 35, compressedImageStream) // 75 is the quality
        val compressedImageData = compressedImageStream.toByteArray()

        // Upload the compressed image to Firebase Storage
        val uploadTask: UploadTask = imageRef.putBytes(compressedImageData)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Handle successful uploads
            // You can get the download URL if needed
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                Log.e("Chat_URL", "Download URL: $downloadUri")
                sharedPreferences.edit().putString("EncodedImageForGpt", downloadUri.toString()).apply()
            }
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Upload failed: ${exception.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel() // Cancel the timer if the activity is destroyed
    }
}