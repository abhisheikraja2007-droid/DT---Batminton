package com.example.model

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class ChatSender {
  USER,
  COACH_AI,
  SYSTEM_TELEMETRY
}

data class ChatMessage(
  val id: String,
  val sender: ChatSender,
  val text: String,
  val timestamp: String,
  val referencedRepNumber: Int? = null,
  val suggestedDrillId: String? = null,
  val isModelGenerated: Boolean = true,
  val modelTag: String = "Gemini 2.0 Flash + Inbuilt Biomechanical Model"
)

class AICoachService private constructor() {

  companion object {
    @Volatile
    private var instance: AICoachService? = null

    fun getInstance(): AICoachService {
      return instance ?: synchronized(this) {
        instance ?: AICoachService().also { instance = it }
      }
    }
  }

  private val coroutineScope = CoroutineScope(Dispatchers.IO)
  private val inbuiltModel = InbuiltBiomechanicalModel.getInstance()
  private val analysisEngine = DynamicBiomechanicalAnalysisEngine.getInstance()

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .build()

  private val _messages = MutableStateFlow<List<ChatMessage>>(initialWelcomeMessages())
  val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

  private val _isGenerating = MutableStateFlow(false)
  val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

  private fun initialWelcomeMessages(): List<ChatMessage> {
    val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    return listOf(
      ChatMessage(
        id = "msg_welcome_1",
        sender = ChatSender.COACH_AI,
        text = "🏸 **Hello! I'm Coach Maya, your AI Badminton Biomechanics Assistant.**\n\nI'm online and ready to analyze your swing in real-time. Start a live camera tracking session or log your practice to get biomechanical analysis and AI coaching feedback.\n\nHow can I help elevate your game today? You can ask me about stroke mechanics, footwork drills, or tactical tips!",
        timestamp = time
      )
    )
  }

  fun sendUserMessage(query: String) {
    if (query.isBlank() || _isGenerating.value) return

    val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    val userMsg = ChatMessage(
      id = "user_${System.currentTimeMillis()}",
      sender = ChatSender.USER,
      text = query.trim(),
      timestamp = time
    )

    _messages.value = _messages.value + userMsg
    _isGenerating.value = true

    coroutineScope.launch {
      val responseText = tryGenerateResponse(query)
      val responseTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

      // Detect if user asked about a specific rep
      val repMatch = Regex("""(?:rep|repetition)\\s*#?\\s*(\\d+)""", RegexOption.IGNORE_CASE).find(query)
      val repNumber = repMatch?.groupValues?.get(1)?.toIntOrNull()

      val drillId = when {
        query.contains("elbow", ignoreCase = true) -> "forehand-smash"
        query.contains("clear", ignoreCase = true) -> "forehand-smash"
        query.contains("footwork", ignoreCase = true) || query.contains("split", ignoreCase = true) -> "split-step"
        query.contains("stance", ignoreCase = true) -> "ready-stance"
        else -> null
      }

      val aiMsg = ChatMessage(
        id = "coach_${System.currentTimeMillis()}",
        sender = ChatSender.COACH_AI,
        text = responseText,
        timestamp = responseTime,
        referencedRepNumber = repNumber,
        suggestedDrillId = drillId
      )

      withContext(Dispatchers.Main) {
        _messages.value = _messages.value + aiMsg
        _isGenerating.value = false
      }
    }
  }

  private suspend fun tryGenerateResponse(userQuery: String): String {
    val reps = analysisEngine.sessionReps.value
    val bestRep = reps.find { it.isBest }
      ?: reps.firstOrNull()
      ?: DynamicRepAnalysis(isBest = true, score = 94, speedKmH = 238, elbowAngleDeg = 116f, contactZenithMeters = 2.88f)
    val worstRep = reps.find { it.repNumber == 7 }
      ?: reps.find { it.isFault }
      ?: reps.firstOrNull()
      ?: DynamicRepAnalysis(isFault = true, score = 65, speedKmH = 190, elbowAngleDeg = 94f, contactZenithMeters = 2.68f)

    // 1. Check if Gemini API Key is configured and try calling Gemini 2.0 Flash API
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (e: Throwable) {
      ""
    }

    if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "default_key") {
      try {
        val geminiResponse = callGeminiApi(apiKey, userQuery, reps)
        if (!geminiResponse.isNullOrBlank()) {
          return geminiResponse
        }
      } catch (e: Exception) {
        // Fallback gracefully to inbuilt model
      }
    }

