package com.example.model

enum class DrillCategory(val label: String, val icon: String) {
  ALL("All Drills", "🏸"),
  SMASH_POWER("Smash & Power", "⚡"),
  NET_DROPS("Net & Drops", "🎯"),
  DEFENSE_CLEARS("Defense & Clears", "🛡️"),
  FOOTWORK_AGILITY("Footwork & Agility", "👟"),
  DRIVE_DECEPTION("Drives & Deception", "🪄"),
  SERVE_TACTICS("Serve & Tactics", "🏹")
}

enum class DrillDifficulty(val label: String, val levelNumber: Int) {
  ALL("All Levels", 0),
  BEGINNER("Beginner", 1),
  INTERMEDIATE("Intermediate", 2),
  ADVANCED("Advanced", 3),
  ELITE_PRO("Elite Pro", 4)
}

data class TargetSkillMetric(
  val name: String,
  val targetValue: String,
  val unit: String,
  val benchmarkComparison: String,
  val description: String
)

data class DrillStepInstruction(
  val stepNumber: Int,
  val title: String,
  val instruction: String,
  val biomechanicalFocus: String,
  val coachCue: String
)

data class CommonBiomechanicalFault(
  val faultTitle: String,
  val description: String,
  val voiceCorrectionCue: String
)

data class StructuredDrill(
  val id: String,
  val title: String,
  val subtitle: String,
  val category: DrillCategory,
  val difficulty: DrillDifficulty,
  val durationMinutes: Int,
  val targetReps: Int,
  val estimatedCalories: Int,
  val equipment: List<String>,
  val summary: String,
  val targetSkillMetrics: List<TargetSkillMetric>,
  val steps: List<DrillStepInstruction>,
  val commonFaults: List<CommonBiomechanicalFault>,
  val badgeLabel: String? = null,
  val imageUrl: String = AceImages.DRILL_POSTER,
  val personalBest: String = "Not recorded"
)

