package com.example.controltiempo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.controltiempo.data.AppDatabase
import com.example.controltiempo.data.Block
import com.example.controltiempo.data.BlockType
import com.example.controltiempo.data.Routine
import com.example.controltiempo.databinding.ItemBlockEditBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class EditRoutineActivity : AppCompatActivity() {

    private val dao by lazy { AppDatabase.get(this).routineDao() }
    private val blocks = mutableListOf<Block>()
    private lateinit var adapter: BlockEditAdapter
    private lateinit var nameInput: TextInputEditText
    private var routineId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_routine)

        routineId = intent.getLongExtra("routineId", 0L)
        nameInput = findViewById(R.id.routineNameInput)

        adapter = BlockEditAdapter(blocks)
        findViewById<RecyclerView>(R.id.blocksRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@EditRoutineActivity)
            adapter = this@EditRoutineActivity.adapter
        }

        findViewById<FloatingActionButton>(R.id.addTaskFab).setOnClickListener {
            blocks.add(Block(type = BlockType.TASK, durationSeconds = 60, label = getString(R.string.task)))
            adapter.notifyItemInserted(blocks.size - 1)
        }
        findViewById<FloatingActionButton>(R.id.addPauseFab).setOnClickListener {
            blocks.add(Block(type = BlockType.PAUSE, durationSeconds = 30, label = getString(R.string.pause_block)))
            adapter.notifyItemInserted(blocks.size - 1)
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.saveButton).setOnClickListener {
            saveRoutine()
        }

        if (routineId != 0L) {
            lifecycleScope.launch {
                val withBlocks = dao.getRoutineWithBlocks(routineId) ?: return@launch
                nameInput.setText(withBlocks.routine.name)
                blocks.clear()
                blocks.addAll(withBlocks.blocks.sortedBy { it.orderIndex })
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun saveRoutine() {
        val name = nameInput.text?.toString()?.trim().orEmpty()
        if (name.isEmpty()) {
            nameInput.error = getString(R.string.name_required)
            return
        }
        if (blocks.isEmpty()) {
            Toast.makeText(this, R.string.at_least_one_block, Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val id = if (routineId != 0L) {
                dao.updateRoutine(Routine(id = routineId, name = name))
                routineId
            } else {
                dao.insertRoutine(Routine(name = name))
            }
            dao.replaceRoutineBlocks(id, blocks.map { it.copy(routineId = id) })
            finish()
        }
    }
}

class BlockEditAdapter(private val blocks: MutableList<Block>) :
    RecyclerView.Adapter<BlockEditAdapter.BlockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder =
        BlockViewHolder(
            ItemBlockEditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        val block = blocks[position]
        val binding = holder.binding
        val context = binding.root.context
        binding.blockTypeText.text = if (block.type == BlockType.TASK) {
            context.getString(R.string.task)
        } else {
            context.getString(R.string.pause_block)
        }
        binding.durationInput.setText(block.durationSeconds.toString())
        binding.moveUpButton.isEnabled = position > 0
        binding.moveDownButton.isEnabled = position < blocks.size - 1
        binding.moveUpButton.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos > 0) {
                blocks.add(pos - 1, blocks.removeAt(pos))
                notifyItemMoved(pos, pos - 1)
            }
        }
        binding.moveDownButton.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION && pos < blocks.size - 1) {
                blocks.add(pos + 1, blocks.removeAt(pos))
                notifyItemMoved(pos, pos + 1)
            }
        }
        binding.deleteButton.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                blocks.removeAt(pos)
                notifyItemRemoved(pos)
                notifyItemRangeChanged(0, blocks.size)
            }
        }
        binding.durationInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnFocusChangeListener
                val text = binding.durationInput.text?.toString().orEmpty()
                val seconds = text.toIntOrNull() ?: block.durationSeconds
                val safe = seconds.coerceIn(1, 3600)
                blocks[pos] = blocks[pos].copy(durationSeconds = safe)
                binding.durationInput.setText(safe.toString())
            }
        }
    }

    override fun getItemCount(): Int = blocks.size

    class BlockViewHolder(val binding: ItemBlockEditBinding) :
        RecyclerView.ViewHolder(binding.root)
}
