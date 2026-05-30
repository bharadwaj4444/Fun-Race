package com.example.game

import kotlin.math.sqrt

object PhysicsEngine {

    /**
     * Calculates the actual top speed (m/s) based on base speed and scale.
     * Large creatures have massive biological top speeds due to stride/wing span, but high inertia.
     */
    fun calculateTopSpeed(baseSpeed: Float, scale: Float): Float {
        // Top speed scale multiplier = (0.65 + 0.35 * sqrt(scale))
        return baseSpeed * (0.65f + 0.35f * sqrt(scale))
    }

    /**
     * Calculates acceleration (m/s^2) based on base acceleration and scale.
     * Small creatures have instant neural impulses and power-to-weight ratios, giving rapid startup.
     */
    fun calculateAcceleration(baseAcc: Float, scale: Float): Float {
        // Acc multiplier decreases as scale increases (2.0 / (1.0 + sqrt(scale)))
        return baseAcc * (2.0f / (1.0f + sqrt(scale)))
    }

    /**
     * Calculates turning rate (degrees of steering shift per frame) based on agility and scale.
     * Smaller creatures can corner sharply, while colossal beasts drift due to centrifugal force.
     */
    fun calculateTurnResponsiveness(baseHandling: Float, scale: Float): Float {
        // High scales make turns wider and slower
        return baseHandling * (1.2f / (0.2f + sqrt(scale)))
    }

    /**
     * Calculates the physical Turning Circle Radius (meters) required to make a full 180 coordinate turn.
     */
    fun calculateTurningCircleRadius(baseHandling: Float, scale: Float): Float {
        return 15.0f * (1.0f / baseHandling) * (1.0f + 1.2f * sqrt(scale))
    }

    /**
     * Calculates collision shock wave / knockback force.
     * If attacker collides with victim:
     * - Output indicates velocity reduction or displacement.
     * - Small victim hit by a massive attacker incurs high displacement.
     */
    data class CollisionResult(
        val attackerSpeedLossRatio: Float,
        val victimSpeedLossRatio: Float,
        val knockbackDistanceX: Float, // horizontal lane push
        val stunDurationMs: Long,
        val logDescription: String
    )

    fun calculateCollision(
        attackerScale: Float,
        attackerMass: Float,
        attackerSpeed: Float,
        victimScale: Float,
        victimMass: Float,
        victimSpeed: Float
    ): CollisionResult {
        val massRatio = attackerMass * attackerScale / (victimMass * victimScale + 0.001f)
        
        return if (massRatio >= 5.0f) {
            // Massive attacker bowls over small victim
            CollisionResult(
                attackerSpeedLossRatio = 0.05f, // Barely slows down
                victimSpeedLossRatio = 0.85f,  // Almost stopped
                knockbackDistanceX = 0.5f * massRatio.coerceAtMost(15f), // Tossed off road
                stunDurationMs = 1500,
                logDescription = "COLLISION: Heavy attacker crushed and launched lightweight victim! (Mass ratio: %.1f)".format(massRatio)
            )
        } else if (massRatio <= 0.2f) {
            // Tiny attacker bumps massive victim and bounces back
            CollisionResult(
                attackerSpeedLossRatio = 0.70f, // Major slowdown
                victimSpeedLossRatio = 0.01f,  // Doesn't notice
                knockbackDistanceX = -0.3f,    // Bounced backwards
                stunDurationMs = 800,
                logDescription = "COLLISION: Tiny racer bounced off the colossal competitor like a pebble!"
            )
        } else {
            // Relatively equal size bump
            CollisionResult(
                attackerSpeedLossRatio = 0.25f,
                victimSpeedLossRatio = 0.35f,
                knockbackDistanceX = 0.15f * (if (RandomHolder.nextFloat() > 0.5f) 1f else -1f),
                stunDurationMs = 500,
                logDescription = "COLLISION: Standard contact collision. Both racers wobbled."
            )
        }
    }
}

// Simple deterministic random for pure pure physics simulations
object RandomHolder {
    private var seed = 42
    fun nextFloat(): Float {
        seed = (seed * 1103515245 + 12345) and 0x7fffffff
        return seed.toFloat() / 0x7fffffff.toFloat()
    }
}
