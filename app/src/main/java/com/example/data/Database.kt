package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Entity to store the player's live ranking, evolution, and available roster
@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val currentRp: Int = 100, // Starts at 100 (Bronze)
    val evolutionPoints: Int = 50, // Starts with 50 EP
    val unlockedCreatureIds: String = "sub_insect_t1,insect_t1,mammal_t1,reptile_t1,avian_t1,fantasy_t1", // comma-separated ids
    val activeCreatureId: String = "insect_t1"
)

// Entity to store the full Seasonal Leaderboard, including AI bots of dynamic scales
@Entity(tableName = "leaderboard")
data class LeaderboardEntryEntity(
    @PrimaryKey val botId: String,
    val name: String,
    val rankType: String, // "Bronze", "Silver", "Gold", "Platinum", "Dragon Tier"
    val rp: Int,
    val creatureTemplateId: String,
    val totalRacesMatched: Int,
    val winRatio: Float,
    val isPlayer: Boolean = false
)

@Dao
interface RacingDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgressDirect(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgressEntity)

    @Query("SELECT * FROM leaderboard ORDER BY rp DESC")
    fun getLeaderboard(): Flow<List<LeaderboardEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntries(entries: List<LeaderboardEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateLeaderboardEntry(entry: LeaderboardEntryEntity)

    @Query("DELETE FROM leaderboard")
    suspend fun clearLeaderboard()
}

@Database(entities = [UserProgressEntity::class, LeaderboardEntryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun racingDao(): RacingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scale_shift_racing_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
