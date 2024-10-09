package com.example.snapquest

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat

class Instructions : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instructions)

        val showInstructionsButton: Button = findViewById(R.id.show_instructions_button)

        showInstructionsButton.setOnClickListener {
            showInstructionsPopup()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showInstructionsPopup() {

        val titleView = TextView(this).apply {
            text = "How to Play SnapQuest"
            textSize = 20f
            setTextColor(ResourcesCompat.getColor(resources, R.color.black, null))
            typeface = ResourcesCompat.getFont(context, R.font.protest_guerrilla) // Load font from res/font
            gravity = Gravity.CENTER
            setPadding(20, 20, 20, 10)
        }

        val messageView = TextView(this).apply {
            text = """
                SnapQuest is an exciting game that challenges your creativity and observation skills.
                
                • Each day, you'll receive a unique task at a random time.
                • When the task appears, you'll have 5 minutes to complete it.
                • The task might be something like: "Take a photo of the object specified."
                • Find the object, capture a photo, and upload it for verification.
                • The faster you complete the task, the more points you earn!
                • If you fail to complete the task in 5 minutes, points will be deducted.
                • Check out the daily leaderboard to see how you rank against others!
                
                Get ready for a thrilling adventure every day with SnapQuest!
            """.trimIndent()
            textSize = 16f
            setTextColor(ResourcesCompat.getColor(resources, R.color.black, null))
            typeface = ResourcesCompat.getFont(context, R.font.protest_guerrilla)
            setPadding(20, 20, 20, 20)
        }


        val builder = AlertDialog.Builder(this)
        builder.setCustomTitle(titleView)
        builder.setView(messageView)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()


        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#8b9c9e")))


        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ResourcesCompat.getColor(resources, R.color.primary, null))
    }
}
