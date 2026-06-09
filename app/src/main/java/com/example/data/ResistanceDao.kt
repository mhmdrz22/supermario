package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ResistanceDao {

    @Query("SELECT * FROM resistance_tasks ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<ResistanceTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: ResistanceTask)

    @Update
    suspend fun updateTask(task: ResistanceTask)

    @Query("DELETE FROM resistance_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    @Query("SELECT * FROM transmission_logs ORDER BY timestamp DESC")
    fun getAllTransmissions(): Flow<List<TransmissionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransmission(transmission: TransmissionLog)

    @Update
    suspend fun updateTransmission(transmission: TransmissionLog)

    @Query("DELETE FROM transmission_logs WHERE id = :id")
    suspend fun deleteTransmissionById(id: Int)
    
    @Query("DELETE FROM transmission_logs")
    suspend fun clearAllTransmissions()
}
