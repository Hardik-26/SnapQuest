package com.example.snapquest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(private val userList: List<User>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    // ViewHolder class to hold the views for each item
    class LeaderboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usernameTextView: TextView = view.findViewById(R.id.name)
        val pointsTextView: TextView = view.findViewById(R.id.points)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        // Inflate the item layout for each user row
        val view = LayoutInflater.from(parent.context).inflate(R.layout.leaderboard_item, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        // Bind the data for each user
        val user = userList[position]
        holder.usernameTextView.text = user.username
        holder.pointsTextView.text = user.points.toString()
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}
