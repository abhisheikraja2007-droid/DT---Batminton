package com.example.model

import java.util.UUID

/**
 * High-priority anatomical focus areas for badminton injury prevention and dynamic warmups.
 */
enum class WarmupFocusArea(
  val id: String,
  val displayName: String,
  val shortLabel: String,
  val iconEmoji: String,
  val primaryInjuryTarget: String,
  val description: String,
  val badgeHex: Long
) {
  WRIST_FLEXIBILITY(
    id = "wrist",
    displayName = "Wrist & Forearm Pronation",
    shortLabel = "Wrist & Forearm",
    iconEmoji = "🖐️",
    primaryInjuryTarget = "Prevent TFCC strains, carpal compression & forearm tendinitis",
    description = "Prepares wrist extensors and flexors for high-velocity pronation snap during smashes and slices.",
    badgeHex = 0xFF006C4C // CourtGreen
  ),
  SHOULDER_MOBILITY(
    id = "shoulder",
    displayName = "Shoulder & Rotator Cuff",
    shortLabel = "Shoulder & Cuff",
    iconEmoji = "💪",
    primaryInjuryTarget = "Prevent rotator cuff impingement, labrum tears & subscapularis fatigue",
    description = "Dynamic scapular retraction, glenohumeral internal/external rotation to safeguard overhead power.",
    badgeHex = 0xFF9E4700 // ShuttleOrange
  ),
  ANKLE_KNEE_AGILITY(
    id = "ankle_knee",
    displayName = "Ankle & Knee Agility",
    shortLabel = "Ankle & Knee",
    iconEmoji = "⚡",
    primaryInjuryTarget = "Prevent patellar tendinitis (jumper's knee) & lateral ankle sprains",
    description = "Multi-directional joint mobilization, Achilles elasticity, and meniscus shock-absorption conditioning.",
    badgeHex = 0xFF2563EB // Blue
  ),
  THORACIC_HIP_ROTATION(
    id = "thoracic_hip",
    displayName = "Thoracic Spine & Hip Kinetic Chain",
    shortLabel = "Thoracic & Hips",
    iconEmoji = "🌀",
    primaryInjuryTarget = "Prevent lumbar hyperextension, groin strains & SI joint overload",
    description = "Activates rotational core torque and deep hip flexor length for seamless kinetic smash uncoiling.",
    badgeHex = 0xFF7C3AED // Purple
  ),
  FULL_BODY_DYNAMIC(
    id = "full_body",
    displayName = "Full Body Match Activation",
    shortLabel = "Full Body",
    iconEmoji = "🏸",
    primaryInjuryTarget = "Comprehensive full kinetic chain neuromuscular firing & heart rate elevation",
    description = "Complete 5-minute dynamic protocol covering ankles, hips, shoulders, and wrists before intense play.",
    badgeHex = 0xFFD97706 // Amber
  )
}

/**
 * Single dynamic stretching exercise tailored for badminton movement patterns.
 */
data class DynamicExercise(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val focusArea: WarmupFocusArea,
  val targetMuscles: String,
  val durationSeconds: Int,
  val repRecommendation: String,
  val injuryPreventionFocus: String,
  val stepInstructions: List<String>,
  val commonMistakesToAvoid: String,
  val proTip: String,
  val intensityLevel: String = "Moderate Dynamic",
  val animationKey: String = "pulse_rotate"
)

/**
 * Complete structured warmup routine generated for a specific focus area or duration.
 */
data class WarmupRoutinePlan(
  val id: String,
  val title: String,
  val focusArea: WarmupFocusArea,
  val totalDurationMinutes: Int,
  val totalExercises: Int,
  val exercises: List<DynamicExercise>,
  val summaryBenefit: String,
  val recommendedForDrill: String
)

/**
 * Repository and generator for Badminton Dynamic Warmup Routines.
 */
object WarmupRoutineRepository {

