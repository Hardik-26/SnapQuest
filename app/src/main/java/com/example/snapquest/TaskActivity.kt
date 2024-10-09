package com.example.snapquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class TaskActivity : AppCompatActivity() {
    private lateinit var description : TextView
    private lateinit var click : Button
    private lateinit var leaderboard : ImageButton
    private lateinit var home : ImageButton
    private lateinit var profile : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.task)

        description = findViewById(R.id.description)
        click = findViewById(R.id.click)

        leaderboard.setOnClickListener{
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }

        home.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        profile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

    }
}