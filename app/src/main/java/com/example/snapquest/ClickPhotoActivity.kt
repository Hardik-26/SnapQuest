package com.example.snapquest

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.google.firebase.firestore.FirebaseFirestore

class ClickPhotoActivity : AppCompatActivity() {
    private lateinit var timer : TextView
    private lateinit var cameraPreview : PreviewView
    private lateinit var click : ImageButton
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.click_photo)

    }
}