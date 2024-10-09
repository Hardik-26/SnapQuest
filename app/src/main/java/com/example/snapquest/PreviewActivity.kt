package com.example.snapquest

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class PreviewActivity : AppCompatActivity() {
    private lateinit var timer : TextView
    private lateinit var clickImage : ImageView
    private lateinit var retake : ImageButton
    private lateinit var verify : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.preview)

        timer = findViewById(R.id.timer)
        clickImage = findViewById(R.id.imageClicked)
        retake = findViewById(R.id.retake)
        verify = findViewById(R.id.verify)
    }
}