object StructuredDrillCatalog {
  val drills: List<StructuredDrill> = listOf(
    StructuredDrill(
      id = "drill_zenith_smash",
      title = "Zenith Jump Smash & Forearm Pronation",
      subtitle = "High apex contact elevation with maximum kinetic uncoil",
      category = DrillCategory.SMASH_POWER,
      difficulty = DrillDifficulty.ADVANCED,
      durationMinutes = 25,
      targetReps = 20,
      estimatedCalories = 185,
      equipment = listOf("Racket", "Feather Shuttles", "Full Court", "Speed Radar"),
      summary = "Focuses on explosive vertical elevation, late shoulder-elbow lag, and steep downward smash trajectory with violent forearm pronation.",
      badgeLabel = "HOT ROUTINE 🔥",
      imageUrl = AceImages.DRILL_POSTER,
      personalBest = "242 km/h • 92% Form",
      targetSkillMetrics = listOf(
        TargetSkillMetric("Contact Zenith Height", "≥ 2.88", "m", "+12cm over standard", "Highest reach point before downward snap"),
        TargetSkillMetric("Smash Exit Velocity", "≥ 240", "km/h", "Tier-1 Collegiate", "Measured at shuttlecock string uncoil"),
        TargetSkillMetric("Elbow Cocking Angle", "135° - 150°", "deg", "Optimal kinematic range", "Elbow held high above shoulder level before hip drive"),
        TargetSkillMetric("Sweet Spot Grouping", "≥ 85", "%", "+15% vs baseline", "Center racket string face contact ratio")
      ),
      steps = listOf(
        DrillStepInstruction(
          1,
          "Split-Step & Rear Scissor Prep",
          "Perform explosive split step on shuttlecock launch. Pivot dominant foot back at a 45° angle while extending the non-racket arm up for optical tracking.",
          "Weight on rear ball of foot, thoracic spine primed for torque.",
          "Split, pivot, non-racket arm tracking high!"
        ),
        DrillStepInstruction(
          2,
          "Vertical Apex Elevation & Cocking",
          "Explode vertically off both feet. Cock hitting elbow to 140° behind the head while keeping chest open to the side tramline.",
          "Chest perpendicular to net, creating elastic torso pre-stretch.",
          "Coil hips, elevate elbow above shoulder line!"
        ),
        DrillStepInstruction(
          3,
          "Kinetic Uncoil & Steep Contact",
          "Fire hips forward first, drag elbow forward, then unleash internal shoulder rotation with rapid forearm pronation at 2.88m apex.",
          "Contact shuttle 30cm in front of hitting shoulder at maximum reach.",
          "Hip drive first, snap wrist and pronate at highest peak!"
        ),
        DrillStepInstruction(
          4,
          "Scissor Kick Landing & T-Hub Recovery",
          "Rear foot scissors forward to absorb landing impact. Push off instantly with dominant leg back to the central court T.",
          "Deceleration control preventing knee shear strain.",
          "Cushion landing, scissor forward, recover central T!"
        )
      ),
      commonFaults = listOf(
        CommonBiomechanicalFault(
          "Dropped Hitting Elbow",
          "Hitting elbow drops below shoulder plane, reducing smash power by 28% and risking rotator cuff strain.",
          "Fault: Keep hitting elbow raised above 135 degrees during prep."
        ),
        CommonBiomechanicalFault(
          "Late Hip Uncoil",
          "Torso rotates before pelvis fires, destroying kinetic chain whip.",
          "Fault: Drive rear hip forward before swinging the racket arm."
        )
      )
    ),

    StructuredDrill(
      id = "drill_crosscourt_drop",
      title = "Crosscourt Fast Slice Drop & Deception",
      subtitle = "Disguised overhead preparation with sharp angle drop",
      category = DrillCategory.NET_DROPS,
      difficulty = DrillDifficulty.INTERMEDIATE,
      durationMinutes = 20,
      targetReps = 24,
      estimatedCalories = 140,
      equipment = listOf("Racket", "Feather Shuttles", "Front Court"),
      summary = "Mimic full jump smash body posture, then decelerate racket face with a 30° slice across the feathers to tumble the shuttle over the white tape.",
      badgeLabel = "DECEPTION FOCUS 🪄",
      imageUrl = AceImages.SUMMARY_REP_HIGHLIGHT,
      personalBest = "86% Hold Score",
      targetSkillMetrics = listOf(
        TargetSkillMetric("Preparation Disguise", "≥ 90", "%", "Matches smash silhouette", "Identical visual cueing until 15ms prior to impact"),
        TargetSkillMetric("Net Clearance Margin", "≤ 15", "cm", "Tight apex", "Height above net tape at apex crossing"),
        TargetSkillMetric("Shuttle Deceleration", "≥ 60", "%", "Abrupt speed drop", "Speed reduction from backcourt to service line")
      ),
      steps = listOf(
        DrillStepInstruction(
          1,
          "Aggressive Full-Smash Silhouette",
          "Adopt full jump smash preparation stance. Raise both arms high, arch lower back to convince opponent of a hard smash.",
          "Body posture perfectly mirrors offensive smash silhouette.",
          "Show smash! High posture, full shoulder arch!"
        ),
        DrillStepInstruction(
          2,
          "Controlled Racket Slicing Action",
          "Instead of flat pronation, carve across the side feathers at a 30° oblique angle, imparting aerodynamic tumble.",
          "Soft grip tension (3/10) right before contact.",
          "Slice the side feathers! Soft grip, sharp angle!"
        ),
        DrillStepInstruction(
          3,
          "Immediate Frontcourt Interception Move",
          "Follow shuttle trajectory immediately forward with low center of gravity to pounce on defensive tumbling returns.",
          "Two forward recovery lunges maintaining balance.",
          "Forward press! Hunt the net kill!"
        )
      ),
      commonFaults = listOf(
        CommonBiomechanicalFault(
          "Slowing Down Arm Speed Early",
          "Slowing arm swing too early reveals the drop shot to the defender.",
          "Fault: Maintain full swing speed until 10cm before contact."
        )
      )
    ),

    StructuredDrill(
      id = "drill_deep_high_clear",
      title = "Deep Backcourt High Clear & Overhead Arc",
      subtitle = "Full-length defensive clearing with 2.90m contact apex",
      category = DrillCategory.DEFENSE_CLEARS,
      difficulty = DrillDifficulty.INTERMEDIATE,
      durationMinutes = 22,
      targetReps = 25,
      estimatedCalories = 160,
      equipment = listOf("Racket", "Shuttlecocks", "Rear Court Target Cones"),
      summary = "Engineers maximum height trajectory to push aggressive opponents deep into their rear tramlines while resetting court positioning.",
      badgeLabel = "TACTICAL RESET 🛡️",
      imageUrl = AceImages.TRAIN_ATHLETE_BG,
      personalBest = "91% Baseline Depth",
      targetSkillMetrics = listOf(
        TargetSkillMetric("Baseline Tramline Depth", "≥ 85", "%", "Within rear tramline box", "Proportion of shuttles landing within rear 76cm"),
        TargetSkillMetric("Flight Apex Trajectory", "≥ 6.5", "m", "High defensive flight", "Maximum vertical altitude during shuttle trajectory"),
        TargetSkillMetric("Contact Zenith Apex", "≥ 2.85", "m", "+8cm reach", "High impact prevents undercutting")
      ),
      steps = listOf(
        DrillStepInstruction(
          1,
          "Early Rear-Court Tracking",
          "Push off decisively with chasse steps into rear corner. Align dominant shoulder beneath oncoming shuttlecock trajectory.",
          "Head up, chin tilted, stable dynamic footing.",
          "Quick chasse steps, get behind the shuttle early!"
        ),
        DrillStepInstruction(
          2,
          "Full Overhead Arc Extension",
          "Swing through with high extension arc. Connect with flat string face at 12 o'clock, releasing kinetic energy upwards.",
          "Smooth continuous swing path without hitching.",
          "Drive upward through contact, reach for the ceiling!"
        ),
        DrillStepInstruction(
          3,
          "High Follow-Through & T-Reset",
          "Allow racket to finish naturally across non-dominant hip. Take dynamic step forward into the central defensive hub.",
          "Eyes immediately scanning opponent court response.",
          "Smooth follow-through, recover defensive stance!"
        )
      ),
      commonFaults = listOf(
        CommonBiomechanicalFault(
          "Contact Behind Body Plane",
          "Hitting shuttle behind head causes flat midcourt return ripe for opponent smash.",
          "Fault: Move further back! Strike shuttle in front of your head."
        )
      )
    ),

    StructuredDrill(
      id = "drill_6corner_footwork",
      title = "Explosive 6-Corner Shadow Footwork & Split Step",
      subtitle = "Multi-directional agility training with sub-0.40s recovery",
      category = DrillCategory.FOOTWORK_AGILITY,
      difficulty = DrillDifficulty.ADVANCED,
      durationMinutes = 30,
      targetReps = 36,
      estimatedCalories = 240,
      equipment = listOf("Agility Markers", "Full Court", "Interval Timer"),
      summary = "Rigorous computer-vision tracked shadow footwork covering both net corners, midcourt defense stations, and deep rear corners with rapid T-hub resets.",
      badgeLabel = "CARDIO BEAST 🔥",
      imageUrl = AceImages.COURT_ALIGNMENT_BG,
      personalBest = "0.38s Split Latency",
      targetSkillMetrics = listOf(
        TargetSkillMetric("Split-Step Reaction Delay", "≤ 0.40", "sec", "National standard", "Time from cue to explosive multi-directional departure"),
        TargetSkillMetric("T-Hub Return Precision", "≤ 25", "cm", "Central anchor", "Distance from designated central court anchor point"),
        TargetSkillMetric("Low Stance Center of Mass", "42° - 48°", "deg", "Optimal knee bend", "Knee angle during split-step landing")
      ),
      steps = listOf(
        DrillStepInstruction(
          1,
          "Active Split-Step Bounce at T",
          "Bounce lightly on the balls of your feet at the center T. Perform split-step with feet wider than shoulders upon visual signal.",
          "Core braced, chest proud, knees loaded like springs.",
          "Split bounce on toes, explode on the flash!"
        ),
        DrillStepInstruction(
          2,
          "Two-Step Lunge to Front Net",
          "Chasse step forward right, deep terminal lunge with dominant knee tracking directly over toes. Extend racket to tap tape.",
          "Non-dominant foot anchors behind to brake inertia.",
          "Deep stable lunge, heel-to-toe roll, brake hard!"
        ),
        DrillStepInstruction(
          3,
          "Springboard Push-Off to T",
          "Push powerfully off leading front foot, returning to central T within two fluid recovery steps. Reset split-step rhythm.",
          "Immediate visual re-centering on opponent court.",
          "Spring back, instant reset to central T!"
        )
      ),
      commonFaults = listOf(
        CommonBiomechanicalFault(
          "Knee Valgus on Lunge",
          "Leading knee collapses inward during deep lunge, risking ACL strain.",
          "Fault: Keep knee aligned directly above your ankle and second toe."
        )
      )
    ),

    StructuredDrill(
      id = "drill_flat_drive_reflex",
      title = "Rapid Flat Drive Counter & Interception",
      subtitle = "High-speed waist-height exchanges with compact swing mechanics",
      category = DrillCategory.DRIVE_DECEPTION,
      difficulty = DrillDifficulty.INTERMEDIATE,
      durationMinutes = 18,
      targetReps = 30,
      estimatedCalories = 135,
      equipment = listOf("Racket", "Feather Shuttles", "Midcourt Feeder"),
      summary = "Trains ultra-compact forearm pronation and supination without full backswing, dominating fast doubles midcourt exchanges.",
      badgeLabel = "REFLEX ACCELERATOR ⚡",
      imageUrl = AceImages.COACH_RADAR_ACTION,
      personalBest = "194 km/h Counter",
      targetSkillMetrics = listOf(
        TargetSkillMetric("Backswing Duration", "≤ 120", "ms", "-40ms vs average", "Ultra-short cocking phase to prevent being jammed"),
        TargetSkillMetric("Drive Flatness Elevation", "≤ 20", "cm", "Tape skimming", "Height above net tape across midcourt tramline"),
        TargetSkillMetric("Forearm Twist Speed", "≥ 950", "deg/s", "Pro twitch speed", "Pronation/supination angular rotational rate")
      ),
      steps = listOf(
        DrillStepInstruction(
          1,
          "Waist-High Neutral Racket Guard",
          "Hold racket in front of chest with relaxed grip. Stance low with knees flexed, ready to shift weight left or right.",
          "Racket head up at eye level, elbows free from ribs.",
          "Racket head up, quick fingers, ready for the fast exchange!"
        ),
        DrillStepInstruction(
          2,
          "Compact Finger Squeeze Snap",
          "Do not take full backswing. Squeeze the last three fingers tightly at impact, snapping the racket face through shuttle.",
          "Isometric finger grip pulse generating explosive whip.",
          "Short take-back, squeeze your grip at impact!"
        ),
        DrillStepInstruction(
          3,
          "Instant Recoil to Guard",
          "Immediately stop the follow-through and recoil racket back to chest level for the next incoming drive.",
          "Absorb opponent pace and redirect crosscourt.",
          "Snap and recoil! Keep your hands ready!"
        )
      ),
      commonFaults = listOf(
        CommonBiomechanicalFault(
          "Oversized Tennis Backswing",
          "Taking racket too far behind body creates timing delay on 180+ km/h drives.",
          "Fault: Keep your swing short! Punch with your fingers and forearm."
        )
      )
    ),

    StructuredDrill(
      id = "drill_reverse_slice_drop",
      title = "Deceptive Reverse Slice Drop Shot",
      subtitle = "Opposite-face slicing for sudden crosscourt deflection",
      category = DrillCategory.NET_DROPS,
      difficulty = DrillDifficulty.ELITE_PRO,
      durationMinutes = 25,
      targetReps = 20,
      estimatedCalories = 170,
      equipment = listOf("Yonex Astrox Racket", "Feather Shuttles", "Video Camera"),
      summary = "World-class technique cutting inside-out across shuttle feathers from the forehand rear court, sending shuttle deceptively crosscourt.",
      badgeLabel = "ELITE MASTERCLASS 👑",
      imageUrl = AceImages.REVIEW_VIDEO_STILL,
      personalBest = "94% Deception Index",
      targetSkillMetrics = listOf(
        TargetSkillMetric("Reverse Slice Angle", "32° - 38°", "deg", "Optimal aerodynamic cut", "Racket face bevel against shuttle trajectory"),
        TargetSkillMetric("Contact Deception Delay", "≤ 10", "ms", "Olympic tier", "Time difference between stroke cue and directional tell"),
        TargetSkillMetric("Tight Hairpin Drop Depth", "≤ 40", "cm", "Inside front service box", "Landing distance behind net tape")
      ),
      steps = listOf(
        DrillStepInstruction(
          1,
          "Straight Line Attack Feint",
          "Set up body angle aiming directly down the line. Keep eyes fixed on the near tramline.",
          "Deceives opponent defender into guarding the straight line.",
          "Sell the straight smash with your shoulders!"
        ),
        DrillStepInstruction(
          2,
          "Inside-Out Supination Cut",
          "At the last 15 milliseconds, supinate forearm sharply, slicing the inner cork and feather skirt from left to right.",
          "Reverse spin causes abrupt flight path veer.",
          "Inside-out brush! Cut across the cork!"
        ),
        DrillStepInstruction(
          3,
          "Low-Stance Forward Pounce",
          "Recover smoothly into forward diagonal stride to capitalize on weak tumbling net returns.",
          "Ready for tight net tumble or push.",
          "Forward momentum, control the front court!"
        )
      ),
      commonFaults = listOf(
        CommonBiomechanicalFault(
          "Looking at the Target Spot",
          "Gazing at crosscourt corner telegraphs intention before contact.",
          "Fault: Keep your gaze down-the-line until after contact."
        )
      )
    ),

    StructuredDrill(
      id = "drill_stick_smash_line",
      title = "Down-the-Line Steep Stick Smash",
      subtitle = "Sharp downward whip using wrist pronation without full jump",
      category = DrillCategory.SMASH_POWER,
      difficulty = DrillDifficulty.ADVANCED,
      durationMinutes = 20,
      targetReps = 24,
      estimatedCalories = 150,
      equipment = listOf("Racket", "Feathers", "Tramline Target Mats"),
      summary = "Fast offensive strike utilizing sudden wrist extension and steep downward angle without full jump recovery delay.",
      badgeLabel = "LINE SNIPER 🎯",
      imageUrl = AceImages.COACH_RADAR_ACTION,
      personalBest = "218 km/h • 88% Form",
      targetSkillMetrics = listOf(
        TargetSkillMetric("Downward Steep Angle", "≥ 32°", "deg", "Steep line trajectory", "Entry angle into opponent frontcourt"),
        TargetSkillMetric("Sideline Precision Margin", "≤ 18", "cm", "Hugging white paint", "Lateral accuracy along side boundary line"),
        TargetSkillMetric("Stroke Cycle Time", "≤ 380", "ms", "-80ms vs jump smash", "Total duration from initiation to baseline return")
      ),
      steps = listOf(
        DrillStepInstruction(
          1,
          "Midcourt Stepping Stance",
          "Step in on dominant rear foot without taking airborne jump. Anchor front toe toward sideline.",
          "High stability foundation for precision placement.",
          "Firm footing, eyes locked on the side tramline!"
        ),
        DrillStepInstruction(
          2,
          "Sharp Overhead Snap",
          "Rapid elbow drive followed by aggressive wrist pronation, driving shuttlecock steeply down the sideline.",
          "Steep downward trajectory into opponent mid-tramline.",
          "Snap down hard! Angle over raw power!"
        ),
        DrillStepInstruction(
          3,
          "Fast Lateral Shuffle",
          "Shuffle sideways back into center court immediately, ready for straight return block.",
          "Minimal recovery lag due to ground contact maintenance.",
          "Quick lateral reset, anticipate the straight block!"
        )
      ),
      commonFaults = listOf(
        CommonBiomechanicalFault(
          "Over-Hitting Into The Net",
          "Forcing too steep an angle from deep court clips the net tape.",
          "Fault: Pick higher apex contact before snapping down."
        )
      )
    ),

    StructuredDrill(
      id = "drill_defensive_lift_block",
      title = "Defensive Smash Block to Tight Net Spin",
      subtitle = "Absorbing opponent 240+ km/h smashes with soft touch",
      category = DrillCategory.DEFENSE_CLEARS,
      difficulty = DrillDifficulty.BEGINNER,
      durationMinutes = 15,
      targetReps = 25,
      estimatedCalories = 110,
      equipment = listOf("Racket", "Shuttle Feeder"),
      summary = "Teaches defensive stability, relaxed grip cushioning, and turning heavy offensive attacks into offensive net tumble opportunities.",
      badgeLabel = "SOLID DEFENSE 🛡️",
      imageUrl = AceImages.COURT_CAMERA_READY,
      personalBest = "89% Net Cushioning",
      targetSkillMetrics = listOf(
        TargetSkillMetric("Grip Softness Index", "≤ 3", "/10", "Feather-light tension", "Relaxed hand tension absorbing shuttle momentum"),
        TargetSkillMetric("Net Spin Revolutions", "≥ 2.5", "rev/s", "Hairpin tumbling", "Rotations of shuttle as it crests net tape"),
        TargetSkillMetric("Knee Stance Flexion", "≥ 50°", "deg", "Low center of gravity", "Deep defensive crouch")
      ),
      steps = listOf(
        DrillStepInstruction(
          1,
          "Low Defensive Wide Base",
          "Drop hips low, feet 1.5x shoulder width apart. Hold racket comfortably in front with backhand grip.",
          "Stable low stance lowering center of mass.",
          "Get low! Wide base, racket poised in front!"
        ),
        DrillStepInstruction(
          2,
          "Cushioning Soft Block",
          "Meet incoming smash in front of body. Allow string bed to yield gently on contact to deaden the speed.",
          "Do not push hard; let opponent power dissipate on string bed.",
          "Soft hands! Absorb the smash, let it tumble over!"
        ),
        DrillStepInstruction(
          3,
          "Forward Pressure Step",
          "Step forward with dominant leg to threaten opponent's next shot at the net.",
          "Transitions defense into immediate net offense.",
          "Step in! Claim the net tape!"
        )
      ),
      commonFaults = listOf(
        CommonBiomechanicalFault(
          "Stiff White-Knuckle Grip",
          "Gripping racket tightly causes shuttle to pop up high for easy opponent kill.",
          "Fault: Loosen your fingers! Hold racket like a fragile bird."
        )
      )
    )
  )
}
