package com.example.snapquest

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var name : TextView
    private lateinit var points : TextView
    private lateinit var lastClickedImage : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile)

        name = findViewById(R.id.name)
        points = findViewById(R.id.points)
        lastClickedImage = findViewById(R.id.lastClickedImage)
    }
}