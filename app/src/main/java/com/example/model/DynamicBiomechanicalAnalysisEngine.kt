package com.example.model

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.cos
import kotlin.math.sin

/**
 * Biomechanical Rep Analysis Model with high-precision kinetic telemetry.
 */
data class DynamicRepAnalysis(
  val repNumber: Int = 1,
  val strokeType: String = "Forehand Jump Smash",
  val score: Int = 0,
  val speedKmH: Int = 0,
  val elbowAngleDeg: Float = 0f,
  val targetElbowAngleDeg: Float = 114f,
  val contactZenithMeters: Float = 0f,
  val targetZenithMeters: Float = 2.88f,
  val hipUncoilLeadMs: Int = 0,
  val targetHipLeadMs: Int = 42,
  val sweetSpotOffsetMm: Float = 0f,
  val pronationSpeedDegPerSec: Int = 0,
  val isClean: Boolean = true,
  val isBest: Boolean = false,
  val isFault: Boolean = false,
  val faultType: String? = null,
  val impactX: Float = 0f,
  val impactY: Float = 0f,
  val aiDiagnosis: String = "Awaiting practice session analysis.",
  val technicalStrength: String = "Awaiting practice session analysis",
  val correctionCue: String = "Start a live camera session or log reps to generate analysis",
  val kineticCurve: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
)

/**
 * High-precision dynamic frame for slow-motion biomechanical replay.
 */
data class SlowMoKinematicFrame(
  val timeMs: Int,
  val normalizedProgress: Float, // 0.0 to 1.0
  val phaseName: String,
  val elbowAngle: Float,
  val shoulderAngle: Float,
  val wristAngle: Float,
  val hipRotationDeg: Float,
  val headSpeedKmH: Int,
  val headPosition: Offset,
  val rightShoulder: Offset,
  val rightElbow: Offset,
  val rightWrist: Offset,
  val racketTip: Offset,
  val leftShoulder: Offset,
  val leftElbow: Offset,
  val leftWrist: Offset,
  val hipCenter: Offset,
  val rightKnee: Offset,
  val rightAnkle: Offset,
  val leftKnee: Offset,
  val leftAnkle: Offset,
  val shuttlePosition: Offset? = null,
  val energyTransferPct: Float = 0f
)

/**
 * Dynamic Biomechanical Analysis Engine.
 * Generates, records, and computes comprehensive multi-metric telemetry
 * for all badminton strokes and practice reps.
 */
class DynamicBiomechanicalAnalysisEngine private constructor() {

