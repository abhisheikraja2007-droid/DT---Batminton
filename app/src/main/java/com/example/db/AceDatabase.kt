package com.example.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    PracticeSessionEntity::class,
    DrillEntity::class,
    CoachChatMessageEntity::class,
    UserProfileEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class AceDatabase : RoomDatabase() {

  abstract fun practiceSessionDao(): PracticeSessionDao
  abstract fun drillDao(): DrillDao
  abstract fun coachChatDao(): CoachChatDao
  abstract fun userProfileDao(): UserProfileDao

  companion object {
    @Volatile
    private var INSTANCE: AceDatabase? = null

    fun getDatabase(context: Context): AceDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AceDatabase::class.java,
          "ace_badminton_db"
        )
          .fallbackToDestructiveMigration()
          .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
              super.onCreate(db)
              // Seed initial database content on first run
              CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                seedDatabase(database)
              }
            }
          })
          .build()
        INSTANCE = instance
        instance
      }
    }

    private suspend fun seedDatabase(db: AceDatabase) {
      // Clear any practice sessions
      db.practiceSessionDao().deleteAllSessions()

      // Initialize default user profile with 0 stats
      db.userProfileDao().insertOrUpdateProfile(UserProfileEntity())

      // Initialize Coach Maya welcome greeting
      db.coachChatDao().insertMessage(
        CoachChatMessageEntity(
          sender = "Coach Maya",
          messageText = "Welcome to Ace AI! I'm Coach Maya, your biomechanical swing coach. Start a camera drill or ask me any technique question!",
          isAi = true
        )
      )
    }
  }
}
