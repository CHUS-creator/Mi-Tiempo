package com.example.controltiempo.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routines",
)
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

@Entity(
    tableName = "blocks",
    foreignKeys = [
        ForeignKey(
            entity = Routine::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("routineId")],
)
data class Block(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val orderIndex: Int,
    val type: BlockType,
    val durationSeconds: Int,
    val label: String,
)

enum class BlockType {
    TASK,
    PAUSE,
}
