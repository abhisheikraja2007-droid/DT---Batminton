package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeSessionDao {
  @Query("SELECT * FROM practice_sessions ORDER BY dateMs DESC")
  fun getAllSessions(): Flow<List<PracticeSessionEntity>>

  @Query("SELECT * FROM practice_sessions ORDER BY dateMs DESC")
  suspend fun getAllSessionsDirect(): List<PracticeSessionEntity>

  @Query("SELECT * FROM practice_sessions WHERE id = :id LIMIT 1")
  suspend fun getSessionById(id: Long): PracticeSessionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: PracticeSessionEntity): Long

  @Query("DELETE FROM practice_sessions WHERE id = :id")
  suspend fun deleteSessionById(id: Long)

  @Query("DELETE FROM practice_sessions")
  suspend fun deleteAllSessions()
}

@Dao
interface DrillDao {
  @Query("SELECT * FROM drills ORDER BY id ASC")
  fun getAllDrills(): Flow<List<DrillEntity>>

  @Query("SELECT * FROM drills WHERE category = :category ORDER BY id ASC")
  fun getDrillsByCategory(category: String): Flow<List<DrillEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDrill(drill: DrillEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDrills(drills: List<DrillEntity>)

  @Update
  suspend fun updateDrill(drill: DrillEntity)
}

@Dao
interface CoachChatDao {
  @Query("SELECT * FROM coach_chat_messages ORDER BY timestampMs ASC")
  fun getAllChatMessages(): Flow<List<CoachChatMessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: CoachChatMessageEntity): Long

  @Query("DELETE FROM coach_chat_messages")
  suspend fun clearChatHistory()
}

@Dao
interface UserProfileDao {
  @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
  fun getUserProfile(): Flow<UserProfileEntity?>

  @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
  suspend fun getUserProfileDirect(): UserProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}
