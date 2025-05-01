package com.example.tiltmazegame

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.ActivityInfo

class GameActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var mazeView: MazeView

    private lateinit var timerTextView: TextView
    private val timerHandler = Handler(Looper.getMainLooper())
    private var startTime: Long = 0
    private var isTimerRunning = false

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isTimerRunning) {
                val elapsed = System.currentTimeMillis() - startTime
                val seconds = elapsed / 1000
                val ms = elapsed % 1000
                timerTextView.text = "Time: $seconds.${ms.toString().padStart(3, '0')}s"
                timerHandler.postDelayed(this, 100)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //AI Prompt - "How can I get the screen to lock while playing the game?"
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContentView(R.layout.activity_game)

        mazeView = findViewById(R.id.mazeView)
        timerTextView = findViewById(R.id.timerTextView)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Start timer
        startTime = System.currentTimeMillis()
        isTimerRunning = true
        timerHandler.post(timerRunnable)

        //AI Prompt - "Can you help me send the time to the win screen once the player reaches the goal?"
        // Set maze win listener
        mazeView.listener = object : MazeView.MazeListener {
            override fun onGameWin(elapsedMillis: Long) {
                isTimerRunning = false

                // Send the time to WinActivity
                val seconds = elapsedMillis / 1000
                val ms = elapsedMillis % 1000
                val winTime = "$seconds.${ms.toString().padStart(3, '0')}s"

                // Navigate to WinActivity
                val intent = Intent(this@GameActivity, WinActivity::class.java)
                intent.putExtra("TIME_TAKEN", winTime)
                startActivity(intent)
                finish()
            }
        }

        val backButton: Button = findViewById(R.id.backToMenuButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val tiltX = -it.values[0]
            val tiltY = it.values[1]
            mazeView.onTilt(tiltX, tiltY)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed
    }
}

