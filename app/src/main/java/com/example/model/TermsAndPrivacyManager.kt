package com.example.model

import android.content.Context
import android.content.SharedPreferences

object TermsAndPrivacyManager {
  private const val PREF_NAME = "ace_ai_terms_privacy_prefs"
  private const val KEY_ACCEPTED = "has_accepted_terms_v1"
  private const val KEY_ACCEPTED_TIMESTAMP = "accepted_terms_timestamp"

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
  }

  fun hasAcceptedTerms(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_ACCEPTED, false)
  }

  fun setAcceptedTerms(context: Context, accepted: Boolean) {
    getPrefs(context).edit()
      .putBoolean(KEY_ACCEPTED, accepted)
      .putLong(KEY_ACCEPTED_TIMESTAMP, System.currentTimeMillis())
      .apply()
  }

  fun getAcceptedTimestamp(context: Context): Long {
    return getPrefs(context).getLong(KEY_ACCEPTED_TIMESTAMP, 0L)
  }

  val termsAndPrivacyTitle = "Terms of Service & Privacy Policy"

  val termsAndPrivacySummary = """
    Welcome to Ace AI - Biomechanical Badminton Performance Coach. Before using our application, please review and accept our Terms of Service and Privacy Policy.

    1. Camera & Biomechanical Pose Estimation
    Ace AI uses your device's camera locally to detect skeletal joint landmarks (shoulder, elbow, wrist, hip, knee) for real-time stroke angle diagnostics and smash velocity measurements. Camera video frames are processed on-device and are not sold or redistributed to third parties.

    2. Microphone & Live Voice Coaching
    Live Voice Coach features process microphone audio to provide real-time audio cues during training sessions using Gemini AI speech capabilities. Audio streams are strictly processed for coaching interaction.

    3. Firebase Cloud Firestore Data Sync
    Your training drill logs, form scores, and user preferences are saved locally in Room Database and optionally synchronized with Google Cloud Firestore when signed in with Firebase Authentication.

    4. Sports Performance & Biomechanical Disclaimer
    Ace AI provides automated motion analysis and coaching cues for athletic training purposes. It is not a substitute for medical evaluation or professional physical therapy advice. Please warm up adequately before high-intensity jump smashes and stop immediately if you experience joint pain.

    5. Privacy Rights & Data Erasure
    You retain ownership of your training records. You may delete local session history or log out of cloud synchronization at any time via your Athlete Profile settings.
  """.trimIndent()
}
