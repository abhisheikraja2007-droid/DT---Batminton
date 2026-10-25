package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AutoVideoClippingEngine
import com.example.model.BadmintonPoseEngine
import com.example.model.CalibrationStep
import com.example.model.CameraCalibrationManager
import com.example.model.CourtMountPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CameraCalibrationAndClippingTest {

  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
  }

  @Test
  fun testCameraCalibrationManagerLifecycleAndSteps() {
    val manager = CameraCalibrationManager.getInstance(context)
    assertNotNull(manager)

    // Reset to baseline
    manager.resetCalibration()
    assertEquals(CalibrationStep.TRIPOD_POSITION, manager.currentStep.value)
    assertFalse(manager.isCalibrationLocked.value)

    // Step progression through all 5 calibration stages
    manager.nextStep()
    assertEquals(CalibrationStep.TILT_AND_PITCH, manager.currentStep.value)

    manager.nextStep()
    assertEquals(CalibrationStep.DISTANCE_AND_FRAME, manager.currentStep.value)

    manager.nextStep()
    assertEquals(CalibrationStep.SWING_ENVELOPE_CHECK, manager.currentStep.value)

    manager.nextStep()
    assertEquals(CalibrationStep.LOCK_AND_VERIFY, manager.currentStep.value)

    // Previous step check
    manager.previousStep()
    assertEquals(CalibrationStep.SWING_ENVELOPE_CHECK, manager.currentStep.value)

    // Direct step jump
    manager.goToStep(CalibrationStep.TILT_AND_PITCH)
    assertEquals(CalibrationStep.TILT_AND_PITCH, manager.currentStep.value)

    // Lock calibration
    manager.lockCalibration()
    assertTrue(manager.isCalibrationLocked.value)
    assertEquals(CalibrationStep.LOCK_AND_VERIFY, manager.currentStep.value)
  }

  @Test
  fun testCameraCalibrationPresetsAndTelemetry() {
    val manager = CameraCalibrationManager.getInstance(context)

    // Select preset
    manager.selectPreset(CourtMountPreset.BASELINE_CORNER)
    assertEquals(CourtMountPreset.BASELINE_CORNER, manager.selectedPreset.value)
    assertEquals(4.2f, manager.selectedPreset.value.idealDistanceMeters)

    manager.selectPreset(CourtMountPreset.SIDE_COURT_T)
    assertEquals(CourtMountPreset.SIDE_COURT_T, manager.selectedPreset.value)

    // Adjust simulated distance and tilt
    manager.adjustSimulatedDistance(5.0f)
    assertEquals(5.0f, manager.telemetry.value.estimatedDistanceM, 0.01f)
    assertTrue(manager.telemetry.value.isDistanceOptimal)

    manager.adjustSimulatedRoll(0.1f)
    assertEquals(0.1f, manager.telemetry.value.rollDeg, 0.01f)
    assertTrue(manager.telemetry.value.isRollOptimal)

    manager.adjustSimulatedPitch(2.5f)
    assertEquals(2.5f, manager.telemetry.value.pitchDeg, 0.01f)
    assertTrue(manager.telemetry.value.isPitchOptimal)
  }

  @Test
  fun testAutoVideoClippingEngineExtraction() {
    val clippingEngine = AutoVideoClippingEngine.getInstance(context)
    assertNotNull(clippingEngine)

    // Extract a high efficiency jump smash clip
    val clip = clippingEngine.evaluateAndExtractMoment(
      repNumber = 4,
      strokeType = "Forehand Jump Smash",
      efficiencyScore = 94,
      speedKmH = 342,
      elbowAngle = 176f,
      reachMeters = 2.68f,
      kineticPhase = "Impact Zenith Strike",
      faultAlert = null
    )

    assertNotNull(clip)
    assertEquals(4, clip!!.repIndex)
    assertEquals("Forehand Jump Smash", clip.strokeType)
    assertEquals(94, clip.efficiencyScore)
    assertTrue(clippingEngine.extractedClips.value.isNotEmpty())

    // Test exporting clip to storage
    clippingEngine.exportClipToStorage(clip.id) { success, path ->
      assertTrue(success)
      assertTrue(path.isNotEmpty())
    }
  }

  @Test
  fun testBadmintonPoseEngineSynthesis() {
    val initialPose = BadmintonPoseEngine.generateAnimatedPose(0f)
    assertNotNull(initialPose)
    assertNotNull(initialPose.nose)
    assertNotNull(initialPose.rightWrist)
    assertNotNull(initialPose.racketTip)
    assertTrue(initialPose.kineticPhase.isNotEmpty())

    val impactPose = BadmintonPoseEngine.generateAnimatedPose(1.4f)
    assertNotNull(impactPose)
    assertTrue(impactPose.elbowAngleDeg > 0f)
  }
}