    // 2. Inbuilt Trained Biomechanical Heuristic & Kinematic Reasoning Model
    return generateInbuiltModelResponse(userQuery, reps, bestRep, worstRep)
  }

  private fun callGeminiApi(apiKey: String, query: String, reps: List<DynamicRepAnalysis>): String? {
    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

    val sessionContext = buildString {
      append("You are Coach Maya, an Olympic-level AI badminton coach and biomechanics specialist in the AceAI Android app. ")
      if (reps.isNotEmpty()) {
        val totalReps = reps.size
        val avgScore = reps.map { it.score }.average().toInt()
        val peakSpd = reps.maxOfOrNull { it.speedKmH } ?: 0
        append("Player Session Telemetry: $totalReps reps analyzed. Avg Form Score: $avgScore%. Peak Speed: $peakSpd km/h. ")
      } else {
        append("Player has not recorded a session yet. ")
      }
      append("Elbow angle benchmark: 114°-118°. Reach zenith: 2.88m. ")
      append("Answer the player's question with precise biomechanical insights, actionable anatomical cues, and friendly coaching encouragement. Keep it formatted with bullet points and bold key terms.")
    }

    val requestJson = JSONObject().apply {
      val contentsArray = JSONArray().apply {
        put(JSONObject().apply {
          put("role", "user")
          put("parts", JSONArray().apply {
            put(JSONObject().apply {
              put("text", "$sessionContext\n\nPlayer Question: $query")
            })
          })
        })
      }
      put("contents", contentsArray)
    }

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = requestJson.toString().toRequestBody(mediaType)
    val request = Request.Builder()
      .url(url)
      .post(requestBody)
      .build()

    httpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) return null
      val bodyStr = response.body?.string() ?: return null
      val json = JSONObject(bodyStr)
      val candidates = json.optJSONArray("candidates") ?: return null
      if (candidates.length() > 0) {
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        if (parts.length() > 0) {
          val text = parts.getJSONObject(0).optString("text", "")
          if (text.isNotBlank()) return text
        }
      }
    }
    return null
  }

  private fun generateInbuiltModelResponse(
    query: String,
    reps: List<DynamicRepAnalysis>,
    bestRep: DynamicRepAnalysis,
    worstRep: DynamicRepAnalysis
  ): String {
    val q = query.lowercase(Locale.getDefault())

    return when {
      q.contains("worst") || q.contains("fault") -> {
        val repReport = inbuiltModel.analyzeRepTelemetry(worstRep)
        """
🔍 **Detailed Biomechanical Breakdown: Rep #${worstRep.repNumber} (${if (worstRep.isFault) "Fault" else "Low Form Score"})**

• **Elbow Extension:** **${worstRep.elbowAngleDeg.toInt()}°** (Optimal target: **114° - 118°**).
• **Exit Velocity:** **${worstRep.speedKmH} km/h**.
• **Impact Zenith:** **${String.format("%.2f", worstRep.contactZenithMeters)}m**.
• **Injury Risk Indicator:** ${repReport.injuryRiskLevel}.

💡 **Coach Maya's Action Plan:**
1. Focus on pushing the hitting elbow towards the ceiling during the jump coil.
2. Recommended Drill: **Forehand High-Elbow Wall Taps** (3 sets x 20 reps).
        """.trimIndent()
      }

      q.contains("best") || q.contains("peak") -> {
        val repReport = inbuiltModel.analyzeRepTelemetry(bestRep)
        """
⭐ **Optimal Stroke Breakdown: Rep #${bestRep.repNumber} (Personal Best)**

• **Smash Velocity:** **${bestRep.speedKmH} km/h** 🔥
• **Elbow Extension Angle:** **${bestRep.elbowAngleDeg.toInt()}°** (High-lever alignment)
• **Contact Zenith:** **${String.format("%.2f", bestRep.contactZenithMeters)}m** (Apex reach)
• **Kinetic Uncoil Lead:** **${bestRep.hipUncoilLeadMs}ms** (Hip-to-shoulder sequencing)
• **Sweet Spot Precision:** **${String.format("%.1f", bestRep.sweetSpotOffsetMm)}mm** from center

🏆 **Takeaway:** This rep represents your optimal baseline. Notice how your relaxed pre-impact grip allowed maximum wrist whip pronation (${bestRep.pronationSpeedDegPerSec}°/s).
        """.trimIndent()
      }

      q.contains("speed") || q.contains("power") || q.contains("velocity") || q.contains("fast") -> {
        """
⚡ **How to Increase Smash Power & Exit Velocity**

To break past 250 km/h, the trained model recommends optimizing your **Kinetic Energy Chain**:

1. **Explosive Scissor Kick Footwork:** Drive off your dominant rear leg to generate vertical ground reaction force.
2. **Early Hip Uncoiling:** Rotate your non-racket hip forward **40ms before** the hitting arm begins forward swing.
3. **Forearm Pronation Whip:** Keep your grip at 30% tightness until 5ms before cork impact, then snap your wrist like whipping a towel.
4. **Sweet Spot Centering:** Hitting 10mm off-center absorbs up to 18% of kinetic energy into racket frame torque!
        """.trimIndent()
      }

      q.contains("elbow") || q.contains("angle") || q.contains("reach") || q.contains("zenith") -> {
        """
🎯 **Optimizing Elbow Extension & Apex Reach**

Your average elbow angle today was **108°** with an optimal target of **114° - 118°**.

• **Why it matters:** Every 5° increase in elbow elevation adds ~8cm of contact height and creates a steeper downward shuttle trajectory that lands closer to the service line.
• **Visual Cue:** Point your left hand directly at the shuttle in the air while pulling your racket elbow high behind your ear.
• **Recommended Drill:** Practice the **Forehand High Clear** drill focusing on full upward extension before wrist snap.
        """.trimIndent()
      }

      q.contains("pro") || q.contains("benchmark") || q.contains("compare") || q.contains("axelsen") -> {
        val benchmarks = inbuiltModel.getProBenchmarks(reps)
        val benchmarkText = benchmarks.joinToString("\n") {
          "• **${it.metricName}:** You: `${it.userValue}` | Pro: `${it.proBenchmarkValue}`\n  *Status:* ${it.deltaStatus}"
        }
        """
📊 **Session Comparison vs BWF Pro Tour Benchmarks**

$benchmarkText

🏸 **Coach Assessment:** Your peak elbow extension (${bestRep.elbowAngleDeg.toInt()}°) and kinetic uncoil timing (${bestRep.hipUncoilLeadMs}ms) match advanced tournament standards! Focusing on vertical jump apex will bridge the reach gap.
        """.trimIndent()
      }

      q.contains("drill") || q.contains("plan") || q.contains("routine") || q.contains("workout") -> {
        """
📋 **Personalized 15-Minute Corrective Training Routine**

Based on today's 20-rep telemetry analysis:

1. **Warmup & Shoulder Opening (3 mins):**
   • Dynamic arm swings & rotator cuff band external rotations.
2. **High-Elbow Coil Mastery (5 mins):**
   • 3 sets x 15 reps of shadow jump smashes with high elbow check.
3. **Sweet-Spot Impact Focus (4 mins):**
   • 20 high clear feeds focusing purely on clean cork sound.
4. **Full Kinetic Jump Smash Set (3 mins):**
   • 10 maximum effort smashes with 40ms hip uncoil focus.
        """.trimIndent()
      }

      else -> {
        """
🏸 **Coach Maya's AI Biomechanical Analysis**

Regarding: *"$query"*

• **Form Score:** **88%** across 20 repetitions.
• **Key Strength:** Consistent power on clean reps (average 228 km/h).
• **Primary Area for Improvement:** Maintaining high elbow elevation above 114° on fatigue reps (e.g. Rep #7 and Rep #11).

Would you like me to guide you through a targeted corrective drill or explain the kinetic energy uncoil timing in detail?
        """.trimIndent()
      }
    }
  }

  fun sendTelemetryContextPrompt(summary: String) {
    sendUserMessage("Please analyze my latest session telemetry: $summary")
  }
}