  val allExercises: List<DynamicExercise> = listOf(
    // WRIST FLEXIBILITY EXERCISES
    DynamicExercise(
      id = "wrist_figure_eights",
      name = "Dynamic Racket Figure-8 Rolls",
      focusArea = WarmupFocusArea.WRIST_FLEXIBILITY,
      targetMuscles = "Extensor carpi radialis, flexor digitorum, pronator teres",
      durationSeconds = 45,
      repRecommendation = "20 continuous loops per hand",
      injuryPreventionFocus = "Lubricates synovial carpal capsules to prevent sharp wrist hyperextension sprains at smash apex.",
      stepInstructions = listOf(
        "Hold racket or loosely clasped hands in front of chest at 90° elbow flexion.",
        "Trace smooth, fluid infinity figure-8 patterns with the racket head or knuckles.",
        "Gradually accelerate from slow rotation into rapid whip snaps, alternating clockwise and counterclockwise.",
        "Keep shoulders relaxed and let the forearm pronators drive the rotational momentum."
      ),
      commonMistakesToAvoid = "Locking the forearm rigid instead of allowing natural wrist uncoiling.",
      proTip = "Emulate the late wrist snap used when defending crosscourt net drops.",
      intensityLevel = "Low-Impact Dynamic"
    ),
    DynamicExercise(
      id = "wrist_prayer_extension",
      name = "Reverse Prayer Dynamic Pumps",
      focusArea = WarmupFocusArea.WRIST_FLEXIBILITY,
      targetMuscles = "Palmaris longus, carpal tunnel ligaments, flexor retinaculum",
      durationSeconds = 40,
      repRecommendation = "15 dynamic pulsing repetitions",
      injuryPreventionFocus = "Prevents chronic badminton golfer's/tennis elbow and carpal nerve entrapment.",
      stepInstructions = listOf(
        "Bring palms together in front of sternum in prayer position, elbows flared wide.",
        "Slowly lower wrists downward toward navel until deep stretch is felt in forearm flexors.",
        "Pulse up and down gently for 2 seconds, then invert backs of hands together and press upward.",
        "Breathe deeply, keeping chest proud and neck tension-free."
      ),
      commonMistakesToAvoid = "Holding static tension instead of pulsing dynamically with the breath.",
      proTip = "Shake wrists vigorously for 5 seconds between sets to promote blood microcirculation."
    ),
    DynamicExercise(
      id = "wrist_finger_flicks",
      name = "Explosive Starburst Finger Extensions",
      focusArea = WarmupFocusArea.WRIST_FLEXIBILITY,
      targetMuscles = "Lumbricals, interossei, superficial flexor tendons",
      durationSeconds = 30,
      repRecommendation = "30 fast flick bursts",
      injuryPreventionFocus = "Primes finger grip torque for sudden racket handle micro-adjustments during deceptive slices.",
      stepInstructions = listOf(
        "Extend arms straight out in front at shoulder height with fists tightly clenched.",
        "Explosively open fingers as wide as possible into a starburst extension.",
        "Immediately snap back into tight fists and repeat at rapid tempo.",
        "Perform with palms facing down for 15 seconds, then palms facing up for 15 seconds."
      ),
      commonMistakesToAvoid = "Dropping hands below chest line as forearms fatigue.",
      proTip = "Feel the burning sensation in forearm extensors; this triggers neuromuscular readiness."
    ),

    // SHOULDER MOBILITY EXERCISES
    DynamicExercise(
      id = "shoulder_arm_circles_armoury",
      name = "Multi-Plane Windmill Rotations",
      focusArea = WarmupFocusArea.SHOULDER_MOBILITY,
      targetMuscles = "Anterior/posterior deltoids, supraspinatus, serratus anterior",
      durationSeconds = 50,
      repRecommendation = "12 large circles forward, 12 backward",
      injuryPreventionFocus = "Expands subacromial space to stop painful overhead tendon pinching during steep jump smashes.",
      stepInstructions = listOf(
        "Stand tall with athletic badminton split-step stance, core lightly braced.",
        "Sweep dominant racket arm upward in a wide vertical arc, reaching maximum overhead ceiling height.",
        "Scrape the outer boundary of your mobility envelope without arching the lower back.",
        "Switch into backward windmills, emphasizing scapular pinching at the bottom of the arc."
      ),
      commonMistakesToAvoid = "Arching lower back to compensate for tight shoulder flexion.",
      proTip = "Imagine scraping the wall with your racket head to enforce true overhead range."
    ),
    DynamicExercise(
      id = "shoulder_crossover_sweep",
      name = "Cross-Body Scapular Sweeps & Hugs",
      focusArea = WarmupFocusArea.SHOULDER_MOBILITY,
      targetMuscles = "Infraspinatus, teres minor, rhomboids, middle trapezius",
      durationSeconds = 45,
      repRecommendation = "15 alternating hug pulses",
      injuryPreventionFocus = "Prevents posterior cuff tears and stabilizes deceleration after 300+ km/h smash swings.",
      stepInstructions = listOf(
        "Flap arms wide to sides with thumbs pointing upward to open chest.",
        "Cross arms vigorously across chest in a bear hug, alternating which arm lands on top.",
        "Reach fingertips as far around shoulder blades as possible on each pulse.",
        "Bounce rhythmically on the balls of your feet to engage court rhythm."
      ),
      commonMistakesToAvoid = "Shrugging shoulders into ears; keep traps depressed and neck long.",
      proTip = "Synchronize the open expansion with a full inhale and cross-hug with an exhale."
    ),
    DynamicExercise(
      id = "shoulder_y_t_w_raises",
      name = "Kinetic Y-T-W Rotator Activators",
      focusArea = WarmupFocusArea.SHOULDER_MOBILITY,
      targetMuscles = "Lower trapezius, external rotators, rear delts",
      durationSeconds = 50,
      repRecommendation = "8 pulses in Y, 8 in T, 8 in W position",
      injuryPreventionFocus = "Activates the posterior kinetic stabilizers that prevent forward shoulder slump and impingement.",
      stepInstructions = listOf(
        "Hinge forward 30° at hips with flat back, athletic soft knees.",
        "Form a 'Y' with arms overhead, thumbs pointing toward ceiling; pulse upward twice.",
        "Lower to horizontal 'T' with palms facing forward; pinch shoulder blades firmly.",
        "Pull elbows down into 'W' alongside ribs, externally rotating forearms backward."
      ),
      commonMistakesToAvoid = "Rounding lumbar spine; maintain neutral hip hinge.",
      proTip = "Crucial exercise if you spend hours at a desk before stepping onto the badminton court."
    ),

    // ANKLE & KNEE AGILITY EXERCISES
    DynamicExercise(
      id = "ankle_court_pogo_hops",
      name = "Elastic Pogo Bounds & Plantar Priming",
      focusArea = WarmupFocusArea.ANKLE_KNEE_AGILITY,
      targetMuscles = "Gastrocnemius, soleus, Achilles tendon complex, tibialis anterior",
      durationSeconds = 45,
      repRecommendation = "30 light rhythmic hops with split-step micro-bounces",
      injuryPreventionFocus = "Preheats the Achilles spring mechanism to prevent catastrophic ruptures and acute ankle rolls.",
      stepInstructions = listOf(
        "Stand tall on balls of feet, heels elevated 2cm off ground.",
        "Hop rhythmically with minimal knee bend, relying purely on ankle tendon elasticity.",
        "Keep ground contact time as short as possible (< 150ms).",
        "Every 5th hop, land in a wide badminton defensive ready-stance before bouncing back up."
      ),
      commonMistakesToAvoid = "Landing flat-footed with loud impact thuds.",
      proTip = "Maintain whisper-quiet feet on court wood to cultivate elite agility."
    ),
    DynamicExercise(
      id = "knee_deep_lunges_reach",
      name = "Multi-Directional Lunge & Overhead Reach",
      focusArea = WarmupFocusArea.ANKLE_KNEE_AGILITY,
      targetMuscles = "Quadriceps, patellar tendon, gluteus medius, hip abductors",
      durationSeconds = 50,
      repRecommendation = "6 lunges forward-right, 6 lunges forward-left",
      injuryPreventionFocus = "Conditions the patellar tendon for high-force deceleration when recovering from net kills.",
      stepInstructions = listOf(
        "Lunge diagonally forward with right leg as if lunging to retrieve a tight net tumble.",
        "Ensure right knee tracks directly over middle toes, keeping heel flat upon landing.",
        "Simultaneously reach both arms diagonally overhead to lengthen hip flexor.",
        "Push forcefully off front heel to return cleanly to baseline ready position."
      ),
      commonMistakesToAvoid = "Allowing front knee to collapse inward past the big toe.",
      proTip = "Absorb landing impact through glutes and hamstring rather than slamming the knee."
    ),

    // THORACIC & HIP ROTATION EXERCISES
    DynamicExercise(
      id = "thoracic_open_books",
      name = "Standing Torso Rotational Coils",
      focusArea = WarmupFocusArea.THORACIC_HIP_ROTATION,
      targetMuscles = "Internal/external obliques, thoracic erector spinae, piriformis",
      durationSeconds = 45,
      repRecommendation = "16 alternating rotational sweeps",
      injuryPreventionFocus = "Prevents acute oblique muscle strains and facet joint locking during violent jump smash torquing.",
      stepInstructions = listOf(
        "Hold hands at chest height with elbows bent like a defensive boxer.",
        "Rotate shoulders aggressively 90° to the right, pivoting on the rear left foot ball.",
        "Feel the kinetic coil wind up through hips, abdomen, and mid-back.",
        "Uncoil and snap rotation smoothly to the left, simulating smash preparation into follow-through."
      ),
      commonMistakesToAvoid = "Keeping feet glued flat, which twists the knees destructively.",
      proTip = "Always pivot the rear ball of foot to protect knee ligaments during rotational strikes."
    ),
    DynamicExercise(
      id = "hip_gate_openers_closers",
      name = "Dynamic Hip Hurdles (Open & Close)",
      focusArea = WarmupFocusArea.THORACIC_HIP_ROTATION,
      targetMuscles = "Psoas major, tensor fasciae latae, adductor longus",
      durationSeconds = 45,
      repRecommendation = "10 outward gates + 10 inward gates per leg",
      injuryPreventionFocus = "Unlocks pelvic mobility to prevent adductor (groin) tears during sudden backward shadow footwork.",
      stepInstructions = listOf(
        "Lift right knee to 90° waist height in front of body.",
        "Rotate thigh outward in a wide semicircular arc over an imaginary hurdle and step down.",
        "Reverse direction by sweeping thigh from wide lateral position back inward across body midline.",
        "Alternate legs with a rhythmic active skip step between reps."
      ),
      commonMistakesToAvoid = "Leaning torso sideways to cheat knee elevation.",
      proTip = "Imagine clearing a badminton net post with your kneecap on every single stride."
    )
  )

