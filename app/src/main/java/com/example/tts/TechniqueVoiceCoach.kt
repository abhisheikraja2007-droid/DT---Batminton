package com.example.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

enum class VoiceCoachStyle(val displayName: String, val description: String) {
  TACTICAL("Tactical Biomechanics", "Detailed angle and kinetic uncoil cues"),
  ENERGETIC("Energetic Trainer", "High-tempo encouragement and velocity callouts"),
  CONCISE("Concise Cues", "Short 2-3 word imperative directions")
}

/**
 * TechniqueVoiceCoach manages the Android Text-to-Speech engine for
 * real-time verbal feedback on swing technique during live camera analysis.
 */
class TechniqueVoiceCoach private constructor(private val context: Context) : TextToSpeech.OnInitListener {

  private val scope = CoroutineScope(Dispatchers.Main)
  private var textToSpeech: TextToSpeech? = null
  private var isInitialized = false

  private val _isMuted = MutableStateFlow(false)
  val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _lastSpokenCue = MutableStateFlow("Voice Coach Ready")
  val lastSpokenCue: StateFlow<String> = _lastSpokenCue.asStateFlow()

  private val _coachStyle = MutableStateFlow(VoiceCoachStyle.TACTICAL)
  val coachStyle: StateFlow<VoiceCoachStyle> = _coachStyle.asStateFlow()

  private var lastSpokenTimestamp = 0L
  private var lastSpokenText = ""

  init {
    try {
      textToSpeech = TextToSpeech(context.applicationContext, this)
    } catch (e: Exception) {
      Log.w("TechniqueVoiceCoach", "TTS init exception: ${e.message}")
    }
  }

  companion object {
    @Volatile
    private var INSTANCE: TechniqueVoiceCoach? = null

    fun getInstance(context: Context): TechniqueVoiceCoach {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: TechniqueVoiceCoach(context.applicationContext).also { INSTANCE = it }
      }
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      val tts = textToSpeech ?: return
      val result = tts.setLanguage(Locale.US)
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        tts.setLanguage(Locale.getDefault())
      }
      tts.setPitch(1.05f)
      tts.setSpeechRate(1.15f) // Snappy athletic tempo for live play

      tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
          _isSpeaking.value = true
        }

        override fun onDone(utteranceId: String?) {
          _isSpeaking.value = false
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
          _isSpeaking.value = false
        }
      })

      isInitialized = true
      Log.d("TechniqueVoiceCoach", "TTS initialized successfully")
    } else {
      Log.w("TechniqueVoiceCoach", "TTS init failed with code $status")
    }
  }

  fun toggleMute() {
    val newMute = !_isMuted.value
    _isMuted.value = newMute
    if (newMute) {
      textToSpeech?.stop()
      _isSpeaking.value = false
    } else {
      speak("Voice coaching unmuted", priority = true)
    }
  }

  fun setMuted(muted: Boolean) {
    _isMuted.value = muted
    if (muted) {
      textToSpeech?.stop()
      _isSpeaking.value = false
    }
  }

  fun setCoachStyle(style: VoiceCoachStyle) {
    _coachStyle.value = style
    speak("Coach style set to ${style.displayName}", priority = true)
  }

  fun speak(text: String, priority: Boolean = false) {
    if (_isMuted.value) return

    val now = System.currentTimeMillis()
    // Cooldown check: 2400ms unless it's a high priority biomechanical warning
    val cooldown = if (priority) 1000L else 2400L
    if (now - lastSpokenTimestamp < cooldown && !priority) {
      return
    }

    if (text == lastSpokenText && now - lastSpokenTimestamp < 5000L && !priority) {
      return
    }

    lastSpokenTimestamp = now
    lastSpokenText = text
    _lastSpokenCue.value = text

    if (isInitialized && textToSpeech != null) {
      val queueMode = if (priority) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
      textToSpeech?.speak(text, queueMode, null, "utterance_${System.currentTimeMillis()}")
    } else {
      Log.d("TechniqueVoiceCoach", "TTS not ready or simulated: $text")
    }
  }

  /**
   * Evaluates live kinematic tracking values from camera pose analysis and speaks
   * concise verbal coaching cues matching the athlete's swing execution.
   */
  fun evaluateLivePoseTelemetry(
    phase: String,
    elbowDeg: Float,
    zenithMeters: Float,
    speedKmH: Int,
    faultAlert: String?
  ) {
    if (_isMuted.value) return

    val style = _coachStyle.value

    // 1. High Priority Biomechanical Fault Alerts
    if (faultAlert != null) {
      val cue = when (style) {
        VoiceCoachStyle.TACTICAL -> "Fault detected. $faultAlert"
        VoiceCoachStyle.ENERGETIC -> "Watch that form! $faultAlert"
        VoiceCoachStyle.CONCISE -> "Fault: $faultAlert"
      }
      speak(cue, priority = true)
      return
    }

    // 2. Specific phase-based technique feedback
    when {
      // Impact / High Velocity Contact
      phase.contains("Impact", ignoreCase = true) || phase.contains("Contact", ignoreCase = true) -> {
        if (speedKmH > 230) {
          val cue = when (style) {
            VoiceCoachStyle.TACTICAL -> "Power contact! $speedKmH kilometers per hour with steep downward angle!"
            VoiceCoachStyle.ENERGETIC -> "Boom! $speedKmH k-m-h smash! Monster hit!"
            VoiceCoachStyle.CONCISE -> "Power contact! $speedKmH k-m-h!"
          }
          speak(cue, priority = false)
        } else if (zenithMeters >= 2.85f) {
          val cue = when (style) {
            VoiceCoachStyle.TACTICAL -> "Apex contact confirmed at ${String.format("%.2f", zenithMeters)} meters."
            VoiceCoachStyle.ENERGETIC -> "Great elevation! Full extension reached!"
            VoiceCoachStyle.CONCISE -> "Apex zenith reached!"
          }
          speak(cue, priority = false)
        }
      }

      // Cocking / Load Phase: check elbow elevation
      phase.contains("Cock", ignoreCase = true) || phase.contains("Load", ignoreCase = true) -> {
        if (elbowDeg < 115f) {
          val cue = when (style) {
            VoiceCoachStyle.TACTICAL -> "Elbow angle at ${elbowDeg.toInt()} degrees. Elevate elbow to 140 for power uncoil."
            VoiceCoachStyle.ENERGETIC -> "Lift that elbow higher before you swing!"
            VoiceCoachStyle.CONCISE -> "Elbow higher."
          }
          speak(cue, priority = false)
        } else if (elbowDeg >= 135f) {
          val cue = when (style) {
            VoiceCoachStyle.TACTICAL -> "Optimal cocking angle. Drive through your hips now."
            VoiceCoachStyle.ENERGETIC -> "Perfect shoulder coil! Unleash it!"
            VoiceCoachStyle.CONCISE -> "Good coil. Drive hips."
          }
          speak(cue, priority = false)
        }
      }

      // Follow Through / Recovery
      phase.contains("Follow", ignoreCase = true) || phase.contains("Recovery", ignoreCase = true) -> {
        val cue = when (style) {
          VoiceCoachStyle.TACTICAL -> "Pronate forearm through contact and split-step back to center T."
          VoiceCoachStyle.ENERGETIC -> "Clean recovery! Ready for the next shuttle!"
          VoiceCoachStyle.CONCISE -> "Pronate and recover."
        }
        speak(cue, priority = false)
      }
    }
  }

  fun testCoachSpeech() {
    val sampleCues = listOf(
      "AceAI Voice Coach online. Keep your hitting elbow elevated and drive through your hips.",
      "High apex reach detected at 2.88 meters! Clean pronation sequence.",
      "Smash exit velocity 242 kilometers per hour! Personal best form score."
    )
    val chosen = sampleCues.random()
    speak(chosen, priority = true)
  }

  fun shutdown() {
    try {
      textToSpeech?.stop()
      textToSpeech?.shutdown()
      textToSpeech = null
      isInitialized = false
    } catch (e: Exception) {
      Log.w("TechniqueVoiceCoach", "TTS shutdown exception: ${e.message}")
    }
  }
}