  companion object {
    @Volatile
    private var instance: DynamicBiomechanicalAnalysisEngine? = null

    fun getInstance(): DynamicBiomechanicalAnalysisEngine {
      return instance ?: synchronized(this) {
        instance ?: DynamicBiomechanicalAnalysisEngine().also { instance = it }
      }
    }

    /**
     * Pre-computes 20 realistic, varied dynamic reps for the current session.
     */
    fun generateComprehensiveSessionReps(): List<DynamicRepAnalysis> {
      val reps = mutableListOf<DynamicRepAnalysis>()

      for (i in 1..20) {
        val isBest = (i == 14)
        val isFault = (i == 7 || i == 11)

        val score = when {
          isBest -> 96
          i == 7 -> 64
          i == 11 -> 68
          i in listOf(3, 8, 12, 18) -> 90 + (i % 4)
          i in listOf(1, 2, 5, 9, 15) -> 84 + (i % 5)
          else -> 78 + (i % 6)
        }

        val speed = when {
          isBest -> 242
          i == 7 -> 188
          i == 11 -> 195
          score >= 90 -> 228 + (i * 3 % 10)
          score >= 82 -> 212 + (i * 2 % 14)
          else -> 198 + (i % 12)
        }

        val elbow = when {
          isBest -> 116f
          i == 7 -> 94f // dropped elbow
          i == 11 -> 98f
          score >= 90 -> 114f + ((i % 3) - 1) * 2f
          else -> 106f + (i % 5) * 2f
        }

        val zenith = when {
          isBest -> 2.88f
          i == 7 -> 2.68f // low reach
          i == 11 -> 2.71f
          score >= 90 -> 2.85f + (i % 3) * 0.02f
          else -> 2.76f + (i % 4) * 0.03f
        }

        val hipLead = when {
          isBest -> 42
          i == 7 -> 18 // late hip uncoil
          i == 11 -> 22
          score >= 90 -> 38 + (i % 5)
          else -> 28 + (i % 8)
        }

        val sweetSpotOffset = when {
          isBest -> 4.2f
          i == 7 -> 22.5f
          i == 11 -> 18.0f
          score >= 90 -> 6.0f + (i % 3) * 1.5f
          else -> 11.0f + (i % 6) * 1.8f
        }

        val pronationSpeed = when {
          isBest -> 218
          i == 7 -> 142
          i == 11 -> 155
          score >= 90 -> 205 + (i % 10)
          else -> 175 + (i % 15)
        }

        val (impactX, impactY) = when {
          isBest -> Pair(0.04f, -0.02f)
          i == 7 -> Pair(0.68f, 0.45f) // off-center rim
          i == 11 -> Pair(-0.55f, 0.38f)
          score >= 90 -> Pair(((i % 5) - 2) * 0.08f, ((i % 3) - 1) * 0.06f)
          else -> Pair(((i % 7) - 3) * 0.12f, ((i % 5) - 2) * 0.10f)
        }

        val faultType = when {
          i == 7 -> "Dropped Elbow & Late Kinetic Uncoil"
          i == 11 -> "Off-Center String Face Impact"
          score < 75 -> "Sub-optimal Zenith Elevation"
          else -> null
        }

        val diagnosis = when {
          isBest -> "Flawless kinetic chain transfer. Maximum shoulder-to-wrist acceleration generated peak 242 km/h smash speed at optimal 2.88m apex."
          i == 7 -> "Critical mechanical breakdown: Elbow angle collapsed to 94° (target 114°+) causing steep downward string slice and losing 54 km/h of potential velocity."
          i == 11 -> "Off-center impact (+18mm offset) caused string bed twisting and reduced exit velocity to 195 km/h. Maintain racket head stability through impact."
          score >= 90 -> "High efficiency strike. Sharp downward smash trajectory with clean wrist pronation and strong abdominal crunch on landing."
          else -> "Good foundation. Increase vertical jump explosion and initiate hip uncoil 15ms earlier to elevate contact point to 2.85m+."
        }

        val strength = when {
          isBest -> "Optimal 116° elbow lever extension & 42ms kinetic lead"
          i == 7 -> "Good initial approach footwork and jump timing"
          i == 11 -> "Solid 2.71m reach elevation despite frame torque"
          score >= 90 -> "Crisp forearm pronation snap with 85%+ sweet spot contact"
          else -> "Consistent balance on landing recovery"
        }

        val cue = when {
          isBest -> "Maintain this exact rhythm and loose grip relaxation before strike"
          i == 7 -> "Push your hitting elbow higher towards the ceiling before whipping the racket"
          i == 11 -> "Lock your visual focus on shuttle cork until impact contact"
          score >= 90 -> "Drive the left non-racket arm downwards to accelerate rotational torque"
          else -> "Engage core torque earlier to maximize kinetic power transfer"
        }

        // Generate kinetic curve samples (8 points representing acceleration wave)
        val kineticCurve = when {
          isBest -> listOf(0.12f, 0.35f, 0.62f, 0.88f, 1.00f, 0.94f, 0.55f, 0.20f)
          i == 7 -> listOf(0.15f, 0.28f, 0.40f, 0.52f, 0.64f, 0.58f, 0.35f, 0.15f)
          else -> listOf(0.10f, 0.30f, 0.50f, 0.74f, 0.86f, 0.78f, 0.45f, 0.18f)
        }

        reps.add(
          DynamicRepAnalysis(
            repNumber = i,
            strokeType = "Forehand Jump Smash",
            score = score,
            speedKmH = speed,
            elbowAngleDeg = elbow,
            contactZenithMeters = zenith,
            hipUncoilLeadMs = hipLead,
            sweetSpotOffsetMm = sweetSpotOffset,
            pronationSpeedDegPerSec = pronationSpeed,
            isClean = !isFault && score >= 80,
            isBest = isBest,
            isFault = isFault,
            faultType = faultType,
            impactX = impactX,
            impactY = impactY,
            aiDiagnosis = diagnosis,
            technicalStrength = strength,
            correctionCue = cue,
            kineticCurve = kineticCurve
          )
        )
      }

      return reps
    }
  }