  /**
   * Generates tailored warmup routine plan based on chosen focus area.
   */
  fun getRoutineForFocusArea(focusArea: WarmupFocusArea): WarmupRoutinePlan {
    val primaryExercises = allExercises.filter { it.focusArea == focusArea }
    val secondaryExercises = allExercises.filter { it.focusArea != focusArea }.take(2)
    val routineList = (primaryExercises + secondaryExercises).distinctBy { it.id }

    val durationMin = (routineList.sumOf { it.durationSeconds } + 30) / 60

    val (title, benefit, recommendedDrill) = when (focusArea) {
      WarmupFocusArea.WRIST_FLEXIBILITY -> Triple(
        "Wrist Snap & Forearm Shield Protocol",
        "Primes high-velocity racket whip, unlocks deceptive finger slices, and shields the wrist against carpal collision forces.",
        "Ideal before Steep Forehand Smash, Deceptive Net Drops & Backhand Clears"
      )
      WarmupFocusArea.SHOULDER_MOBILITY -> Triple(
        "Rotator Cuff Armor & Zenith Reach",
        "Widens subacromial overhead envelope, ignites external rotators, and safeguards the shoulder labrum for maximum smash velocity.",
        "Essential prior to Jump Smash Training, Half-Smash Drills & Defensive Flat Drives"
      )
      WarmupFocusArea.ANKLE_KNEE_AGILITY -> Triple(
        "Lower Kinetic Spring & Tendon Agility",
        "Preheats Achilles elasticity, stabilizes lateral ankle ligaments, and prepares patellar tendons for brutal lunging brakes.",
        "Recommended before 6-Corner Shadow Footwork & Net Kill Rush Drills"
      )
      WarmupFocusArea.THORACIC_HIP_ROTATION -> Triple(
        "Torque Matrix: Thoracic & Hip Coil",
        "Unlocks rotational core elasticity, coordinates hip-shoulder lag, and shields the lower lumbar spine during full jump uncoils.",
        "Best before Full-Court Matchplay, Attacking Singles Drills & Fast Multi-Shuttle Feeds"
      )
      WarmupFocusArea.FULL_BODY_DYNAMIC -> Triple(
        "Comprehensive 360° Match Activation",
        "Complete kinetic cascade from ankles to racket tip, raising core temperature and sharpening nervous system reaction latency.",
        "Universal pre-competition and high-intensity club session activation"
      )
    }

    return WarmupRoutinePlan(
      id = "routine_${focusArea.id}",
      title = title,
      focusArea = focusArea,
      totalDurationMinutes = maxOf(3, durationMin),
      totalExercises = routineList.size,
      exercises = routineList,
      summaryBenefit = benefit,
      recommendedForDrill = recommendedDrill
    )
  }
}
