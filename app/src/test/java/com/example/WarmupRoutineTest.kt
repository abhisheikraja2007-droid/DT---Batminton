package com.example

import com.example.model.WarmupFocusArea
import com.example.model.WarmupRoutineRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WarmupRoutineTest {

  @Test
  fun testWarmupFocusAreasAndAllExercisesRepository() {
    val exercises = WarmupRoutineRepository.allExercises
    assertTrue("Exercises repository should have dynamic stretches", exercises.size >= 8)

    // Verify all focus areas have exercises
    WarmupFocusArea.values().forEach { area ->
      assertNotNull(area.displayName)
      assertNotNull(area.primaryInjuryTarget)
      assertTrue(area.primaryInjuryTarget.isNotEmpty())
    }
  }

  @Test
  fun testWristFlexibilityRoutinePlanGeneration() {
    val wristPlan = WarmupRoutineRepository.getRoutineForFocusArea(WarmupFocusArea.WRIST_FLEXIBILITY)
    assertNotNull(wristPlan)
    assertEquals(WarmupFocusArea.WRIST_FLEXIBILITY, wristPlan.focusArea)
    assertTrue(wristPlan.exercises.isNotEmpty())
    assertTrue(wristPlan.totalDurationMinutes >= 3)
    assertTrue(wristPlan.title.contains("Wrist", ignoreCase = true))

    // Check that primary exercises target wrist/forearm
    val hasWristExercise = wristPlan.exercises.any { it.focusArea == WarmupFocusArea.WRIST_FLEXIBILITY }
    assertTrue(hasWristExercise)
  }

  @Test
  fun testShoulderMobilityRoutinePlanGeneration() {
    val shoulderPlan = WarmupRoutineRepository.getRoutineForFocusArea(WarmupFocusArea.SHOULDER_MOBILITY)
    assertNotNull(shoulderPlan)
    assertEquals(WarmupFocusArea.SHOULDER_MOBILITY, shoulderPlan.focusArea)
    assertTrue(shoulderPlan.exercises.isNotEmpty())
    assertTrue(shoulderPlan.title.contains("Rotator Cuff", ignoreCase = true) || shoulderPlan.title.contains("Shoulder", ignoreCase = true))

    val hasShoulderExercise = shoulderPlan.exercises.any { it.focusArea == WarmupFocusArea.SHOULDER_MOBILITY }
    assertTrue(hasShoulderExercise)
  }

  @Test
  fun testAnkleKneeAndThoracicRoutines() {
    val anklePlan = WarmupRoutineRepository.getRoutineForFocusArea(WarmupFocusArea.ANKLE_KNEE_AGILITY)
    assertNotNull(anklePlan)
    assertEquals(WarmupFocusArea.ANKLE_KNEE_AGILITY, anklePlan.focusArea)

    val thoracicPlan = WarmupRoutineRepository.getRoutineForFocusArea(WarmupFocusArea.THORACIC_HIP_ROTATION)
    assertNotNull(thoracicPlan)
    assertEquals(WarmupFocusArea.THORACIC_HIP_ROTATION, thoracicPlan.focusArea)
  }

  @Test
  fun testExerciseDetailsAndInjuryPreventionGuidelines() {
    val firstEx = WarmupRoutineRepository.allExercises.first()
    assertNotNull(firstEx.name)
    assertNotNull(firstEx.targetMuscles)
    assertTrue(firstEx.durationSeconds > 0)
    assertNotNull(firstEx.injuryPreventionFocus)
    assertTrue(firstEx.stepInstructions.isNotEmpty())
    assertNotNull(firstEx.proTip)
  }
}
