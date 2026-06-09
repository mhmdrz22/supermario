package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "mario_high_scores")
data class MarioHighScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerName: String,
    val score: Int,
    val coins: Int,
    val levelName: String, // e.g. "World 1-1", "World 1-2", "World 1-3"
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface MarioDao {
    @Query("SELECT * FROM mario_high_scores ORDER BY score DESC LIMIT 10")
    fun getTopTenScores(): Flow<List<MarioHighScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: MarioHighScore)

    @Query("SELECT MAX(score) FROM mario_high_scores")
    suspend fun getMaxScore(): Int?

    @Query("DELETE FROM mario_high_scores")
    suspend fun clearLeaderboard()
}