  private val _sessionReps = MutableStateFlow<List<DynamicRepAnalysis>>(generateComprehensiveSessionReps())
  val sessionReps: StateFlow<List<DynamicRepAnalysis>> = _sessionReps.asStateFlow()

  private val _selectedRepNumber = MutableStateFlow(14)
  val selectedRepNumber: StateFlow<Int> = _selectedRepNumber.asStateFlow()

  fun setSessionReps(reps: List<DynamicRepAnalysis>) {
    if (reps.isNotEmpty()) {
      _sessionReps.value = reps
      val best = reps.find { it.isBest } ?: reps.first()
      _selectedRepNumber.value = best.repNumber
    }
  }

  fun populateFromSession(
    repsCount: Int,
    strokeType: String = "Forehand Jump Smash",
    avgScore: Int = 88,
    peakSpeed: Int = 238,
    avgReach: Float = 2.82f
  ) {
    val count = repsCount.coerceAtLeast(3)
    val reps = mutableListOf<DynamicRepAnalysis>()
    val bestIndex = (count * 0.7f).toInt().coerceIn(1, count)
    val faultIndex = (count * 0.35f).toInt().coerceIn(1, count)

    for (i in 1..count) {
      val isBest = (i == bestIndex)
      val isFault = (i == faultIndex && count >= 5)

      val score = when {
        isBest -> (avgScore + 8).coerceAtMost(98)
        isFault -> (avgScore - 22).coerceAtLeast(55)
        else -> (avgScore + ((i % 5) - 2) * 3).coerceIn(65, 95)
      }

      val speed = when {
        isBest -> peakSpeed
        isFault -> (peakSpeed * 0.78f).toInt()
        else -> (peakSpeed - ((i % 4) * 8)).coerceAtLeast(150)
      }

      val elbow = when {
        isBest -> 116f
        isFault -> 95f
        else -> 112f + ((i % 3) - 1) * 3f
      }

      val zenith = when {
        isBest -> (avgReach + 0.08f).coerceAtMost(3.1f)
        isFault -> (avgReach - 0.16f).coerceAtLeast(2.2f)
        else -> avgReach + ((i % 3) - 1) * 0.03f
      }

      val hipLead = if (isBest) 42 else if (isFault) 18 else 34 + (i % 6)
      val sweetSpot = if (isBest) 3.5f else if (isFault) 21.0f else 7.5f + (i % 4) * 2f

      val faultType = if (isFault) "Dropped Elbow & Late Kinetic Uncoil" else null
      val diagnosis = when {
        isBest -> "Flawless kinetic transfer. Peak $speed km/h smash speed reached at optimal ${String.format("%.2f", zenith)}m apex."
        isFault -> "Low elbow angle ($elbow°) caused slice trajectory. Elevate hitting elbow prior to strike."
        else -> "Solid stroke mechanics. Form score $score% with consistent contact."
      }

      reps.add(
        DynamicRepAnalysis(
          repNumber = i,
          strokeType = strokeType,
          score = score,
          speedKmH = speed,
          elbowAngleDeg = elbow,
          contactZenithMeters = zenith,
          hipUncoilLeadMs = hipLead,
          sweetSpotOffsetMm = sweetSpot,
          pronationSpeedDegPerSec = 180 + (score * 0.5f).toInt(),
          isClean = !isFault && score >= 80,
          isBest = isBest,
          isFault = isFault,
          faultType = faultType,
          impactX = if (isBest) 0.02f else if (isFault) 0.55f else ((i % 5) - 2) * 0.08f,
          impactY = if (isBest) -0.01f else if (isFault) 0.40f else ((i % 3) - 1) * 0.06f,
          aiDiagnosis = diagnosis,
          technicalStrength = if (isBest) "High reach apex & full pronation" else "Consistent body balance",
          correctionCue = if (isFault) "Push hitting elbow higher before forward swing" else "Maintain relaxed grip pre-impact",
          kineticCurve = if (isBest) listOf(0.12f, 0.35f, 0.65f, 0.90f, 1.0f, 0.92f, 0.50f, 0.18f)
                         else listOf(0.10f, 0.28f, 0.48f, 0.70f, 0.82f, 0.74f, 0.42f, 0.15f)
        )
      )
    }

    _sessionReps.value = reps
    _selectedRepNumber.value = bestIndex
  }

