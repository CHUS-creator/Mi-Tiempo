package com.example.controltiempo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class RoutineWithBlocks(
    val routine: Routine,
    val blocks: List<Block>,
)

@Dao
interface RoutineDao {

    @Query("SELECT * FROM routines ORDER BY id")
    fun observeRoutines(): Flow<List<Routine>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :routineId")
    suspend fun getRoutineWithBlocks(routineId: Long): RoutineWithBlocks?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Delete
    suspend fun deleteRoutine(routine: Routine)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<Block>)

    @Query("DELETE FROM blocks WHERE routineId = :routineId")
    suspend fun deleteBlocksOfRoutine(routineId: Long)

    @Transaction
    suspend fun replaceRoutineBlocks(routineId: Long, blocks: List<Block>) {
        deleteBlocksOfRoutine(routineId)
        insertBlocks(blocks.mapIndexed { index, block ->
            block.copy(routineId = routineId, orderIndex = index)
        })
    }
}
