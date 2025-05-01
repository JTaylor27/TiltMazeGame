package com.example.tiltmazegame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WinActivity : AppCompatActivity() {

    private lateinit var timeTakenTextView: TextView
    private lateinit var playAgainButton: Button
    private lateinit var mainMenuButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_win)

        // Get the time from the intent
        val timeTaken = intent.getStringExtra("TIME_TAKEN")

        // Find the TextView and set the text to the time received
        timeTakenTextView = findViewById(R.id.timeTakenTextView)
        playAgainButton = findViewById(R.id.playAgainButton)
        mainMenuButton = findViewById(R.id.mainMenuButton)

        // Display the time on the screen
        timeTakenTextView.text = "You Win!\nTime: $timeTaken"

        // Set up button listeners
        playAgainButton.setOnClickListener {
            // Start the game again (go back to the GameActivity)
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            finish()  // Finish WinActivity to prevent going back to it
        }

        mainMenuButton.setOnClickListener {
            // Go back to the Main Menu
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            finish()  // Finish WinActivity to prevent going back to it
        }
    }
}
