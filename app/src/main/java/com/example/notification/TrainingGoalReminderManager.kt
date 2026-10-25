package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

data class FitnessGoal(
  val id: String,
  val title: String,
  val targetValue: String,
  val currentValue: String,
  val unit: String,
  val progress: Float, // 0.0f - 1.0f
  val category: String,
  val iconSymbol: String,
  val motivationalQuote: String
)

data class ReminderSettings(
  val isEnabled: Boolean = true,
  val hour: Int = 18,
  val minute: Int = 30,
  val selectedGoalId: String = "goal_smash_speed",
  val daysStreak: Int = 3,
  val soundEnabled: Boolean = true,
  val vibrateEnabled: Boolean = true
)

class TrainingGoalReminderManager private constructor(private val context: Context) {

  private val prefs: SharedPreferences = context.getSharedPreferences("aceai_reminders", Context.MODE_PRIVATE)

  private val _goals = MutableStateFlow<List<FitnessGoal>>(defaultGoals)
  val goals: StateFlow<List<FitnessGoal>> = _goals.asStateFlow()

  private val _settings = MutableStateFlow(loadSettings())
  val settings: StateFlow<ReminderSettings> = _settings.asStateFlow()

  init {
    createNotificationChannel(context)
  }

  companion object {
    const val CHANNEL_ID = "aceai_daily_drills"
    const val CHANNEL_NAME = "AceAI Daily Training Drills"
    const val NOTIFICATION_ID = 20261
    const val TEST_NOTIFICATION_ID = 20262

    val defaultGoals = listOf(
      FitnessGoal(
        id = "goal_smash_speed",
        title = "245 km/h Smash Exit Velocity",
        targetValue = "245",
        currentValue = "0",
        unit = "km/h",
        progress = 0f,
        category = "Speed & Power",
        iconSymbol = "⚡",
        motivationalQuote = "Start a camera tracking session to register your first smash speed!"
      ),
      FitnessGoal(
        id = "goal_daily_drills",
        title = "3 Structured Drills Per Day",
        targetValue = "3",
        currentValue = "0",
        unit = "drills",
        progress = 0f,
        category = "Consistency",
        iconSymbol = "🏸",
        motivationalQuote = "Complete your first drill today to kick off your active streak!"
      ),
      FitnessGoal(
        id = "goal_daily_reps",
        title = "50 Computer-Vision Reps",
        targetValue = "50",
        currentValue = "0",
        unit = "reps",
        progress = 0f,
        category = "Volume",
        iconSymbol = "🎯",
        motivationalQuote = "Computer-vision stroke tracking guarantees clean kinetic memory."
      ),
      FitnessGoal(
        id = "goal_zenith_reach",
        title = "2.90m Apex Contact Height",
        targetValue = "2.90",
        currentValue = "2.88",
        unit = "meters",
        progress = 0.88f,
        category = "Biomechanics",
        iconSymbol = "🦅",
        motivationalQuote = "Extend through your non-dominant shoulder to gain +2cm of steep smash angle."
      ),
      FitnessGoal(
        id = "goal_form_mastery",
        title = "88%+ Biomechanical Form Score",
        targetValue = "88",
        currentValue = "84",
        unit = "%",
        progress = 0.84f,
        category = "Technique",
        iconSymbol = "💎",
        motivationalQuote = "Keep your hitting elbow cocked above 140° before torso uncoil for peak efficiency."
      )
    )

    @Volatile
    private var INSTANCE: TrainingGoalReminderManager? = null

    fun getInstance(context: Context): TrainingGoalReminderManager {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: TrainingGoalReminderManager(context.applicationContext).also { INSTANCE = it }
      }
    }

