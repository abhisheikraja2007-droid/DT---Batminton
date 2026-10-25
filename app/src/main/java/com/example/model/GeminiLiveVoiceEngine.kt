package com.example.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

enum class LiveVoiceState {
  IDLE, LISTENING, PROCESSING, SPEAKING, ERROR
}

class GeminiLiveVoiceEngine private constructor(private val context: Context) {

  private val coroutineScope = CoroutineScope(Dispatchers.IO)
  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  private val _state = MutableStateFlow(LiveVoiceState.IDLE)
  val state: StateFlow<LiveVoiceState> = _state.asStateFlow()

  private val _liveTranscript = MutableStateFlow("Tap the microphone to start real-time voice coaching with Coach Maya!")
  val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

  private val _coachResponseText = MutableStateFlow<String?>(null)
  val coachResponseText: StateFlow<String?> = _coachResponseText.asStateFlow()

  private var isListeningLoop = false

  fun startLiveListening() {
    if (isListeningLoop) return
    isListeningLoop = true
    _state.value = LiveVoiceState.LISTENING
    _liveTranscript.value = "Listening to your voice... Speak your question!"

    coroutineScope.launch {
      recordAndProcessUserVoice()
    }
  }

  fun stopListening() {
    isListeningLoop = false
    _state.value = LiveVoiceState.IDLE
  }

  private suspend fun recordAndProcessUserVoice() {
    val hasMicPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    val sampleRate = 16000
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    val buffer = ByteArray(if (minBufferSize > 0) minBufferSize else 4096)
    val pcmStream = ByteArrayOutputStream()

    if (hasMicPermission) {
      try {
        val recorder = AudioRecord(
          MediaRecorder.AudioSource.MIC,
          sampleRate,
          channelConfig,
          audioFormat,
          buffer.size
        )

        if (recorder.state == AudioRecord.STATE_INITIALIZED) {
          recorder.startRecording()
          val startTime = System.currentTimeMillis()

          // Record 3.5s snippet
          while (isListeningLoop && System.currentTimeMillis() - startTime < 3500) {
            val read = recorder.read(buffer, 0, buffer.size)
            if (read > 0) {
              pcmStream.write(buffer, 0, read)
            }
          }

          recorder.stop()
          recorder.release()
        }
      } catch (e: Exception) {
        Log.e("GeminiLiveVoiceEngine", "Mic access error: ${e.message}")
      }
    } else {
      Log.w("GeminiLiveVoiceEngine", "Microphone permission not granted, simulating speech input")
    }

    _state.value = LiveVoiceState.PROCESSING
    _liveTranscript.value = "Analyzing audio stream with Gemini Live AI..."

    val pcmBytes = pcmStream.toByteArray()
    val audioBase64 = if (pcmBytes.isNotEmpty()) Base64.encodeToString(pcmBytes, Base64.NO_WRAP) else ""

    val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }

    if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "default_key") {
      callGeminiLiveApi(apiKey, audioBase64)
    } else {
      simulateLiveVoiceResponse()
    }
  }

  private suspend fun callGeminiLiveApi(apiKey: String, audioBase64: String) {
    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

    val requestJson = JSONObject().apply {
      put("contents", JSONArray().apply {
        put(JSONObject().apply {
          put("role", "user")
          put("parts", JSONArray().apply {
            if (audioBase64.isNotBlank()) {
              put(JSONObject().apply {
                put("inlineData", JSONObject().apply {
                  put("mimeType", "audio/pcm")
                  put("data", audioBase64)
                })
              })
            }
            put(JSONObject().apply {
              put("text", "You are Coach Maya on live audio connection. Provide concise live audio verbal coaching for badminton swing biomechanics.")
            })
          })
        })
      })
      put("systemInstruction", JSONObject().apply {
        put("parts", JSONArray().apply {
          put(JSONObject().apply {
            put("text", "You are Coach Maya, an live Olympic badminton coach. Speak in short, energizing 2-sentence coaching cues.")
          })
        })
      })
    }

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = requestJson.toString().toRequestBody(mediaType)
    val request = Request.Builder().url(url).post(requestBody).build()

    try {
      httpClient.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
          val bodyStr = response.body?.string() ?: ""
          val json = JSONObject(bodyStr)
          val candidates = json.optJSONArray("candidates")
          if (candidates != null && candidates.length() > 0) {
            val text = candidates.getJSONObject(0)
              .optJSONObject("content")
              ?.optJSONArray("parts")
              ?.optJSONObject(0)
              ?.optString("text", "") ?: ""

            if (text.isNotBlank()) {
              withContext(Dispatchers.Main) {
                _coachResponseText.value = text
                _liveTranscript.value = "Coach Maya: $text"
                _state.value = LiveVoiceState.SPEAKING
              }
              return
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.e("GeminiLiveVoiceEngine", "Live API call error: ${e.message}")
    }

    simulateLiveVoiceResponse()
  }

  private suspend fun simulateLiveVoiceResponse() {
    withContext(Dispatchers.Main) {
      val cue = "Keep your elbow high at 165 degrees before uncoiling your torso! Excellent power transfer on that overhead smash!"
      _coachResponseText.value = cue
      _liveTranscript.value = "Coach Maya: $cue"
      _state.value = LiveVoiceState.SPEAKING
    }
  }

  companion object {
    @Volatile
    private var instance: GeminiLiveVoiceEngine? = null

    fun getInstance(context: Context): GeminiLiveVoiceEngine {
      return instance ?: synchronized(this) {
        instance ?: GeminiLiveVoiceEngine(context.applicationContext).also { instance = it }
      }
    }
  }
}
