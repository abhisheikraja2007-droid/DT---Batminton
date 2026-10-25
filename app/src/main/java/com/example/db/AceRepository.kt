package com.example.db

import android.content.Context
import kotlinx.coroutines.flow.Flow

class AceRepository(context: Context) {
  private val db = AceDatabase.getDatabase(context)
  private val sessionDao = db.practiceSessionDao()
  private val drillDao = db.drillDao()
  private val chatDao = db.coachChatDao()
  private val profileDao = db.userProfileDao()

  val allSessions: Flow<List<PracticeSessionEntity>> = sessionDao.getAllSessions()
  val allDrills: Flow<List<DrillEntity>> = drillDao.getAllDrills()
  val allChatMessages: Flow<List<CoachChatMessageEntity>> = chatDao.getAllChatMessages()
  val userProfile: Flow<UserProfileEntity?> = profileDao.getUserProfile()

  suspend fun savePracticeSession(session: PracticeSessionEntity): Long {
    val id = sessionDao.insertSession(session)
    recalculateProfileStats()
    return id
  }

  suspend fun deletePracticeSession(id: Long) {
    sessionDao.deleteSessionById(id)
    recalculateProfileStats()
  }

  suspend fun clearAllPracticeSessions() {
    sessionDao.deleteAllSessions()
    profileDao.insertOrUpdateProfile(UserProfileEntity())
  }

  private suspend fun recalculateProfileStats() {
    val currentProfile = profileDao.getUserProfileDirect() ?: UserProfileEntity()
    val allSessions = sessionDao.getAllSessionsDirect()
    val totalSmashes = allSessions.sumOf { it.totalReps }
    val totalMinutes = allSessions.sumOf { it.durationSeconds } / 60
    val avgFormScore = if (allSessions.isNotEmpty()) allSessions.map { it.averageFormScore }.average().toInt() else 0
    profileDao.insertOrUpdateProfile(
      currentProfile.copy(
        totalSmashes = totalSmashes,
        totalPracticeMinutes = totalMinutes,
        averageFormScore = avgFormScore
      )
    )
  }

  suspend fun getAllSessionsDirect(): List<PracticeSessionEntity> {
    return sessionDao.getAllSessionsDirect()
  }

  suspend fun getUserProfileDirect(): UserProfileEntity? {
    return profileDao.getUserProfileDirect()
  }

  suspend fun getSessionById(id: Long): PracticeSessionEntity? {
    return sessionDao.getSessionById(id)
  }

  suspend fun addDrill(drill: DrillEntity) {
    drillDao.insertDrill(drill)
  }

  suspend fun updateDrill(drill: DrillEntity) {
    drillDao.updateDrill(drill)
  }

  suspend fun sendChatMessage(messageText: String, isAi: Boolean, sender: String): Long {
    return chatDao.insertMessage(
      CoachChatMessageEntity(
        sender = sender,
        messageText = messageText,
        isAi = isAi
      )
    )
  }

  suspend fun updateUserProfile(profile: UserProfileEntity) {
    profileDao.insertOrUpdateProfile(profile)
  }

  companion object {
    @Volatile
    private var INSTANCE: AceRepository? = null

    fun getInstance(context: Context): AceRepository {
      return INSTANCE ?: synchronized(this) {
        val instance = AceRepository(context.applicationContext)
        INSTANCE = instance
        instance
      }
    }
  }
}
