package com.example.controltiempo

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.controltiempo.data.AppDatabase
import com.example.controltiempo.data.Block
import com.example.controltiempo.data.BlockType
import kotlinx.coroutines.launch

class RunActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var blockText: TextView
    private lateinit var nextText: TextView
    private lateinit var startPauseButton: Button
    private lateinit var resetButton: Button

    private var blocks: List<Block> = emptyList()
    private var currentBlockIndex = 0
    private var countdownTimer: CountDownTimer? = null
    private var remainingMillis: Long = 0
    private var timerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run)

        timerText = findViewById(R.id.timerText)
        blockText = findViewById(R.id.blockText)
        nextText = findViewById(R.id.nextText)
        startPauseButton = findViewById(R.id.startPauseButton)
        resetButton = findViewById(R.id.resetButton)

        startPauseButton.setOnClickListener {
            if (timerRunning) pauseTimer() else startOrResumeTimer()
        }
        resetButton.setOnClickListener { resetRun() }

        val routineId = intent.getLongExtra("routineId", 0L)
        lifecycleScope.launch {
            val withBlocks = AppDatabase.get(this@RunActivity)
                .routineDao()
                .getRoutineWithBlocks(routineId)
            blocks = withBlocks?.blocks?.sortedBy { it.orderIndex }.orEmpty()
            if (blocks.isEmpty()) {
                finish()
                return@launch
            }
            loadBlock(0)
        }
    }

    private fun loadBlock(index: Int) {
        currentBlockIndex = index
        val block = blocks[index]
        remainingMillis = block.durationSeconds * 1000L
        blockText.text = if (block.type == BlockType.TASK) {
            getString(R.string.task_block_label, block.label)
        } else {
            getString(R.string.pause_block_label, block.label)
        }
        val next = blocks.getOrNull(index + 1)
        nextText.text = if (next != null) {
            getString(
                R.string.next_block_label,
                if (next.type == BlockType.TASK) getString(R.string.task) else getString(R.string.pause_block),
                next.durationSeconds,
            )
        } else {
            getString(R.string.last_block)
        }
        startPauseButton.text = getString(R.string.start)
        updateDisplay()
    }

    private fun startOrResumeTimer() {
        val block = blocks[currentBlockIndex]
        countdownTimer = object : CountDownTimer(remainingMillis, 100L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                updateDisplay()
            }

            override fun onFinish() {
                advance()
            }
        }.start()
        timerRunning = true
        startPauseButton.text = getString(R.string.pause)
    }

    private fun advance() {
        countdownTimer?.cancel()
        countdownTimer = null
        timerRunning = false
        if (currentBlockIndex + 1 < blocks.size) {
            loadBlock(currentBlockIndex + 1)
            startOrResumeTimer()
        } else {
            remainingMillis = 0
            blockText.text = getString(R.string.routine_finished)
            nextText.text = ""
            startPauseButton.text = getString(R.string.start)
            updateDisplay()
        }
    }

    private fun pauseTimer() {
        countdownTimer?.cancel()
        countdownTimer = null
        timerRunning = false
        startPauseButton.text = getString(R.string.resume)
    }

    private fun resetRun() {
        countdownTimer?.cancel()
        countdownTimer = null
        timerRunning = false
        if (blocks.isNotEmpty()) loadBlock(0)
    }

    private fun updateDisplay() {
        val totalSeconds = (remainingMillis + 999) / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        timerText.text = getString(R.string.time_format, minutes, seconds)
    }

    override fun onDestroy() {
        countdownTimer?.cancel()
        super.onDestroy()
    }
}