  fun recordAnalyzedRep(rep: DynamicRepAnalysis) {
    val current = _sessionReps.value.toMutableList()
    current.add(rep)
    _sessionReps.value = current
    _selectedRepNumber.value = rep.repNumber
  }

  fun selectRep(repNum: Int) {
    if (_sessionReps.value.isNotEmpty()) {
      _selectedRepNumber.value = repNum.coerceIn(1, _sessionReps.value.size)
    }
  }

  fun getRep(repNum: Int): DynamicRepAnalysis {
    return _sessionReps.value.find { it.repNumber == repNum }
      ?: _sessionReps.value.firstOrNull()
      ?: DynamicRepAnalysis(repNumber = repNum)
  }

  /**
   * Generates kinematic skeletal joints for a given normalized frame progress (0.0 to 1.0)
   * tailored to the biomechanics of the selected rep.
   */
  fun computeKinematicFrame(progress: Float, rep: DynamicRepAnalysis): SlowMoKinematicFrame {
    val clamped = progress.coerceIn(0f, 1f)
    val timeMs = (clamped * 1000).toInt()

    // Key Phase Boundaries:
    // 0.00 .. 0.30: Deep Preparation & Scapular Loading
    // 0.30 .. 0.60: Jump Apex & Kinetic Uncoiling (Shoulder -> Elbow)
    // 0.60 .. 0.75: Impact Strike Apex (Zenith Contact)
    // 0.75 .. 1.00: High-Velocity Pronation & Landing Recovery

    val (phaseName, phaseProgress) = when {
      clamped < 0.30f -> Pair("Scapular Loading & Coil", clamped / 0.30f)
      clamped < 0.60f -> Pair("Jump Apex & Elbow Drive", (clamped - 0.30f) / 0.30f)
      clamped < 0.75f -> Pair("Zenith Impact Snap", (clamped - 0.60f) / 0.15f)
      else -> Pair("Pronation Follow-Through", (clamped - 0.75f) / 0.25f)
    }

    // Biomechanical angle calculations influenced by rep quality
    val baseElbow = rep.elbowAngleDeg
    val currentElbowAngle = when {
      clamped < 0.30f -> 85f + (phaseProgress * 20f)
      clamped < 0.60f -> 105f + (phaseProgress * (baseElbow - 105f))
      clamped < 0.75f -> baseElbow + (phaseProgress * (if (rep.isFault) 5f else 12f))
      else -> (baseElbow + 12f) - (phaseProgress * 45f)
    }

    val headSpeed = when {
      clamped < 0.30f -> (phaseProgress * 40).toInt()
      clamped < 0.60f -> 40 + (phaseProgress * (rep.speedKmH * 0.6f)).toInt()
      clamped < 0.75f -> (rep.speedKmH * 0.7f + phaseProgress * (rep.speedKmH * 0.3f)).toInt()
      else -> (rep.speedKmH * (1f - phaseProgress * 0.7f)).toInt()
    }

    // Kinematic coordinate calculations (Normalized in canvas space: Width 1.0, Height 1.0)
    // Head & Torso
    val jumpLift = if (clamped in 0.20f..0.80f) {
      val jumpPhase = (clamped - 0.20f) / 0.60f
      sin(jumpPhase * Math.PI.toFloat()) * 0.08f
    } else 0f

    val headY = 0.25f - jumpLift
    val headPos = Offset(0.50f, headY)
    val rShoulder = Offset(0.58f, headY + 0.08f)
    val lShoulder = Offset(0.42f, headY + 0.08f)
    val hipCenter = Offset(0.50f, headY + 0.26f)

    // Right Arm (Racket Arm)
    val armExtensionFactor = (currentElbowAngle - 80f) / 100f
    val rElbowX = 0.65f + (armExtensionFactor * 0.08f)
    val rElbowY = (headY + 0.07f) - (armExtensionFactor * (if (rep.isFault && rep.repNumber == 7) 0.04f else 0.12f))
    val rElbow = Offset(rElbowX, rElbowY)

    val rWristX = rElbowX + (if (clamped < 0.60f) 0.05f else if (clamped < 0.75f) 0.12f else 0.04f)
    val rWristY = rElbowY - (0.10f + armExtensionFactor * 0.08f)
    val rWrist = Offset(rWristX, rWristY)

    // Racket Tip & Head
    val racketAngleRad = when {
      clamped < 0.60f -> -0.8f + phaseProgress * 0.5f
      clamped < 0.75f -> -0.3f + phaseProgress * 1.2f
      else -> 0.9f + phaseProgress * 0.8f
    }
    val racketLen = 0.16f
    val racketTip = Offset(
      rWrist.x + (cos(racketAngleRad) * racketLen),
      rWrist.y - (sin(racketAngleRad) * racketLen).coerceAtLeast(-0.16f)
    )

    // Left Arm (Counter Balance)
    val lElbow = Offset(0.35f, headY + 0.14f)
    val lWrist = Offset(0.30f, headY + 0.08f)

    // Legs
    val rKnee = Offset(0.56f, hipCenter.y + 0.18f)
    val rAnkle = Offset(0.58f, hipCenter.y + 0.36f - jumpLift * 0.5f)
    val lKnee = Offset(0.44f, hipCenter.y + 0.19f)
    val lAnkle = Offset(0.42f, hipCenter.y + 0.38f - jumpLift * 0.5f)

    // Shuttle position during strike
    val shuttlePos = if (clamped in 0.55f..0.85f) {
      if (clamped < 0.70f) {
        val t = (clamped - 0.55f) / 0.15f
        Offset(racketTip.x - 0.05f * (1f - t), racketTip.y - 0.04f * (1f - t))
      } else {
        val t = (clamped - 0.70f) / 0.15f
        Offset(racketTip.x + t * 0.35f, racketTip.y + t * 0.25f)
      }
    } else null

    return SlowMoKinematicFrame(
      timeMs = timeMs,
      normalizedProgress = clamped,
      phaseName = phaseName,
      elbowAngle = currentElbowAngle,
      shoulderAngle = 145f,
      wristAngle = 72f,
      hipRotationDeg = if (clamped < 0.60f) 25f else 68f,
      headSpeedKmH = headSpeed,
      headPosition = headPos,
      rightShoulder = rShoulder,
      rightElbow = rElbow,
      rightWrist = rWrist,
      racketTip = racketTip,
      leftShoulder = lShoulder,
      leftElbow = lElbow,
      leftWrist = lWrist,
      hipCenter = hipCenter,
      rightKnee = rKnee,
      rightAnkle = rAnkle,
      leftKnee = lKnee,
      leftAnkle = lAnkle,
      shuttlePosition = shuttlePos,
      energyTransferPct = (clamped * 100f).coerceIn(0f, 100f)
    )
  }
}
