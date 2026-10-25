package com.example.db

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreSyncService private constructor(context: Context) {

  private val firestore = FirebaseFirestore.getInstance()
  private val auth = FirebaseAuth.getInstance()
  private val repository = AceRepository.getInstance(context)
  private val scope = CoroutineScope(Dispatchers.IO)

  private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
  val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

  sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val errorMsg: String) : SyncState()
  }

  fun syncUserDataToCloud() {
    val user = auth.currentUser
    if (user == null) {
      _syncState.value = SyncState.Error("User not authenticated")
      return
    }

    scope.launch {
      _syncState.value = SyncState.Syncing
      try {
        val userId = user.uid

        // 1. Sync User Profile stats
        val profile = repository.getUserProfileDirect()
        val profileData = mapOf(
          "displayName" to (user.displayName ?: profile?.name ?: "Athlete"),
          "email" to (user.email ?: ""),
          "ranking" to (profile?.ranking ?: "Unranked"),
          "skillLevel" to (profile?.skillLevel ?: "Beginner"),
          "totalSmashes" to (profile?.totalSmashes ?: 0),
          "totalPracticeMinutes" to (profile?.totalPracticeMinutes ?: 0),
          "averageFormScore" to (profile?.averageFormScore ?: 0),
          "preferredHand" to (profile?.preferredHand ?: "Right Handed"),
          "lastSynced" to System.currentTimeMillis()
        )
        firestore.collection("users").document(userId)
          .set(profileData, SetOptions.merge())
          .await()

        // 2. Sync Practice Sessions
        val sessions = repository.getAllSessionsDirect()
        for (session in sessions.take(20)) {
          val sessionMap = mapOf(
            "id" to session.id,
            "dateMs" to session.dateMs,
            "strokeType" to session.strokeType,
            "totalReps" to session.totalReps,
            "averageFormScore" to session.averageFormScore,
            "maxSmashSpeedKmH" to session.maxSmashSpeedKmH,
            "avgZenithReachMeters" to session.avgZenithReachMeters,
            "durationSeconds" to session.durationSeconds,
            "clipsExtractedCount" to session.clipsExtractedCount,
            "coachSummary" to session.coachSummary
          )
          firestore.collection("users").document(userId)
            .collection("practice_sessions").document(session.id.toString())
            .set(sessionMap, SetOptions.merge())
            .await()
        }

        _syncState.value = SyncState.Success("Cloud backup complete (${sessions.size} sessions synced)")
      } catch (e: Exception) {
        Log.e("FirestoreSyncService", "Error syncing to Firestore: ${e.message}", e)
        _syncState.value = SyncState.Error("Sync error: ${e.message}")
      }
    }
  }

  companion object {
    @Volatile
    private var instance: FirestoreSyncService? = null

    fun getInstance(context: Context): FirestoreSyncService {
      return instance ?: synchronized(this) {
        instance ?: FirestoreSyncService(context.applicationContext).also { instance = it }
      }
    }
  }
}
