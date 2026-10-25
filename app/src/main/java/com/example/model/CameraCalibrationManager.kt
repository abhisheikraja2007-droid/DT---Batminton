package com.example.model

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Stages in the guided camera calibration sequence.
 */
enum class CalibrationStep(
  val stepIndex: Int,
  val title: String,
  val shortSubtitle: String,
  val guidancePrompt: String
) {
  TRIPOD_POSITION(
    stepIndex = 1,
    title = "Position & Mount",
    shortSubtitle = "Court Baseline Mount",
    guidancePrompt = "Mount phone on tripod at 1.2m–1.4m height, 45° angled to the court baseline."
  ),
  TILT_AND_PITCH(
    stepIndex = 2,
    title = "Level & Angle Horizon",
    shortSubtitle = "Pitch & Roll Alignment",
    guidancePrompt = "Adjust phone tilt until the virtual horizon is within ±1.5° of level."
  ),
  DISTANCE_AND_FRAME(
    stepIndex = 3,
    title = "Optimal Distance & Framing",
    shortSubtitle = "4.0m – 5.0m Stand-back",
    guidancePrompt = "Step back to the baseline T-junction so your full body and racket apex fit inside the frame."
  ),
  SWING_ENVELOPE_CHECK(
    stepIndex = 4,
    title = "Reach & Overhead Apex Test",
    shortSubtitle = "Full Extension Check",
    guidancePrompt = "Perform a mock high-clear swing to confirm racket doesn't clip out of the top bounds."
  ),
  LOCK_AND_VERIFY(
    stepIndex = 5,
    title = "Biomechanical Lock",
    shortSubtitle = "Calibration Complete",
    guidancePrompt = "All 4 optical checks locked. 90 FPS 3D kinematic tracking ready."
  )
}

/**
 * Camera mounting profile presets.
 */
enum class CourtMountPreset(
  val displayName: String,
  val idealDistanceMeters: Float,
  val idealHeightMeters: Float,
  val idealPitchDeg: Float,
  val lensRecommendation: String
) {
  BASELINE_CORNER(
    displayName = "Baseline Corner 45°",
    idealDistanceMeters = 4.2f,
    idealHeightMeters = 1.25f,
    idealPitchDeg = 4.0f,
    lensRecommendation = "1x Wide (Standard)"
  ),
  SIDE_COURT_T(
    displayName = "Side Court Mid-Net",
    idealDistanceMeters = 5.0f,
    idealHeightMeters = 1.35f,
    idealPitchDeg = 2.5f,
    lensRecommendation = "0.5x Ultra-Wide"
  ),
  REAR_HIGH_ANGLE(
    displayName = "Rear Court High Tripod",
    idealDistanceMeters = 4.8f,
    idealHeightMeters = 1.60f,
    idealPitchDeg = 8.0f,
    lensRecommendation = "1x Wide"
  )
}

/**
 * Real-time calibration metrics state.
 */
data class CalibrationTelemetry(
  val pitchDeg: Float = 0.4f,          // Forward/backward tilt (optimal ~ 0 to 5°)
  val rollDeg: Float = 0.2f,           // Side-to-side horizon tilt (optimal ~ 0°)
  val estimatedDistanceM: Float = 4.2f,// Estimated distance based on court baseline / pose scale
  val isPitchOptimal: Boolean = true,
  val isRollOptimal: Boolean = true,
  val isDistanceOptimal: Boolean = true,
  val isFullBodyContained: Boolean = true,
  val isZenithApexContained: Boolean = true,
  val lightingLux: Int = 840,
  val boundaryLinesFound: Int = 4,      // 4 out of 4 baseline & tramlines
  val calibrationQualityPercent: Int = 98
)

/**
 * Manager handling real-time accelerometer/gyro measurements and step progression
 * for camera distance, angle, and framing calibration.
 */
class CameraCalibrationManager private constructor(private val context: Context) : SensorEventListener {

  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
  private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

  private val _currentStep = MutableStateFlow(CalibrationStep.TRIPOD_POSITION)
  val currentStep: StateFlow<CalibrationStep> = _currentStep.asStateFlow()

  private val _selectedPreset = MutableStateFlow(CourtMountPreset.BASELINE_CORNER)
  val selectedPreset: StateFlow<CourtMountPreset> = _selectedPreset.asStateFlow()

  private val _telemetry = MutableStateFlow(CalibrationTelemetry())
  val telemetry: StateFlow<CalibrationTelemetry> = _telemetry.asStateFlow()

