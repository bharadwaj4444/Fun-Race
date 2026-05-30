package com.example.game

import androidx.compose.ui.graphics.Color

enum class CreatureCategory(val displayName: String, val color: Color) {
    INSECT("Insects", Color(0xFF4CAF50)),       // Acid / Green
    MAMMAL("Mammals", Color(0xFFFF9800)),       // Aggressive / Orange
    REPTILE("Reptiles", Color(0xFF00BCD4)),      // Chill / Cyan
    AVIAN("Avians", Color(0xFF9C27B0)),         // Shock / Purple
    FANTASY_BEAST("Fantasy", Color(0xFFE91E63))  // Fire / Deep Pink
}

enum class AbilityType {
    OFFENSIVE_HARM,   // e.g. projectile/acid spit that slows opponents ahead
    UTILITY_NUMB,     // e.g. stun/freeze/static that jitters/locks target controls
    DEFENSIVE         // e.g. standard barrier shield or swift dodge
}

data class CreatureAbility(
    val name: String,
    val type: AbilityType,
    val description: String,
    val emoji: String
)

data class CreatureTemplate(
    val id: String,
    val name: String,
    val category: CreatureCategory,
    val tier: Int,              // Tier 1, 2, or 3
    val scale: Float,            // Scale factor (from 0.1x to 10.0x)
    val baseSpeed: Float,        // Max velocity parameter
    val baseAcceleration: Float, // Stride frequency / Inertia combat
    val baseHandling: Float,     // Turn radius / Agility response
    val massKg: Float,           // Controls momentum and collision knockbacks
    val ability: CreatureAbility,
    val upgradeCost: Int,        // Evolution Points to unlock this next Tier
    val description: String
) {
    // Computed physics values based on Variable Creature Scaling Rules
    val inertia: Float get() = massKg * scale
    val turningCircleRadiusMeters: Float get() = 12.0f * (1.0f / baseHandling) * (1.0f + (scale * 0.5f))
    val knockbackResistance: Float get() = massKg * scale
}

