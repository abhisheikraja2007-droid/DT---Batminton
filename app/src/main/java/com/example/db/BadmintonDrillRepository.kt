package com.example.db

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class BadmintonDrillModel(
  @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
  @get:PropertyName("drillName") @set:PropertyName("drillName") var drillName: String = "",
  @get:PropertyName("difficulty") @set:PropertyName("difficulty") var difficulty: String = "Intermediate",
  @get:PropertyName("targetStroke") @set:PropertyName("targetStroke") var targetStroke: String = "Jump Smash",
  @get:PropertyName("category") @set:PropertyName("category") var category: String = "Smash & Power",
  @get:PropertyName("durationMinutes") @set:PropertyName("durationMinutes") var durationMinutes: Int = 20,
  @get:PropertyName("targetReps") @set:PropertyName("targetReps") var targetReps: Int = 20,
  @get:PropertyName("estimatedCalories") @set:PropertyName("estimatedCalories") var estimatedCalories: Int = 150,
  @get:PropertyName("summary") @set:PropertyName("summary") var summary: String = "",
  @get:PropertyName("equipment") @set:PropertyName("equipment") var equipment: List<String> = emptyList(),
  @get:PropertyName("isFavorite") @set:PropertyName("isFavorite") var isFavorite: Boolean = false,
  @get:PropertyName("completedCount") @set:PropertyName("completedCount") var completedCount: Int = 0,
  @get:PropertyName("createdAtMs") @set:PropertyName("createdAtMs") var createdAtMs: Long = System.currentTimeMillis()
)

class BadmintonDrillRepository private constructor(context: Context) {

  private val firestore = FirebaseFirestore.getInstance()
  private val auth = FirebaseAuth.getInstance()
  private val aceRepository = AceRepository.getInstance(context)
  private val scope = CoroutineScope(Dispatchers.IO)

  private val _drills = MutableStateFlow<List<BadmintonDrillModel>>(emptyList())
  val drills: StateFlow<List<BadmintonDrillModel>> = _drills.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  init {
    scope.launch {
      initializeFirestoreWithSampleDrills()
      listenToFirestoreDrills()
    }
  }

  private fun createSampleDrills(): List<BadmintonDrillModel> {
    val items = mutableListOf<BadmintonDrillModel>()
    
    items.add(
      BadmintonDrillModel(
        id = "drill_zenith_jump_smash",
        drillName = "Zenith Jump Smash & Forearm Pronation",
        difficulty = "Advanced",
        targetStroke = "Jump Smash",
        category = "Smash & Power",
        durationMinutes = 25,
        targetReps = 20,
        estimatedCalories = 185,
        summary = "Explosive vertical jump smash focusing on maximum reach apex (2.88m), forearm pronation, and steep downward trajectory.",
        equipment = listOf("Yonex Racket", "Feather Shuttles", "Full Court", "Speed Radar")
      )
    )

    items.add(
      BadmintonDrillModel(
        id = "drill_crosscourt_fast_slice",
        drillName = "Crosscourt Fast Slice Drop & Deception",
        difficulty = "Intermediate",
        targetStroke = "Fast Slice Drop Shot",
        category = "Net & Drops",
        durationMinutes = 20,
        targetReps = 24,
        estimatedCalories = 140,
        summary = "Disguise overhead smash stance, then execute a 30-degree slice across shuttle feathers for a tight net tumble.",
        equipment = listOf("Racket", "Feather Shuttles", "Front Court Tape")
      )
    )

    items.add(
      BadmintonDrillModel(
        id = "drill_deep_high_clear",
        drillName = "Deep Backcourt High Defensive Clear",
        difficulty = "Intermediate",
        targetStroke = "High Defensive Clear",
        category = "Defense & Clears",
        durationMinutes = 22,
        targetReps = 25,
        estimatedCalories = 160,
        summary = "Maximum height overhead flight trajectory driving aggressive opponents back to their baseline rear tramline.",
        equipment = listOf("Racket", "Shuttlecocks", "Rear Target Mats")
      )
    )

    items.add(
      BadmintonDrillModel(
        id = "drill_6corner_shadow_agility",
        drillName = "Explosive 6-Corner Shadow Footwork",
        difficulty = "Advanced",
        targetStroke = "Multi-Directional Footwork",
        category = "Footwork & Agility",
        durationMinutes = 30,
        targetReps = 36,
        estimatedCalories = 240,
        summary = "High-speed shadow footwork covering net corners, midcourt defense stations, and deep rear corners with sub-0.40s reaction split-steps.",
        equipment = listOf("Agility Cones", "Full Court", "Interval Timer")
      )
    )

    items.add(
      BadmintonDrillModel(
        id = "drill_flat_drive_interception",
        drillName = "Rapid Flat Drive Counter & Interception",
        difficulty = "Intermediate",
        targetStroke = "Flat Drive Counter",
        category = "Drives & Deception",
        durationMinutes = 18,
        targetReps = 30,
        estimatedCalories = 135,
        summary = "Compact backswing drive exchanges targeting midcourt doubles angles using twitch forearm pronation/supination.",
        equipment = listOf("Racket", "Feather Shuttles", "Midcourt Feeder")
      )
    )

    items.add(
      BadmintonDrillModel(
        id = "drill_reverse_slice_drop",
        drillName = "Deceptive Reverse Slice Drop Shot",
        difficulty = "Elite Pro",
        targetStroke = "Reverse Slice Drop",
        category = "Net & Drops",
        durationMinutes = 25,
        targetReps = 20,
        estimatedCalories = 170,
        summary = "Inside-out supination cut across shuttle cork from forehand rear court, sending shuttle crosscourt with hidden intention.",
        equipment = listOf("Astrox 99 Racket", "Feather Shuttles", "Camera Analysis")
      )
    )

    items.add(
      BadmintonDrillModel(
        id = "drill_steep_stick_smash",
        drillName = "Down-the-Line Steep Stick Smash",
        difficulty = "Advanced",
        targetStroke = "Steep Stick Smash",
        category = "Smash & Power",
        durationMinutes = 20,
        targetReps = 24,
        estimatedCalories = 150,
        summary = "Sharp downward whip using sudden wrist pronation without full jump, keeping shuttle hugging the side tramline paint.",
        equipment = listOf("Racket", "Feathers", "Tramline Target Mats")
      )
    )

    items.add(
      BadmintonDrillModel(
        id = "drill_defensive_smash_block",
        drillName = "Defensive Smash Block to Tight Net Spin",
        difficulty = "Beginner",
        targetStroke = "Defensive Smash Block",
        category = "Defense & Clears",
        durationMinutes = 15,
        targetReps = 25,
        estimatedCalories = 110,
        summary = "Soft cushioning grip technique absorbing 240+ km/h opponent smashes and converting them into tight hairpin net tumbles.",
        equipment = listOf("Racket", "Feather Feeder")
      )
    )

    return items
  }

