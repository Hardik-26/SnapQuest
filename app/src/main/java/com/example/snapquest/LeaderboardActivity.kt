package com.example.snapquest

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var leaderboardRecyclerView: RecyclerView
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.leaderboard)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize RecyclerView
        leaderboardRecyclerView = findViewById(R.id.leaderboard_recycler_view)
        leaderboardRecyclerView.layoutManager = LinearLayoutManager(this)

        leaderboardAdapter = LeaderboardAdapter(userList)
        leaderboardRecyclerView.adapter = leaderboardAdapter
        // Fetch top users and update RecyclerView
        fetchTopUsers()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchTopUsers() {
        // Query to fetch the top 10 users sorted by points in descending order
        db.collection("Users")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    userList.clear()
                    Log.e("Leaderboard_Docs", documents.toString())
                    for (document in documents) {
                        val username = document.getString("username") ?: "Unknown"
                        val points = document.getLong("points") ?: 0

                        // Add each user to the list
                        val user = User(username, points)
                        userList.add(user)
                    }
                    leaderboardAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "No users found in leaderboard", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("LeaderboardActivity", "Error fetching leaderboard", e)
                Toast.makeText(this, "Error fetching leaderboard", Toast.LENGTH_SHORT).show()
            }
            Log.e("Learderboard","CP2")
    }
}
