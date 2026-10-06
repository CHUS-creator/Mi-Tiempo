package com.example.controltiempo

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var blockText: TextView
    private lateinit var startPauseButton: Button
    private lateinit var resetButton: Button

    private var countdownTimer: CountDownTimer? = null
    private var remainingMillis: Long = BLOCK_DURATION_MILLIS
    private var timerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText = findViewById(R.id.timerText)
        blockText = findViewById(R.id.blockText)
        startPauseButton = findViewById(R.id.startPauseButton)
        resetButton = findViewById(R.id.resetButton)

        startPauseButton.setOnClickListener {
            if (timerRunning) pauseTimer() else startOrResumeTimer()
        }
        resetButton.setOnClickListener { resetTimer() }

        updateDisplay()
    }

    private fun startOrResumeTimer() {
        countdownTimer = object : CountDownTimer(remainingMillis, 100L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                updateDisplay()
            }

            override fun onFinish() {
                remainingMillis = 0
                timerRunning = false
                startPauseButton.text = getString(R.string.start)
                updateDisplay()
            }
        }.start()
        timerRunning = true
        startPauseButton.text = getString(R.string.pause)
    }

    private fun pauseTimer() {
        countdownTimer?.cancel()
        countdownTimer = null
        timerRunning = false
        startPauseButton.text = getString(R.string.resume)
    }

    private fun resetTimer() {
        countdownTimer?.cancel()
        countdownTimer = null
        remainingMillis = BLOCK_DURATION_MILLIS
        timerRunning = false
        startPauseButton.text = getString(R.string.start)
        updateDisplay()
    }

    private fun updateDisplay() {
        val totalSeconds = (remainingMillis + 999) / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        timerText.text = getString(R.string.time_format, minutes, seconds)
    }

    companion object {
        private const val BLOCK_DURATION_MILLIS = 60_000L
    }
}
