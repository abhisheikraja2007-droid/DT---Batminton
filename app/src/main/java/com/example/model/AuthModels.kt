package com.example.model

data class AuthUser(
  val uid: String,
  val email: String?,
  val displayName: String?,
  val photoUrl: String?,
  val isAnonymous: Boolean = false
)

sealed class AuthState {
  object Idle : AuthState()
  object Loading : AuthState()
  data class Authenticated(val user: AuthUser) : AuthState()
  data class Unauthenticated(val message: String? = null) : AuthState()
  data class Error(val message: String) : AuthState()
}

data class CloudSyncStatus(
  val isSyncing: Boolean = false,
  val lastSyncedTime: String? = "Just now",
  val totalSessionsSynced: Int = 12,
  val isCloudConnected: Boolean = true,
  val pendingChangesCount: Int = 0
)