object CreatureRoster {
    // Map of evolution chains
    // Category -> List representing Tiers [Tier 1, Tier 2, Tier 3]
    val evolutionChains = mapOf(
        CreatureCategory.INSECT to listOf(
            CreatureTemplate(
                id = "insect_t1",
                name = "Nano Worker Ant",
                category = CreatureCategory.INSECT,
                tier = 1,
                scale = 0.1f,
                baseSpeed = 15f,
                baseAcceleration = 9.5f,
                baseHandling = 9.8f,
                massKg = 0.01f,
                ability = CreatureAbility("Formic Acid Spit", AbilityType.OFFENSIVE_HARM, "Spits corrosive acid that melts obstacle speed penalties and slows down opponents directly in front.", "🧪"),
                upgradeCost = 0,
                description = "Extremely miniature, highly swift, and capable of instantaneous cornering. Has virtually zero mass but incredible reactive reflexes."
            ),
            CreatureTemplate(
                id = "insect_t2",
                name = "Goliath Rhino Beetle",
                category = CreatureCategory.INSECT,
                tier = 2,
                scale = 0.6f,
                baseSpeed = 26f,
                baseAcceleration = 7.0f,
                baseHandling = 7.5f,
                massKg = 4.5f,
                ability = CreatureAbility("Chitin Shield", AbilityType.DEFENSIVE, "Hardens its body envelope, negating any incoming control hacks or projectile spikes for 3 seconds.", "🛡️"),
                upgradeCost = 150,
                description = "Encased in dense bio-engineered chitin plating. Heavy armor grants substantial obstacle plow capability and knockback protection."
            ),
            CreatureTemplate(
                id = "insect_t3",
                name = "Chimera Hornet",
                category = CreatureCategory.INSECT,
                tier = 3,
                scale = 1.8f,
                baseSpeed = 40f,
                baseAcceleration = 8.5f,
                baseHandling = 8.0f,
                massKg = 25f,
                ability = CreatureAbility("Neuro-Numbing Stinger", AbilityType.UTILITY_NUMB, "Launches a rapid venom dart. Target opponent experiences severe control drift for 3 seconds.", "🐝"),
                upgradeCost = 350,
                description = "The ultimate insect racer. Uses high-frequency compound wings to slice through drafts, achieving massive velocity while retaining tight air drift."
            )
        ),
        CreatureCategory.MAMMAL to listOf(
            CreatureTemplate(
                id = "mammal_t1",
                name = "Field Pocket Mouse",
                category = CreatureCategory.MAMMAL,
                tier = 1,
                scale = 0.15f,
                baseSpeed = 18f,
                baseAcceleration = 9.0f,
                baseHandling = 9.5f,
                massKg = 0.05f,
                ability = CreatureAbility("Swift Dodge Roll", AbilityType.DEFENSIVE, "Leaps sideway in a blur of fur, dodging any impending hazard or track trap.", "💨"),
                upgradeCost = 0,
                description = "A compact rodent utilizing immediate muscle trigger fire to sprint through track margins and micro-tunnels with perfect traction."
            ),
            CreatureTemplate(
                id = "mammal_t2",
                name = "Saber Cat Huntress",
                category = CreatureCategory.MAMMAL,
                tier = 2,
                scale = 1.5f,
                baseSpeed = 34f,
                baseAcceleration = 8.0f,
                baseHandling = 7.2f,
                massKg = 180f,
                ability = CreatureAbility("Pounce Strike", AbilityType.OFFENSIVE_HARM, "Sprints forward in a heavy lunge, slamming and spinning out anything in its direct trajectory.", "🐅"),
                upgradeCost = 150,
                description = "A powerful quadruped engineered with dense muscle fibers. Sprints down straightaways, creating high drafts that pull smaller racers along."
            ),
            CreatureTemplate(
                id = "mammal_t3",
                name = "Colossal Behemoth",
                category = CreatureCategory.MAMMAL,
                tier = 3,
                scale = 8.5f,
                baseSpeed = 50f,
                baseAcceleration = 4.0f,
                baseHandling = 3.5f,
                massKg = 6500f,
                ability = CreatureAbility("Earthquake Stomp", AbilityType.UTILITY_NUMB, "Stomps the track, creating a seismic wave that stuns and numbs the steering of all standard racers.", "💥"),
                upgradeCost = 400,
                description = "An absolute mountain of muscle and trunk. Possesses immense inertia, yielding supreme immunity to knockbacks, but experiences wide, drifting turns."
            )
        ),
        CreatureCategory.REPTILE to listOf(
            CreatureTemplate(
                id = "reptile_t1",
                name = "Spiny Tree Gecko",
                category = CreatureCategory.REPTILE,
                tier = 1,
                scale = 0.18f,
                baseSpeed = 16f,
                baseAcceleration = 9.2f,
                baseHandling = 9.6f,
                massKg = 0.08f,
                ability = CreatureAbility("Acid Spit Bubble", AbilityType.OFFENSIVE_HARM, "Spits acidic green bubbles that coat the track, heavily slowing racers who touch them.", "🤢"),
                upgradeCost = 0,
                description = "Relies on microscopic foot-pads to hold maximum corner traction. Suffers zero slide even on highly angled corners."
            ),
            CreatureTemplate(
                id = "reptile_t2",
                name = "Obsidian Basilisk",
                category = CreatureCategory.REPTILE,
                tier = 2,
                scale = 2.2f,
                baseSpeed = 32f,
                baseAcceleration = 6.5f,
                baseHandling = 6.8f,
                massKg = 220f,
                ability = CreatureAbility("Petrifying Glare", AbilityType.UTILITY_NUMB, "Glares ahead, freezing the immediate controls of the racer directly in front for 2 seconds.", "👁️"),
                upgradeCost = 150,
                description = "Slinks along the hot tarmac, converting heat scales to sudden bursts of momentum. High durability enables reliable obstacle smashing."
            ),
            CreatureTemplate(
                id = "reptile_t3",
                name = "Tidal Leviathan",
                category = CreatureCategory.REPTILE,
                tier = 3,
                scale = 8.0f,
                baseSpeed = 48f,
                baseAcceleration = 4.5f,
                baseHandling = 4.0f,
                massKg = 5200f,
                ability = CreatureAbility("Tidal Wave Surge", AbilityType.OFFENSIVE_HARM, "Releases a deluge of aquatic pressure, pushing all other players backward from its path.", "🌊"),
                upgradeCost = 400,
                description = "A massive maritime dragon gliding above the ground on a pocket of high-pressure vapour. Possesses devastating mass and infinite momentum."
            )
        ),
        CreatureCategory.AVIAN to listOf(
            CreatureTemplate(
                id = "avian_t1",
                name = "Flash Hummingbird",
                category = CreatureCategory.AVIAN,
                tier = 1,
                scale = 0.12f,
                baseSpeed = 22f,
                baseAcceleration = 9.8f,
                baseHandling = 9.9f,
                massKg = 0.015f,
                ability = CreatureAbility("Nectar Boost", AbilityType.DEFENSIVE, "Consumes compressed sugars, gaining a brief double-speed boost and invincibility.", "🌸"),
                upgradeCost = 0,
                description = "Beats wings at supersonic speeds. Can reverse direction, hover instantly, and dodge oncoming traffic with absolute ease."
            ),
            CreatureTemplate(
                id = "avian_t2",
                name = "Storm Falcon",
                category = CreatureCategory.AVIAN,
                tier = 2,
                scale = 1.6f,
                baseSpeed = 38f,
                baseAcceleration = 8.8f,
                baseHandling = 7.8f,
                massKg = 15f,
                ability = CreatureAbility("Aero Sonic Boom", AbilityType.OFFENSIVE_HARM, "Emits an acoustic shockwave forwards, dazing other racers and disrupting their trajectory.", "🔊"),
                upgradeCost = 150,
                description = "A dive specialist that locks its feathers to enter high-speed aerodynamic drafts, executing breathtaking passes down the centers of runways."
            ),
            CreatureTemplate(
                id = "avian_t3",
                name = "Astral Thunder Phoenix",
                category = CreatureCategory.AVIAN,
                tier = 3,
                scale = 5.0f,
                baseSpeed = 54f,
                baseAcceleration = 8.2f,
                baseHandling = 5.5f,
                massKg = 900f,
                ability = CreatureAbility("Static Shock Burst", AbilityType.UTILITY_NUMB, "Unleashes lightning that arcs to all nearby racers, causing their control screens to flicker and inputs to swap.", "⚡"),
                upgradeCost = 420,
                description = "Emanates extreme cosmic bio-electricity. Ignites the air around it, combining rapid wingspans with fiery gravity wells."
            )
        ),
        CreatureCategory.FANTASY_BEAST to listOf(
            CreatureTemplate(
                id = "fantasy_t1",
                name = "Pygmy Fire Wyvern",
                category = CreatureCategory.FANTASY_BEAST,
                tier = 1,
                scale = 0.35f,
                baseSpeed = 20f,
                baseAcceleration = 9.0f,
                baseHandling = 9.0f,
                massKg = 0.8f,
                ability = CreatureAbility("Spark Puff", AbilityType.OFFENSIVE_HARM, "Fires a minor spark projectile forwards. Sparks detonate on contact, causing opponents to spin.", "🔥"),
                upgradeCost = 0,
                description = "A juvenile wyvern with underdeveloped fire glands. High maneuverability enables it to breeze past colossal blockades."
            ),
            CreatureTemplate(
                id = "fantasy_t2",
                name = "Abyssal Chimera Gryphon",
                category = CreatureCategory.FANTASY_BEAST,
                tier = 2,
                scale = 2.5f,
                baseSpeed = 36f,
                baseAcceleration = 7.5f,
                baseHandling = 6.5f,
                massKg = 380f,
                ability = CreatureAbility("Void Ward", AbilityType.DEFENSIVE, "Open a shadow portal that swallows all incoming obstacles or negative project effects.", "🌀"),
                upgradeCost = 200,
                description = "A fierce hybrid of lion, eagle, and shadow matter. Sprints on all fours or glides low to execute powerful high-acceleration curves."
            ),
            CreatureTemplate(
                id = "fantasy_t3",
                name = "Dreadnought Wyrm Dragon",
                category = CreatureCategory.FANTASY_BEAST,
                tier = 3,
                scale = 10.0f,
                baseSpeed = 56f,
                baseAcceleration = 5.0f,
                baseHandling = 3.0f,
                massKg = 8500f,
                ability = CreatureAbility("Doomsday Breath", AbilityType.OFFENSIVE_HARM, "Floods the track in front with an apocalyptic firestorm, slowing and melting all objects.", "☄️"),
                upgradeCost = 500,
                description = "The ultimate world-breaker. A legendary colossal ancient dragon. Its footsteps shatter roads, and its massive frame plows through obstacles with zero slowdown."
            )
        )
    )

    fun getTemplateById(id: String): CreatureTemplate? {
        return evolutionChains.values.flatten().find { it.id == id }
    }

    fun getTier(category: CreatureCategory, tier: Int): CreatureTemplate {
        val chain = evolutionChains[category] ?: throw IllegalArgumentException("Unknown category $category")
        return chain.firstOrNull { it.tier == tier } ?: chain.last()
    }
}
