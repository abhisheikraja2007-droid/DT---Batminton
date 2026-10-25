package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_sessions")
data class PracticeSessionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val dateMs: Long = System.currentTimeMillis(),
  val strokeType: String,
  val totalReps: Int,
  val averageFormScore: Int,
  val maxSmashSpeedKmH: Int,
  val avgZenithReachMeters: Float,
  val durationSeconds: Int,
  val clipsExtractedCount: Int,
  val coachSummary: String
)

@Entity(tableName = "drills")
data class DrillEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val category: String, // Smash, Clear, Drop, Footwork
  val difficulty: String, // Beginner, Intermediate, Elite
  val durationMinutes: Int,
  val description: String,
  val targetReps: Int,
  val targetElbowAngleDeg: Float,
  val isFavorite: Boolean = false,
  val completedCount: Int = 0
)

@Entity(tableName = "coach_chat_messages")
data class CoachChatMessageEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sender: String, // User or Coach Maya
  val messageText: String,
  val timestampMs: Long = System.currentTimeMillis(),
  val isAi: Boolean
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
  @PrimaryKey val id: Long = 1,
  val name: String = "Athlete",
  val ranking: String = "Unranked",
  val skillLevel: String = "Beginner",
  val totalSmashes: Int = 0,
  val totalPracticeMinutes: Int = 0,
  val averageFormScore: Int = 0,
  val preferredHand: String = "Right Handed"
)
