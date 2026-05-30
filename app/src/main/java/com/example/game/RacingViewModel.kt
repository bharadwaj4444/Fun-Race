package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.LeaderboardEntryEntity
import com.example.data.RacingRepository
import com.example.data.UserProgressEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

sealed class RacingScreen {
    object Welcome : RacingScreen()
    object Lab : RacingScreen()
    object Matchmaker : RacingScreen()
    object ActiveRace : RacingScreen()
    object RaceReport : RacingScreen()
    object Leaderboard : RacingScreen()
}

class RacingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = RacingRepository(db.racingDao())

    // Expose flows to the UI layer
    val userProgress: StateFlow<UserProgressEntity?> = repository.userProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val leaderboard: StateFlow<List<LeaderboardEntryEntity>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Global navigation
    private val _currentScreen = MutableStateFlow<RacingScreen>(RacingScreen.Welcome)
    val currentScreen: StateFlow<RacingScreen> = _currentScreen.asStateFlow()

    // Matchmaking configuration
    private val _selectedTrack = MutableStateFlow<TrackTemplate>(PredefinedTracks.tracks.first())
    val selectedTrack: StateFlow<TrackTemplate> = _selectedTrack.asStateFlow()

    // Live Race Simulation State
    private val _liveRace = MutableStateFlow<LiveRaceSimulationState>(LiveRaceSimulationState(PredefinedTracks.tracks.first()))
    val liveRace: StateFlow<LiveRaceSimulationState> = _liveRace.asStateFlow()

    // Report configuration
    private val _lastRacePlacement = MutableStateFlow(1)
    val lastRacePlacement: StateFlow<Int> = _lastRacePlacement.asStateFlow()

    private val _lastRaceTime = MutableStateFlow("0:00.00")
    val lastRaceTime: StateFlow<String> = _lastRaceTime.asStateFlow()

    private val _lastRaceEpReward = MutableStateFlow(0)
    val lastRaceEpReward: StateFlow<Int> = _lastRaceEpReward.asStateFlow()

    private val _lastRaceRpReward = MutableStateFlow(0)
    val lastRaceRpReward: StateFlow<Int> = _lastRaceRpReward.asStateFlow()

    // Steer input set by UI drag or buttons (-1.0 to +1.0)
    private var playerSteerInput = 0f
    private var abilityCooldownMs = 0L

    private var gameLoopJob: Job? = null

    init {
        viewModelScope.launch {
            // Pre-seed database with progress and high-scores
            repository.seedLeaderboardIfEmpty()
        }
    }

    fun navigateTo(screen: RacingScreen) {
        _currentScreen.value = screen
        if (screen == RacingScreen.ActiveRace) {
            setupAndStartRace()
        } else {
            stopGameLoop()
        }
    }

    fun selectTrack(trackId: String) {
        _selectedTrack.value = PredefinedTracks.getById(trackId)
    }

    fun selectActiveCreature(creatureId: String) {
        viewModelScope.launch {
            repository.saveActiveCreature(creatureId)
        }
    }

    fun upgradeCreatureTier(category: CreatureCategory, targetTier: Int) {
        viewModelScope.launch {
            val chain = CreatureRoster.evolutionChains[category] ?: return@launch
            val nextTemplate = chain.find { it.tier == targetTier } ?: return@launch
            val progress = repository.getOrCreateProgress()
            
            if (progress.evolutionPoints >= nextTemplate.upgradeCost) {
                repository.unlockCreature(nextTemplate.id, nextTemplate.upgradeCost)
            }
        }
    }

    // Input hooks for the UI steer dials
    fun updatePlayerSteer(input: Float) {
        playerSteerInput = input.coerceIn(-1.0f, 1.0f)
    }

    fun activatePlayerAbility() {
        if (abilityCooldownMs > 0) return
        
        val liveState = _liveRace.value
        if (!liveState.raceOngoing) return

        val player = liveState.racers.find { it.isPlayer } ?: return
        if (player.isStunned || player.isFinished) return

        val ability = player.template.ability
        val newProjId = "p_player_${System.currentTimeMillis()}"
        
        // Formulate description for logging
        val desc = "You triggered ${ability.emoji} ${ability.name}!"
        
        val direction = if (ability.type == AbilityType.OFFENSIVE_HARM || ability.type == AbilityType.UTILITY_NUMB) 1f else 0f
        
        // Spawn active projectile or self effect
        viewModelScope.launch {
            if (ability.type == AbilityType.DEFENSIVE) {
                // Instantly apply self protective barrier shield
                addLog("SHIELD ACTIVATED: You deployed ${ability.name}!")
                updateRacer(player.id) {
                    it.copy(shieldRemainingMs = 4000L) // 4 seconds shield
                }
            } else {
                // Attack projectile
                val proj = ProjectileState(
                    id = newProjId,
                    firedByRacerId = player.id,
                    x = player.x,
                    z = player.z + (player.scaleFactor * 1.5f), // spawn slightly ahead
                    speed = player.speed + 25.0f, // Fired projectile flies rapidly ahead
                    type = ability.type,
                    emoji = ability.emoji,
                    description = desc
                )
                
                addLog(desc)
                _liveRace.update { current ->
                    current.copy(projectiles = current.projectiles + proj)
                }
            }
            // Trigger 6s cooldown
            abilityCooldownMs = 6000L
        }
    }

    private fun addLog(message: String) {
        _liveRace.update { current ->
            val updatedLogs = (listOf(message) + current.logs).take(20)
            current.copy(logs = updatedLogs)
        }
    }

    private fun updateRacer(racerId: String, transform: (RacerState) -> RacerState) {
        _liveRace.update { current ->
            current.copy(
                racers = current.racers.map { if (it.id == racerId) transform(it) else it }
            )
        }
    }

    private fun setupAndStartRace() {
        viewModelScope.launch {
            val progress = repository.getOrCreateProgress()
            val playerTemplate = CreatureRoster.getTemplateById(progress.activeCreatureId)
                ?: CreatureRoster.getTier(CreatureCategory.INSECT, 1)

            val trackSelected = _selectedTrack.value

            // Pick 3 random AI opponents on different sizes
            val botsPool = listOf(
                // Small Insect
                CreatureRoster.getTier(CreatureCategory.INSECT, 1),
                CreatureRoster.getTier(CreatureCategory.INSECT, 2),
                CreatureRoster.getTier(CreatureCategory.INSECT, 3),
                // Mid Mammal
                CreatureRoster.getTier(CreatureCategory.MAMMAL, 1),
                CreatureRoster.getTier(CreatureCategory.MAMMAL, 2),
                // Large Behemoth
                CreatureRoster.getTier(CreatureCategory.MAMMAL, 3),
                // Reptiles
                CreatureRoster.getTier(CreatureCategory.REPTILE, 1),
                CreatureRoster.getTier(CreatureCategory.REPTILE, 2),
                CreatureRoster.getTier(CreatureCategory.REPTILE, 3),
                // Avians
                CreatureRoster.getTier(CreatureCategory.AVIAN, 1),
                CreatureRoster.getTier(CreatureCategory.AVIAN, 2),
                CreatureRoster.getTier(CreatureCategory.AVIAN, 3),
                // Fantasy
                CreatureRoster.getTier(CreatureCategory.FANTASY_BEAST, 1),
                CreatureRoster.getTier(CreatureCategory.FANTASY_BEAST, 2),
                CreatureRoster.getTier(CreatureCategory.FANTASY_BEAST, 3)
            ).shuffled()
                .filter { it.id != playerTemplate.id }
                .take(3)

            val initialRacers = mutableListOf<RacerState>()
            
            // Add Player
            initialRacers.add(
                RacerState(
                    id = "player_id",
                    name = "Player (${playerTemplate.name})",
                    template = playerTemplate,
                    x = -0.4f, // Start on the left part
                    z = 0.0f,
                    speed = 0.0f,
                    isPlayer = true,
                    massKg = playerTemplate.massKg
                )
            )

            // Add Bots
            botsPool.forEachIndexed { i, botTemplate ->
                val startX = if (i == 0) 0.4f else if (i == 1) -0.1f else 0.1f
                // Stagger spawn so they don't instacollide at start
                val startZ = -(i + 1) * 8.0f 
                initialRacers.add(
                    RacerState(
                        id = "bot_${i + 1}",
                        name = botTemplate.name,
                        template = botTemplate,
                        x = startX,
                        z = startZ,
                        speed = 0.0f,
                        isPlayer = false,
                        massKg = botTemplate.massKg
                    )
                )
            }

            // Create obstacles at spaced intervals along the track length
            val initialObstacles = mutableListOf<TrackObstacle>()
            val segmentSpacing = trackSelected.lengthMeters / (trackSelected.obstacleDensity * 2)
            
            // Layout barriers, stones, bubbles based on track scale
            for (i in 1..trackSelected.obstacleDensity) {
                val zPos = i * segmentSpacing + (Random.nextFloat() * 20f)
                val laneOffset = if (i % 2 == 0) -0.5f else 0.5f
                
                // Select theme emoji
                val (emoji, obsScale) = when (trackSelected.id) {
                    "micro_meadows" -> if (i % 2 == 0) Pair("💧", 0.4f) else Pair("🍁", 0.3f)
                    "canyon_ways" -> if (i % 2 == 0) Pair("🪨", 1.8f) else Pair("🌵", 1.0f)
                    else -> if (i % 2 == 0) Pair("💠", 1.0f) else Pair("💥", 0.6f)
                }

                initialObstacles.add(
                    TrackObstacle(
                        id = "obs_$i",
                        x = laneOffset,
                        z = zPos,
                        scaleSize = obsScale,
                        emoji = emoji
                    )
                )
            }

            // Set up initial state with 3-second countdown
            _liveRace.value = LiveRaceSimulationState(
                track = trackSelected,
                racers = initialRacers,
                projectiles = emptyList(),
                obstacles = initialObstacles,
                durationElapsedMs = 0,
                raceOngoing = false,
                showStartCountdown = true,
                countdownRemainingMs = 3000L,
                logs = listOf(
                    "TRACK PREPARED: Ready at ${trackSelected.name}!",
                    "LAWS OF NATURE: Scale shifts turning radius and impact speed."
                )
            )

            abilityCooldownMs = 0L
            playerSteerInput = 0f
            
            startGameLoop()
        }
    }

    private fun startGameLoop() {
        stopGameLoop()
        
        gameLoopJob = viewModelScope.launch {
            val tickRateMs = 30L // ~33 frames per second simulation
            var lastUpdateNano = System.nanoTime()

            while (true) {
                delay(tickRateMs)
                
                val currentNano = System.nanoTime()
                val deltaMs = (currentNano - lastUpdateNano) / 1_000_000L
                lastUpdateNano = currentNano

                val state = _liveRace.value

                if (state.showStartCountdown) {
                    val remaining = state.countdownRemainingMs - deltaMs
                    if (remaining <= 0) {
                        _liveRace.update { it.copy(
                            showStartCountdown = false,
                            countdownRemainingMs = 0,
                            raceOngoing = true
                        )}
                        addLog("🚦 LIGHTS GREEN!! GO GO GO!")
                    } else {
                        _liveRace.update { it.copy(countdownRemainingMs = remaining) }
                    }
                    continue
                }

                if (!state.raceOngoing) continue

                // 1. Decelerate ability cooldown
                if (abilityCooldownMs > 0) {
                    abilityCooldownMs = (abilityCooldownMs - deltaMs).coerceAtLeast(0)
                }

                // 2. Physics & Kinematics updates for all racers
                val updatedRacers = state.racers.map { racer ->
                    if (racer.isFinished) return@map racer

                    // Compute physics characteristics based on scaling rules
                    val topSpeed = PhysicsEngine.calculateTopSpeed(racer.template.baseSpeed, racer.scaleFactor)
                    val acceleration = PhysicsEngine.calculateAcceleration(racer.template.baseAcceleration, racer.scaleFactor)
                    val turnScale = PhysicsEngine.calculateTurnResponsiveness(racer.template.baseHandling, racer.scaleFactor)

                    // Decelerate stuns and timers
                    var stunTimer = (racer.stunRemainingMs - deltaMs).coerceAtLeast(0)
                    var shieldTimer = (racer.shieldRemainingMs - deltaMs).coerceAtLeast(0)
                    var numbTimer = (racer.numbedRemainingMs - deltaMs).coerceAtLeast(0)

                    // Steer & Movement update
                    var currentSpeed = racer.speed
                    var updatedX = racer.x

                    if (stunTimer > 0) {
                        // Fully stunned, brake hard
                        currentSpeed = (currentSpeed - 8.0f * (deltaMs / 1000f)).coerceAtLeast(0f)
                    } else {
                        // Accelerate
                        currentSpeed = (currentSpeed + acceleration * (deltaMs / 1000f)).coerceAtMost(topSpeed)

                        // If player is numbed, controls are unstable / jittery or reversed
                        var currentSteer = if (racer.isPlayer) playerSteerInput else computeBOTSteering(racer, state.track, state.obstacles)
                        
                        if (numbTimer > 0) {
                            // Reverse sterling input and add visual jitter
                            currentSteer = -currentSteer + (sin(System.currentTimeMillis() * 0.05f) * 0.15f)
                        }

                        // Apply turning motion
                        updatedX += currentSteer * turnScale * 0.05f
                    }

                    // Border collision dynamics (scaled based on creature size)
                    // Massive scales squeeze within narrow lane bounds and slam margins easily!
                    val sizeSafetyMargin = (1.0f - (racer.scaleFactor * 0.06f)).coerceAtLeast(0.3f)
                    
                    if (abs(updatedX) > sizeSafetyMargin) {
                        // Border crash!
                        updatedX = if (updatedX > 0) sizeSafetyMargin else -sizeSafetyMargin
                        
                        // Large scale creatures plow/slide borders with minor speed penalties; small scales rebound/halt instantly
                        val speedCrashPenalty = if (racer.scaleFactor >= 4.0f) 0.15f else 0.45f
                        currentSpeed *= (1.0f - speedCrashPenalty * (deltaMs / 200f)).coerceAtLeast(0.1f)
                        
                        if (racer.isPlayer && Random.nextFloat() < 0.2f) {
                            addLog("⚠️ SQUEEZE: Sized too large for lane! Scraped the track margin.")
                        }
                    }

                    // Move racer forward along z
                    val nextZ = racer.z + currentSpeed * (deltaMs / 1000f)

                    // Check Lap and Finish line
                    val trackLength = state.track.lengthMeters
                    var currentLap = racer.currentLap
                    var isFinished = false
                    var finishTimeMs = racer.finishTimeMs

                    if (nextZ >= trackLength * currentLap) {
                        if (currentLap >= state.maxLaps) {
                            isFinished = true
                            finishTimeMs = state.durationElapsedMs
                            if (racer.isPlayer) {
                                addLog("🏆 FINISHED! You completed the race in ${formatMinsSecs(finishTimeMs)}!")
                            } else {
                                addLog("🏁 ${racer.name} crossed the finish line!")
                            }
                        } else {
                            currentLap += 1
                            if (racer.isPlayer) {
                                addLog("🔔 LAP $currentLap! Pitch up velocity!")
                            }
                        }
                    }

                    racer.copy(
                        x = updatedX,
                        z = nextZ,
                        speed = currentSpeed,
                        stunRemainingMs = stunTimer,
                        shieldRemainingMs = shieldTimer,
                        numbedRemainingMs = numbTimer,
                        currentLap = currentLap,
                        isFinished = isFinished,
                        finishTimeMs = finishTimeMs,
                        massKg = racer.template.massKg
                    )
                }

                // 3. Update dynamic placements (order by distance 'z' and lap desc)
                val sortedRacers = updatedRacers.sortedWith(
                    compareByDescending<RacerState> { it.isFinished }
                        .thenBy { if (it.isFinished) it.finishTimeMs else 1L }
                        .thenByDescending { it.currentLap }
                        .thenByDescending { it.z }
                )

                val withPlacements = sortedRacers.mapIndexed { idx, racer ->
                    racer.copy(currentPlacement = idx + 1)
                }

                // 4. Update projectiles physics
                val remainingProjectiles = mutableListOf<ProjectileState>()
                var racersMutation = withPlacements.toMutableList()

                state.projectiles.forEach { proj ->
                    val nextZ = proj.z + proj.speed * (deltaMs / 1000f)
                    var isActive = nextZ < state.track.lengthMeters * state.maxLaps

                    // Collision detection with opposing creatures
                    if (isActive) {
                        val victim = racersMutation.find { racer ->
                            racer.id != proj.firedByRacerId && !racer.isFinished && abs(racer.z - nextZ) < (racer.scaleFactor * 3.5f + 1.5f) && abs(racer.x - proj.x) < 0.45f
                        }

                        if (victim != null) {
                            isActive = false // projectile explodes
                            
                            viewModelScope.launch {
                                if (victim.isInvincible) {
                                    addLog("🛡️ BLOCKED: ${victim.name} deflected projectile with biological shields!")
                                } else {
                                    val logMsg = when (proj.type) {
                                        AbilityType.OFFENSIVE_HARM -> {
                                            // Apply heavy speed reduction & stun
                                            updateRacerInList(racersMutation, victim.id) {
                                                it.copy(
                                                    stunRemainingMs = 2000L,
                                                    speed = (it.speed * 0.3f)
                                                )
                                            }
                                            "💥 BLOWOUT: ${victim.name} was blasted by offensive spit and spun out!"
                                        }
                                        AbilityType.UTILITY_NUMB -> {
                                            // Apply steering numbing chaos
                                            updateRacerInList(racersMutation, victim.id) {
                                                it.copy(
                                                    numbedRemainingMs = 3500L,
                                                    speed = (it.speed * 0.5f)
                                                )
                                            }
                                            "🌀 NUMBED: ${victim.name}'s central core was frozen! Controls scrambled."
                                        }
                                        else -> ""
                                    }
                                    addLog(logMsg)
                                }
                            }
                        }
                    }

                    if (isActive) {
                        remainingProjectiles.add(proj.copy(z = nextZ))
                    }
                }

                // 5. Check Obstacle impacts
                var obstaclesMutation = state.obstacles.map { obs ->
                    if (obs.isSmashed) return@map obs

                    val hittingRacer = racersMutation.find { racer ->
                        !racer.isFinished && abs(racer.z - obs.z) < (racer.scaleFactor * 1.5f + 0.5f) && abs(racer.x - obs.x) < 0.40f
                    }

                    if (hittingRacer != null) {
                        if (hittingRacer.isInvincible) {
                            return@map obs.copy(isSmashed = true) // Smashe during shield
                        }

                        // Scaling Physics Rule:
                        // Huge scales (e.g. Behemoth scale = 8.5) crush rock/plant obstacles directly under step, experiencing minimal deceleration
                        // Tiny scales (e.g. Ant scale = 0.1) suffer absolute speed stoppage (stun)
                        val isHeavyWeight = hittingRacer.scaleFactor >= 4.0f
                        
                        viewModelScope.launch {
                            val msg = if (isHeavyWeight) {
                                updateRacerInList(racersMutation, hittingRacer.id) {
                                    it.copy(speed = it.speed * 0.88f) // Only 12% speed loss
                                }
                                "🏋️ SMASH: ${hittingRacer.name} (Huge Scale) pulverized a ${obs.emoji} obstacle without breaking stride!"
                            } else {
                                updateRacerInList(racersMutation, hittingRacer.id) {
                                    it.copy(
                                        speed = it.speed * 0.25f, // 75% speed loss
                                        stunRemainingMs = 1200L
                                    )
                                }
                                "🤕 TRIP: ${hittingRacer.name} (Low Scale) crashed headlong into a ${obs.emoji} and bounced back!"
                            }
                            addLog(msg)
                        }

                        obs.copy(isSmashed = true)
                    } else {
                        obs
                    }
                }

                // 6. Check if Racer Collisions Occurred (Elastic Knockback based on scale)
                val racersCount = racersMutation.size
                for (i in 0 until racersCount) {
                    for (j in i + 1 until racersCount) {
                        val r1 = racersMutation[i]
                        val r2 = racersMutation[j]

                        if (!r1.isFinished && !r2.isFinished &&
                            abs(r1.z - r2.z) < (r1.scaleFactor + r2.scaleFactor * 0.6f) &&
                            abs(r1.x - r2.x) < 0.35f
                        ) {
                            // Compute collision rebound forces!
                            val pResult = PhysicsEngine.calculateCollision(
                                attackerScale = r1.scaleFactor,
                                attackerMass = r1.massKg,
                                attackerSpeed = r1.speed,
                                victimScale = r2.scaleFactor,
                                victimMass = r2.massKg,
                                victimSpeed = r2.speed
                            )

                            addLog(pResult.logDescription)

                            // Apply knockback offsets and speed losses
                            updateRacerInList(racersMutation, r1.id) {
                                it.copy(
                                    x = (it.x + pResult.knockbackDistanceX).coerceIn(-1f, 1f),
                                    speed = (it.speed * (1f - pResult.attackerSpeedLossRatio)).coerceAtLeast(1f)
                                )
                            }
                            updateRacerInList(racersMutation, r2.id) {
                                it.copy(
                                    x = (it.x - pResult.knockbackDistanceX).coerceIn(-1f, 1f),
                                    speed = (it.speed * (1f - pResult.victimSpeedLossRatio)).coerceAtLeast(1f),
                                    stunRemainingMs = if (pResult.stunDurationMs > 0 && !it.isInvincible) pResult.stunDurationMs else it.stunRemainingMs
                                )
                            }
                        }
                    }
                }

                // 7. Check overall End-of-Race condition
                val totalTime = state.durationElapsedMs + deltaMs
                val playerState = racersMutation.find { it.isPlayer }
                val isPlayerFinished = playerState?.isFinished == true
                
                // If everyone is finished or player finished, show summary
                val allFinished = racersMutation.all { it.isFinished }
                val isRaceOver = allFinished || isPlayerFinished

                _liveRace.value = state.copy(
                    racers = racersMutation,
                    projectiles = remainingProjectiles,
                    obstacles = obstaclesMutation,
                    durationElapsedMs = totalTime,
                    raceOngoing = !isRaceOver
                )

                if (isRaceOver) {
                    delay(1200) // Brief suspension for player satisfaction
                    saveRaceResultsAndConclude(playerState?.currentPlacement ?: 4, totalTime)
                    break
                }
            }
        }
    }

    private fun updateRacerInList(list: MutableList<RacerState>, id: String, transform: (RacerState) -> RacerState) {
        val idx = list.indexOfFirst { it.id == id }
        if (idx != -1) {
            list[idx] = transform(list[idx])
        }
    }

    private fun computeBOTSteering(bot: RacerState, track: TrackTemplate, obstacles: List<TrackObstacle>): Float {
        // AI guides towards the track center line while dodging nearby active obstacles
        val futureZ = bot.z + bot.speed * 0.4f
        val seg = track.getCurvatureAt(futureZ)
        
        // Curve compensation steer
        var targetX = -seg.curve * 0.25f

        // Scan for nearest active obstacle directly ahead
        val dangerousObstacle = obstacles.firstOrNull { obs ->
            !obs.isSmashed && obs.z > bot.z && (obs.z - bot.z) < 45.0f && abs(obs.x - bot.x) < 0.45f
        }

        if (dangerousObstacle != null) {
            // Divert direction away from the obstacle
            targetX = if (dangerousObstacle.x > 0) -0.6f else 0.6f
        }

        // Steer factor calculation
        val steerDelta = targetX - bot.x
        return steerDelta.coerceIn(-1.0f, 1.0f)
    }

    private fun saveRaceResultsAndConclude(placement: Int, finishTimeMs: Long) {
        viewModelScope.launch {
            // Calculate Evolution rewardpoints based on placement
            val baseEpReward = when (placement) {
                1 -> 120
                2 -> 80
                3 -> 50
                else -> 20
            }

            // Calculate Seasonal Rank points (RP) boost/penalty
            val rpDelta = when (placement) {
                1 -> 50
                2 -> 25
                3 -> 0
                else -> -20
            }

            // Persistence write
            repository.addRewards(baseEpReward, rpDelta)

            _lastRacePlacement.value = placement
            _lastRaceTime.value = formatMinsSecs(finishTimeMs)
            _lastRaceEpReward.value = baseEpReward
            _lastRaceRpReward.value = rpDelta

            navigateTo(RacingScreen.RaceReport)
        }
    }

    private fun formatMinsSecs(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / 60000)
        val hundredths = (ms % 1000) / 10
        return "%02d:%02d.%02d".format(minutes, seconds, hundredths)
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}