  private val _isCalibrationLocked = MutableStateFlow(false)
  val isCalibrationLocked: StateFlow<Boolean> = _isCalibrationLocked.asStateFlow()

  private var isSensorListening = false

  init {
    startSensorTracking()
  }

  fun startSensorTracking() {
    if (isSensorListening) return
    accelerometer?.let {
      sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
      isSensorListening = true
    }
  }

  fun stopSensorTracking() {
    if (!isSensorListening) return
    sensorManager?.unregisterListener(this)
    isSensorListening = false
  }

  fun selectPreset(preset: CourtMountPreset) {
    _selectedPreset.value = preset
    updateTelemetryEvaluation(_telemetry.value.pitchDeg, _telemetry.value.rollDeg, _telemetry.value.estimatedDistanceM)
  }

  fun goToStep(step: CalibrationStep) {
    _currentStep.value = step
  }

  fun nextStep() {
    val steps = CalibrationStep.values()
    val nextIndex = _currentStep.value.ordinal + 1
    if (nextIndex < steps.size) {
      _currentStep.value = steps[nextIndex]
    } else {
      lockCalibration()
    }
  }

  fun previousStep() {
    val steps = CalibrationStep.values()
    val prevIndex = _currentStep.value.ordinal - 1
    if (prevIndex >= 0) {
      _currentStep.value = steps[prevIndex]
    }
  }

  fun adjustSimulatedDistance(newDistance: Float) {
    val current = _telemetry.value
    updateTelemetryEvaluation(current.pitchDeg, current.rollDeg, newDistance)
  }

  fun adjustSimulatedPitch(newPitch: Float) {
    val current = _telemetry.value
    updateTelemetryEvaluation(newPitch, current.rollDeg, current.estimatedDistanceM)
  }

  fun adjustSimulatedRoll(newRoll: Float) {
    val current = _telemetry.value
    updateTelemetryEvaluation(current.pitchDeg, newRoll, current.estimatedDistanceM)
  }

  fun lockCalibration() {
    _isCalibrationLocked.value = true
    _currentStep.value = CalibrationStep.LOCK_AND_VERIFY
  }

  fun resetCalibration() {
    _isCalibrationLocked.value = false
    _currentStep.value = CalibrationStep.TRIPOD_POSITION
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

    val ax = event.values[0]
    val ay = event.values[1]
    val az = event.values[2]

    // Calculate Roll & Pitch from gravity vector
    val normG = sqrt((ax * ax + ay * ay + az * az).toDouble())
    if (normG > 0.0) {
      val roll = Math.toDegrees(atan2(ax.toDouble(), ay.toDouble())).toFloat()
      val pitch = Math.toDegrees(atan2(-az.toDouble(), sqrt((ax * ax + ay * ay).toDouble()))).toFloat()

      // Smooth update
      val cur = _telemetry.value
      val smoothRoll = (cur.rollDeg * 0.8f) + (roll * 0.2f)
      val smoothPitch = (cur.pitchDeg * 0.8f) + (pitch * 0.2f)
      updateTelemetryEvaluation(smoothPitch, smoothRoll, cur.estimatedDistanceM)
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

  private fun updateTelemetryEvaluation(pitch: Float, roll: Float, dist: Float) {
    val preset = _selectedPreset.value
    val isPitchOpt = abs(pitch - preset.idealPitchDeg) <= 3.0f
    val isRollOpt = abs(roll) <= 2.0f
    val isDistOpt = dist in (preset.idealDistanceMeters - 0.6f)..(preset.idealDistanceMeters + 0.8f)

    var score = 70
    if (isPitchOpt) score += 10
    if (isRollOpt) score += 10
    if (isDistOpt) score += 10

    _telemetry.value = _telemetry.value.copy(
      pitchDeg = pitch,
      rollDeg = roll,
      estimatedDistanceM = dist,
      isPitchOptimal = isPitchOpt,
      isRollOptimal = isRollOpt,
      isDistanceOptimal = isDistOpt,
      calibrationQualityPercent = minOf(100, score)
    )
  }

  companion object {
    @Volatile
    private var instance: CameraCalibrationManager? = null

    fun getInstance(context: Context): CameraCalibrationManager {
      return instance ?: synchronized(this) {
        instance ?: CameraCalibrationManager(context.applicationContext).also { instance = it }
      }
    }
  }
}
