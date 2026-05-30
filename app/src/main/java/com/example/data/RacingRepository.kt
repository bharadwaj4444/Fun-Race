package com.example.data

import com.example.game.CreatureRoster
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class RacingRepository(private val dao: RacingDao) {

    val userProgress: Flow<UserProgressEntity?> = dao.getUserProgress()
    val leaderboard: Flow<List<LeaderboardEntryEntity>> = dao.getLeaderboard()

    suspend fun getOrCreateProgress(): UserProgressEntity {
        var progress = dao.getUserProgressDirect()
        if (progress == null) {
            progress = UserProgressEntity()
            dao.insertUserProgress(progress)
        }
        return progress
    }

    suspend fun saveActiveCreature(creatureId: String) {
        val current = getOrCreateProgress()
        dao.insertUserProgress(current.copy(activeCreatureId = creatureId))
    }

    suspend fun unlockCreature(creatureId: String, cost: Int): Boolean {
        val current = getOrCreateProgress()
        if (current.evolutionPoints >= cost) {
            val unlockedSet = current.unlockedCreatureIds.split(",").toMutableSet()
            if (unlockedSet.add(creatureId)) {
                val updatedList = unlockedSet.joinToString(",")
                val newProgress = current.copy(
                    evolutionPoints = current.evolutionPoints - cost,
                    unlockedCreatureIds = updatedList,
                    activeCreatureId = creatureId
                )
                dao.insertUserProgress(newProgress)
                
                // Also update player's entry on the leaderboard
                updatePlayerLeaderboardEntry(newProgress.currentRp, creatureId)
                return true
            }
        }
        return false
    }

    suspend fun updatePlayerLeaderboardEntry(rp: Int, creatureId: String) {
        val tier = getLeagueTierForRp(rp)
        val entry = LeaderboardEntryEntity(
            botId = "player_id",
            name = "You (Challenger)",
            rankType = tier,
            rp = rp,
            creatureTemplateId = creatureId,
            totalRacesMatched = 0, // In a real app we can track total races
            winRatio = 0.5f,
            isPlayer = true
        )
        dao.updateLeaderboardEntry(entry)
    }

    suspend fun addRewards(epEarned: Int, rpDelta: Int): UserProgressEntity {
        val current = getOrCreateProgress()
        val newRp = (current.currentRp + rpDelta).coerceAtLeast(0)
        val newEp = current.evolutionPoints + epEarned
        val newProgress = current.copy(
            currentRp = newRp,
            evolutionPoints = newEp
        )
        dao.insertUserProgress(newProgress)
        updatePlayerLeaderboardEntry(newRp, newProgress.activeCreatureId)
        
        // Let the AI opponents advance a tiny bit too to simulate active seasonal competition!
        simulateSeasonalAIGrowth()
        return newProgress
    }

    // Seeds initial AI competitors on the leaderboard if empty
    suspend fun seedLeaderboardIfEmpty() {
        val currentList = dao.getLeaderboard().firstOrNull() ?: emptyList()
        if (currentList.isEmpty()) {
            val bots = listOf(
                LeaderboardEntryEntity("bot_1", "Giga Ant 'Spike'", "Gold", 3200, "insect_t3", 42, 0.65f),
                LeaderboardEntryEntity("bot_2", "Mountain Lord", "Gold", 2900, "mammal_t3", 58, 0.61f),
                LeaderboardEntryEntity("bot_3", "Venomous Bite", "Silver", 2200, "reptile_t2", 33, 0.54f),
                LeaderboardEntryEntity("bot_4", "Humming Hurricane", "Silver", 1850, "avian_t1", 29, 0.52f),
                LeaderboardEntryEntity("bot_5", "Wyvern Hatchling", "Bronze", 900, "fantasy_t1", 14, 0.45f),
                LeaderboardEntryEntity("bot_6", "Dread Draconis", "Dragon Tier", 8600, "fantasy_t3", 112, 0.78f),
                LeaderboardEntryEntity("bot_7", "Sonic Kestrel", "Platinum", 5500, "avian_t2", 72, 0.69f),
                LeaderboardEntryEntity("bot_8", "Mammoth Express", "Silver", 1400, "mammal_t3", 48, 0.50f),
                LeaderboardEntryEntity("bot_9", "Emerald Basilisk", "Platinum", 6100, "reptile_t3", 89, 0.72f),
                LeaderboardEntryEntity("bot_10", "Swift Whisper", "Bronze", 300, "mammal_t1", 5, 0.40f)
            )
            dao.insertLeaderboardEntries(bots)
            
            // Insert initial player slot
            val playerProgress = getOrCreateProgress()
            updatePlayerLeaderboardEntry(playerProgress.currentRp, playerProgress.activeCreatureId)
        }
    }

    private suspend fun simulateSeasonalAIGrowth() {
        val allEntries = dao.getLeaderboard().firstOrNull() ?: emptyList()
        allEntries.forEach { entry ->
            if (!entry.isPlayer) {
                // Growth delta (slightly positive random fluctuation)
                val delta = Random.nextInt(-15, 45)
                val newRp = (entry.rp + delta).coerceAtLeast(0)
                val newTier = getLeagueTierForRp(newRp)
                dao.updateLeaderboardEntry(
                    entry.copy(
                        rp = newRp,
                        rankType = newTier,
                        totalRacesMatched = entry.totalRacesMatched + 1,
                        winRatio = ((entry.winRatio * entry.totalRacesMatched) + (if (delta > 10) 1.0f else 0.0f)) / (entry.totalRacesMatched + 1)
                    )
                )
            }
        }
    }

    fun getLeagueTierForRp(rp: Int): String {
        return when {
            rp < 1000 -> "Bronze"
            rp < 2500 -> "Silver"
            rp < 5000 -> "Gold"
            rp < 8000 -> "Platinum"
            else -> "Dragon Tier"
        }
    }
}
