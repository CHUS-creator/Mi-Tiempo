package com.example.controltiempo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.controltiempo.data.AppDatabase
import com.example.controltiempo.data.Routine
import com.example.controltiempo.databinding.ItemRoutineBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: RoutineAdapter
    private val dao by lazy { AppDatabase.get(this).routineDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_list)

        adapter = RoutineAdapter(
            onClick = { routine ->
                startActivity(
                    Intent(this, RunActivity::class.java).putExtra("routineId", routine.id)
                )
            },
            onLongClick = { routine -> confirmDelete(routine) },
        )
        findViewById<RecyclerView>(R.id.routinesRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
        findViewById<FloatingActionButton>(R.id.addRoutineFab).setOnClickListener {
            startActivity(Intent(this, EditRoutineActivity::class.java))
        }

        lifecycleScope.launch {
            dao.observeRoutines().collectLatest { adapter.submitList(it) }
        }
    }

    private fun confirmDelete(routine: Routine) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_routine_confirm, routine.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch { dao.deleteRoutine(routine) }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}

class RoutineAdapter(
    private val onClick: (Routine) -> Unit,
    private val onLongClick: (Routine) -> Unit,
) : ListAdapter<Routine, RoutineAdapter.RoutineViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder =
        RoutineViewHolder(
            ItemRoutineBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = getItem(position)
        holder.binding.routineNameText.text = routine.name
        holder.binding.root.setOnClickListener { onClick(routine) }
        holder.binding.root.setOnLongClickListener {
            onLongClick(routine)
            true
        }
    }

    class RoutineViewHolder(val binding: ItemRoutineBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Routine>() {
            override fun areItemsTheSame(old: Routine, new: Routine) = old.id == new.id
            override fun areContentsTheSame(old: Routine, new: Routine) = old == new
        }
    }
}