    fun createNotificationChannel(context: Context) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(
          CHANNEL_ID,
          CHANNEL_NAME,
          importance
        ).apply {
          description = "Daily badminton training drill reminders customized to your active fitness goals."
          enableVibration(true)
          setShowBadge(true)
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
      }
    }
  }

  private fun loadSettings(): ReminderSettings {
    return ReminderSettings(
      isEnabled = prefs.getBoolean("reminders_enabled", true),
      hour = prefs.getInt("reminder_hour", 18),
      minute = prefs.getInt("reminder_minute", 30),
      selectedGoalId = prefs.getString("selected_goal_id", "goal_smash_speed") ?: "goal_smash_speed",
      daysStreak = prefs.getInt("days_streak", 3),
      soundEnabled = prefs.getBoolean("sound_enabled", true),
      vibrateEnabled = prefs.getBoolean("vibrate_enabled", true)
    )
  }

  fun updateSettings(newSettings: ReminderSettings) {
    _settings.value = newSettings
    prefs.edit()
      .putBoolean("reminders_enabled", newSettings.isEnabled)
      .putInt("reminder_hour", newSettings.hour)
      .putInt("reminder_minute", newSettings.minute)
      .putString("selected_goal_id", newSettings.selectedGoalId)
      .putInt("days_streak", newSettings.daysStreak)
      .putBoolean("sound_enabled", newSettings.soundEnabled)
      .putBoolean("vibrate_enabled", newSettings.vibrateEnabled)
      .apply()

    if (newSettings.isEnabled) {
      scheduleAlarm(context, newSettings)
    } else {
      cancelAlarm(context)
    }
  }

  fun selectActiveGoal(goalId: String) {
    val updated = _settings.value.copy(selectedGoalId = goalId)
    updateSettings(updated)
  }

  fun scheduleDailyReminder(hour: Int, minute: Int, goalId: String) {
    val updated = _settings.value.copy(
      isEnabled = true,
      hour = hour,
      minute = minute,
      selectedGoalId = goalId
    )
    updateSettings(updated)
  }

  private fun scheduleAlarm(context: Context, settings: ReminderSettings) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val intent = Intent(context, TrainingReminderReceiver::class.java).apply {
        action = "com.example.action.DAILY_DRILL_REMINDER"
        putExtra("goal_id", settings.selectedGoalId)
      }
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        1001,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, settings.hour)
        set(Calendar.MINUTE, settings.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
          add(Calendar.DAY_OF_YEAR, 1)
        }
      }

      alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
      )
      Log.d("GoalReminderManager", "Daily alarm scheduled for ${settings.hour}:${settings.minute}")
    } catch (e: Exception) {
      Log.w("GoalReminderManager", "Error scheduling alarm: ${e.message}")
    }
  }

  private fun cancelAlarm(context: Context) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val intent = Intent(context, TrainingReminderReceiver::class.java)
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        1001,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
      )
      if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
      }
    } catch (e: Exception) {
      Log.w("GoalReminderManager", "Error cancelling alarm: ${e.message}")
    }
  }

  fun triggerTestNotification(goalId: String? = null) {
    val targetGoalId = goalId ?: _settings.value.selectedGoalId
    val goal = _goals.value.find { it.id == targetGoalId } ?: _goals.value.first()
    sendNotification(context, goal, isTest = true)
  }

  fun sendNotification(context: Context, goal: FitnessGoal, isTest: Boolean = false) {
    createNotificationChannel(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("open_screen", "TRAIN")
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      0,
      openAppIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val streakText = "${_settings.value.daysStreak}-day streak active 🔥"
    val titleText = if (isTest) {
      "🏸 [Test Alert] Goal: ${goal.title}"
    } else {
      "🏸 Daily Training Reminder: ${goal.title}"
    }

    val bodyText = "${goal.currentValue} / ${goal.targetValue} ${goal.unit} reached. ${goal.motivationalQuote} ($streakText)"

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle(titleText)
      .setContentText(bodyText)
      .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .addAction(
        android.R.drawable.ic_media_play,
        "Start Live Camera Drill",
        pendingIntent
      )

    val id = if (isTest) TEST_NOTIFICATION_ID else NOTIFICATION_ID
    notificationManager.notify(id, builder.build())
  }
}

class TrainingReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    Log.d("TrainingReminderReceiver", "Alarm fired for daily training drills reminder")
    val goalId = intent.getStringExtra("goal_id") ?: "goal_smash_speed"
    val manager = TrainingGoalReminderManager.getInstance(context)
    val targetGoal = manager.goals.value.find { it.id == goalId } ?: TrainingGoalReminderManager.defaultGoals.first()
    manager.sendNotification(context, targetGoal, isTest = false)
  }
}
