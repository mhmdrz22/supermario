package com.example.data

import kotlinx.coroutines.flow.Flow

class ResistanceRepository(private val dao: ResistanceDao) {

    val allTasks: Flow<List<ResistanceTask>> = dao.getAllTasks()
    val allTransmissions: Flow<List<TransmissionLog>> = dao.getAllTransmissions()

    suspend fun insertTask(task: ResistanceTask) {
        dao.insertTask(task)
    }

    suspend fun updateTask(task: ResistanceTask) {
        dao.updateTask(task)
    }

    suspend fun deleteTask(id: Int) {
        dao.deleteTaskById(id)
    }

    suspend fun insertTransmission(transmission: TransmissionLog) {
        dao.insertTransmission(transmission)
    }

    suspend fun updateTransmission(transmission: TransmissionLog) {
        dao.updateTransmission(transmission)
    }

    suspend fun deleteTransmission(id: Int) {
        dao.deleteTransmissionById(id)
    }

    suspend fun clearAllTransmissions() {
        dao.clearAllTransmissions()
    }
}
