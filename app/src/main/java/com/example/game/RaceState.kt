package com.example.game

import kotlin.math.sin
import kotlin.math.cos

// Class representing a track segment curve deflection
data class TrackSegment(
    val curve: Float, // horizontal bend (-2 to 2)
    val hill: Float   // vertical bend (-1 to 1)
)

data class TrackTemplate(
    val id: String,
    val name: String,
    val lengthMeters: Float,
    val idealRoadWidth: Float, // Small width favors small scales, large width favors colossal scales
    val baseCurveIntensity: Float,
    val obstacleDensity: Int,
    val description: String,
    val backgroundColorHex: Long
) {
    // Computes curvature at any distance along the track
    fun getCurvatureAt(z: Float): TrackSegment {
        val s1 = sin(z * 0.01f) * baseCurveIntensity
        val s2 = cos(z * 0.05f) * (baseCurveIntensity * 0.5f)
        val hill = sin(z * 0.02f) * 1.5f
        return TrackSegment(curve = s1 + s2, hill = hill)
    }
}

object PredefinedTracks {
    val tracks = listOf(
        TrackTemplate(
            id = "micro_meadows",
            name = "Microscopic Dew-Meadows",
            lengthMeters = 700f,
            idealRoadWidth = 1.8f, // Extremely narrow! Large scale beasts scrape the borders constantly
            baseCurveIntensity = 2.4f, // High bending curves!
            obstacleDensity = 6,
            description = "Narrow blades of dew-soaked grass form a twisty micro corridor. Small scale insects navigate with perfect ease; giant behemoths scrap margins and encounter heavy decelerations.",
            backgroundColorHex = 0xFF0D2511
        ),
        TrackTemplate(
            id = "canyon_ways",
            name = "Colossal Echo Canyons",
            lengthMeters = 1600f,
            idealRoadWidth = 8.5f, // Extremely wide! Huge creatures can sprint at extreme warp speeds
            baseCurveIntensity = 0.8f, // Gentle swoops
            obstacleDensity = 3,
            description = "A massive sandstone canyon with immense wide horizontal paths. Perfect for large scale dinosaurs and dragons to achieve top velocities, while tiny insects take longer to cover the ground.",
            backgroundColorHex = 0xFF351F0D
        ),
        TrackTemplate(
            id = "nexus_speedway",
            name = "Aether Nexus Grid",
            lengthMeters = 1100f,
            idealRoadWidth = 4.2f, // Standard balanced width
            baseCurveIntensity = 1.5f,
            obstacleDensity = 4,
            description = "A floating grid suspended in neon digital space. Offers equal opportunities, filled with speed rings and sudden, high-rebound barrier grids.",
            backgroundColorHex = 0xFF140D25
        )
    )

    fun getById(id: String): TrackTemplate {
        return tracks.find { it.id == id } ?: tracks.first()
    }
}

data class RacerState(
    val id: String,
    val name: String,
    val template: CreatureTemplate,
    val x: Float,                 // Horizontal lane offset (-1.0f = left border, +1.0f = right border)
    val z: Float,                 // Distance along the track (meters)
    val speed: Float,             // Current velocity (m/s)
    val stunRemainingMs: Long = 0,
    val shieldRemainingMs: Long = 0,
    val numbedRemainingMs: Long = 0, // Injected control delay / wiggle steering
    val isPlayer: Boolean = false,
    val currentLap: Int = 1,
    val isFinished: Boolean = false,
    val finishTimeMs: Long = 0,
    val currentPlacement: Int = 4,
    val massKg: Float             // Real-time mass
) {
    val scaleFactor: Float get() = template.scale
    val isInvincible: Boolean get() = shieldRemainingMs > 0
    val isStunned: Boolean get() = stunRemainingMs > 0
    val isNumbed: Boolean get() = numbedRemainingMs > 0
}

data class ProjectileState(
    val id: String,
    val firedByRacerId: String,
    val x: Float,
    val z: Float,
    val speed: Float,
    val type: AbilityType,
    val emoji: String,
    val description: String
)

data class TrackObstacle(
    val id: String,
    val x: Float, // Offset on road (-0.8 to 0.8)
    val z: Float, // Distance along the track
    val scaleSize: Float,
    val emoji: String,
    val isSmashed: Boolean = false
)

data class LiveRaceSimulationState(
    val track: TrackTemplate,
    val racers: List<RacerState> = emptyList(),
    val projectiles: List<ProjectileState> = emptyList(),
    val obstacles: List<TrackObstacle> = emptyList(),
    val durationElapsedMs: Long = 0,
    val maxLaps: Int = 2,
    val raceOngoing: Boolean = false,
    val showStartCountdown: Boolean = true,
    val countdownRemainingMs: Long = 3000,
    val logs: List<String> = listOf("RACING TELEMETRY SYSTEM INITIALIZED...")
)