  suspend fun initializeFirestoreWithSampleDrills() {
    _isLoading.value = true
    _statusMessage.value = "Initializing Firestore database with sample badminton drills..."

    val sampleDrills = createSampleDrills()

    try {
      // 1. Always seed local Room DB first
      for (drill in sampleDrills) {
        aceRepository.addDrill(
          DrillEntity(
            title = drill.drillName,
            category = drill.category,
            difficulty = drill.difficulty,
            durationMinutes = drill.durationMinutes,
            description = drill.summary,
            targetReps = drill.targetReps,
            targetElbowAngleDeg = 140f
          )
        )
      }
      _drills.value = sampleDrills

      // 2. Only write to Firestore if authenticated (per firestore.rules)
      val user = auth.currentUser
      if (user != null) {
        for (drill in sampleDrills) {
          val drillData = mapOf(
            "id" to drill.id,
            "drillName" to drill.drillName,
            "difficulty" to drill.difficulty,
            "targetStroke" to drill.targetStroke,
            "category" to drill.category,
            "durationMinutes" to drill.durationMinutes,
            "targetReps" to drill.targetReps,
            "estimatedCalories" to drill.estimatedCalories,
            "summary" to drill.summary,
            "equipment" to drill.equipment,
            "isFavorite" to drill.isFavorite,
            "completedCount" to drill.completedCount,
            "createdAtMs" to drill.createdAtMs
          )
          try {
            firestore.collection("drills").document(drill.id)
              .set(drillData, SetOptions.merge())
              .await()
          } catch (e: Exception) {
            // Non-critical if user lacks admin role
          }

          firestore.collection("users").document(user.uid)
            .collection("drills").document(drill.id)
            .set(
              mapOf(
                "id" to drill.id,
                "drillName" to drill.drillName,
                "difficulty" to drill.difficulty,
                "targetStroke" to drill.targetStroke,
                "category" to drill.category,
                "durationMinutes" to drill.durationMinutes,
                "targetReps" to drill.targetReps,
                "summary" to drill.summary,
                "lastSynced" to System.currentTimeMillis()
              ),
              SetOptions.merge()
            )
            .await()
        }
        _statusMessage.value = "Firestore synchronized with ${sampleDrills.size} badminton drills."
      } else {
        _statusMessage.value = "Loaded ${sampleDrills.size} badminton drills locally."
      }
      _isLoading.value = false
    } catch (e: Exception) {
      Log.e("BadmintonDrillRepo", "Drill initialization note: ${e.message}")
      _statusMessage.value = "Drills ready locally."
      _isLoading.value = false
      _drills.value = sampleDrills
    }
  }

