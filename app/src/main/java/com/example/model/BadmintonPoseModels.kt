package com.example.model

data class PoseLandmarkPoint(
  val id: Int,
  val name: String,
  val x: Float, // Normalized 0.0 .. 1.0
  val y: Float, // Normalized 0.0 .. 1.0
  val confidence: Float = 0.95f
)

data class BadmintonPoseFrame(
  val nose: PoseLandmarkPoint,
  val neck: PoseLandmarkPoint,
  val leftShoulder: PoseLandmarkPoint,
  val rightShoulder: PoseLandmarkPoint,
  val leftElbow: PoseLandmarkPoint,
  val rightElbow: PoseLandmarkPoint,
  val leftWrist: PoseLandmarkPoint,
  val rightWrist: PoseLandmarkPoint,
  val racketTip: PoseLandmarkPoint,
  val leftHip: PoseLandmarkPoint,
  val rightHip: PoseLandmarkPoint,
  val leftKnee: PoseLandmarkPoint,
  val rightKnee: PoseLandmarkPoint,
  val leftAnkle: PoseLandmarkPoint,
  val rightAnkle: PoseLandmarkPoint,
  val elbowAngleDeg: Float,
  val zenithReachMeters: Float,
  val smashSpeedKmH: Int,
  val kineticPhase: String,
  val formScore: Int,
  val faultAlert: String? = null
)

object BadmintonPoseEngine {
  fun generateAnimatedPose(timeSec: Float): BadmintonPoseFrame {
    // Phase cycle of badminton overhead smash:
    // 0..1.2s: Preparation & Coiling
    // 1.2..2.0s: Jump & Knee Flexion
    // 2.0..2.8s: Elbow Drive & Zenith Apex Contact
    // 2.8..4.0s: Follow-through & Pronation
    val cycle = (timeSec % 4.0f)

    val (phase, elbowAngle, reach, speed, form, fault) = when {
      cycle < 1.2f -> {
        val t = cycle / 1.2f
        Tuple6(
          "Preparation & Coiling",
          105f + (t * 15f),
          2.40f + (t * 0.15f),
          0,
          88,
          null
        )
      }
      cycle < 2.0f -> {
        val t = (cycle - 1.2f) / 0.8f
        Tuple6(
          "Kinetic Jump Loading",
          120f + (t * 22f),
          2.55f + (t * 0.22f),
          65 + (t * 40).toInt(),
          92,
          null
        )
      }
      cycle < 2.8f -> {
        val t = (cycle - 2.0f) / 0.8f
        Tuple6(
          "Impact Zenith Strike",
          142f + (t * 28f),
          2.77f + (t * 0.11f),
          210 + (t * 32).toInt(),
          96,
          "Optimal 168° Zenith Angle"
        )
      }
      else -> {
        val t = (cycle - 2.8f) / 1.2f
        Tuple6(
          "Pronation Follow-Through",
          170f - (t * 55f),
          2.88f - (t * 0.45f),
          242 - (t * 90).toInt(),
          90,
          null
        )
      }
    }

    // Dynamic joint coordinates swaying realistically with phase
    val armExtension = (elbowAngle - 100f) / 70f
    val rWristY = 0.28f - (armExtension * 0.14f)
    val rElbowY = 0.38f - (armExtension * 0.08f)
    val racketTipY = rWristY - 0.10f

    return BadmintonPoseFrame(
      nose = PoseLandmarkPoint(0, "Nose", 0.50f, 0.32f),
      neck = PoseLandmarkPoint(1, "Neck", 0.49f, 0.37f),
      leftShoulder = PoseLandmarkPoint(2, "Left Shoulder", 0.40f, 0.39f),
      rightShoulder = PoseLandmarkPoint(3, "Right Shoulder", 0.59f, 0.38f),
      leftElbow = PoseLandmarkPoint(4, "Left Elbow", 0.32f, 0.45f),
      rightElbow = PoseLandmarkPoint(5, "Right Elbow", 0.68f, rElbowY),
      leftWrist = PoseLandmarkPoint(6, "Left Wrist", 0.28f, 0.40f),
      rightWrist = PoseLandmarkPoint(7, "Right Wrist", 0.65f, rWristY),
      racketTip = PoseLandmarkPoint(8, "Racket Face", 0.73f, racketTipY),
      leftHip = PoseLandmarkPoint(9, "Left Hip", 0.43f, 0.56f),
      rightHip = PoseLandmarkPoint(10, "Right Hip", 0.56f, 0.55f),
      leftKnee = PoseLandmarkPoint(11, "Left Knee", 0.40f, 0.70f),
      rightKnee = PoseLandmarkPoint(12, "Right Knee", 0.61f, 0.69f),
      leftAnkle = PoseLandmarkPoint(13, "Left Ankle", 0.36f, 0.85f),
      rightAnkle = PoseLandmarkPoint(14, "Right Ankle", 0.66f, 0.84f),
      elbowAngleDeg = elbowAngle,
      zenithReachMeters = reach,
      smashSpeedKmH = speed,
      kineticPhase = phase,
      formScore = form,
      faultAlert = fault
    )
  }

  private data class Tuple6<A, B, C, D, E, F>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
  )
}
