package com.example.tiltmazegame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Reference to buttons
        val playButton: Button = findViewById(R.id.playButton)
        val aboutButton: Button = findViewById(R.id.aboutButton)
        val exitButton: Button = findViewById(R.id.exitButton)

        // Handle the Play button click
        playButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent) // Start the game activity
        }

        // Handle the About button click
        aboutButton.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent) // Start the about activity (you will need to create this activity later)
        }

        // Handle the Exit button click
        exitButton.setOnClickListener {
            finish() // Close the app
        }
    }
}