  fun listenToFirestoreDrills() {
    firestore.collection("drills")
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.e("BadmintonDrillRepo", "Firestore snapshot listener error: ${error.message}")
          return@addSnapshotListener
        }

        if (snapshot != null && !snapshot.isEmpty) {
          val drillList = snapshot.documents.mapNotNull { doc ->
            try {
              doc.toObject(BadmintonDrillModel::class.java)?.apply {
                if (id.isEmpty()) id = doc.id
              }
            } catch (e: Exception) {
              val id = doc.id
              val name = doc.getString("drillName") ?: doc.getString("title") ?: "Badminton Drill"
              val diff = doc.getString("difficulty") ?: "Intermediate"
              val stroke = doc.getString("targetStroke") ?: doc.getString("category") ?: "Smash"
              val cat = doc.getString("category") ?: "Smash & Power"
              val dur = (doc.getLong("durationMinutes") ?: 20L).toInt()
              val reps = (doc.getLong("targetReps") ?: 20L).toInt()
              val calories = (doc.getLong("estimatedCalories") ?: 150L).toInt()
              val sum = doc.getString("summary") ?: doc.getString("description") ?: ""
              BadmintonDrillModel(
                id = id,
                drillName = name,
                difficulty = diff,
                targetStroke = stroke,
                category = cat,
                durationMinutes = dur,
                targetReps = reps,
                estimatedCalories = calories,
                summary = sum
              )
            }
          }
          if (drillList.isNotEmpty()) {
            _drills.value = drillList
          }
        }
      }
  }

  fun addDrillToFirestore(
    drillName: String,
    difficulty: String,
    targetStroke: String,
    category: String = "Smash & Power",
    durationMinutes: Int = 20,
    targetReps: Int = 20,
    summary: String = "",
    equipment: List<String> = listOf("Racket", "Shuttlecocks")
  ) {
    scope.launch {
      _isLoading.value = true
      val id = "drill_custom_${System.currentTimeMillis()}"
      val newDrill = BadmintonDrillModel(
        id = id,
        drillName = drillName,
        difficulty = difficulty,
        targetStroke = targetStroke,
        category = category,
        durationMinutes = durationMinutes,
        targetReps = targetReps,
        estimatedCalories = durationMinutes * 7,
        summary = summary,
        equipment = equipment
      )

      try {
        val drillMap = mapOf(
          "id" to newDrill.id,
          "drillName" to newDrill.drillName,
          "difficulty" to newDrill.difficulty,
          "targetStroke" to newDrill.targetStroke,
          "category" to newDrill.category,
          "durationMinutes" to newDrill.durationMinutes,
          "targetReps" to newDrill.targetReps,
          "estimatedCalories" to newDrill.estimatedCalories,
          "summary" to newDrill.summary,
          "equipment" to newDrill.equipment,
          "createdAtMs" to newDrill.createdAtMs
        )

        firestore.collection("drills").document(id)
          .set(drillMap, SetOptions.merge())
          .await()

        val user = auth.currentUser
        if (user != null) {
          firestore.collection("users").document(user.uid)
            .collection("drills").document(id)
            .set(drillMap, SetOptions.merge())
            .await()
        }

        _drills.value = _drills.value + newDrill
        aceRepository.addDrill(
          DrillEntity(
            title = drillName,
            category = category,
            difficulty = difficulty,
            durationMinutes = durationMinutes,
            description = summary,
            targetReps = targetReps,
            targetElbowAngleDeg = 140f
          )
        )

        _statusMessage.value = "Added drill '$drillName' to Firestore."
      } catch (e: Exception) {
        Log.e("BadmintonDrillRepo", "Failed to add drill to Firestore: ${e.message}")
        _drills.value = _drills.value + newDrill
        _statusMessage.value = "Saved drill locally: $drillName"
      } finally {
        _isLoading.value = false
      }
    }
  }

  companion object {
    @Volatile
    private var instance: BadmintonDrillRepository? = null

    fun getInstance(context: Context): BadmintonDrillRepository {
      return instance ?: synchronized(this) {
        instance ?: BadmintonDrillRepository(context.applicationContext).also { instance = it }
      }
    }
  }
}
