package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. ENTITY DEFINITIONS
// ==========================================

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String, // e.g., "PQ91038"
    val mobile: String,
    val name: String,
    val coins: Int,
    val checkInStreak: Int = 1,
    val lastCheckInTimestamp: Long = 0,
    val dailyMathCoinsEarned: Int = 0,
    val dailyMathCoinsResetTimestamp: Long = 0,
    val isBanned: Boolean = false,
    val isCurrentUser: Boolean = false,
    val totalCorrectAnswers: Int = 0,
    val totalQuizzesCompleted: Int = 0,
    val totalContestsWon: Int = 0
)

@Entity(tableName = "questions")
data class DbQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctIndex: Int, // 0 to 3
    val category: String = "Admin Q"
)

@Entity(tableName = "tournaments")
data class DbTournament(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val entryFee: Int,
    val prizePool: Int,
    val maxPlayers: Int,
    val joinedPlayers: Int,
    val status: String, // "Upcoming", "Live", "Ended"
    val startTime: String, // formatted representation e.g. "Tomorrow 10:00 AM" or time string
    val durationSeconds: Int = 120,
    val mockLeaderboardJson: String = "" // Simulates players with scores for the evaluation
)

@Entity(tableName = "withdraw_requests")
data class DbWithdraw(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val upiId: String,
    val amountInRs: Int,
    val coinCost: Int,
    val status: String, // "Pending", "Approved", "Rejected"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class DbTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val title: String,
    val coinDiff: Int, // positive or negative e.g. +2, -100
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class DbNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val type: String = "Alert", // "Contest", "Result", "Withdrawal", "Reward"
    val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// 2. DATA ACCESS OBJECT (DAO) DEFINITIONS
// ==========================================

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles")
    fun getAllUsers(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)

    @Update
    suspend fun update(profile: UserProfile)

    @Query("SELECT * FROM user_profiles WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserProfile?

    @Query("DELETE FROM user_profiles WHERE isCurrentUser = 1")
    suspend fun deleteCurrentUser()
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY id DESC")
    fun getAllQuestions(): Flow<List<DbQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: DbQuestion)

    @Delete
    suspend fun delete(question: DbQuestion)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int
}

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY id DESC")
    fun getAllTournaments(): Flow<List<DbTournament>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(tournament: DbTournament)

    @Query("SELECT * FROM tournaments WHERE id = :id LIMIT 1")
    suspend fun getTournamentById(id: Int): DbTournament?

    @Query("DELETE FROM tournaments WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface WithdrawDao {
    @Query("SELECT * FROM withdraw_requests ORDER BY timestamp DESC")
    fun getAllWithdrawRequests(): Flow<List<DbWithdraw>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: DbWithdraw)

    @Update
    suspend fun update(request: DbWithdraw)

    @Query("SELECT * FROM withdraw_requests WHERE id = :requestId LIMIT 1")
    suspend fun getRequestById(requestId: Int): DbWithdraw?
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<DbTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: DbTransaction)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<DbNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(n: DbNotification)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}

// ==========================================
// 3. DATABASE DEFINITION
// ==========================================

@Database(
    entities = [
        UserProfile::class,
        DbQuestion::class,
        DbTournament::class,
        DbWithdraw::class,
        DbTransaction::class,
        DbNotification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun questionDao(): QuestionDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun withdrawDao(): WithdrawDao
    abstract fun transactionDao(): TransactionDao
    abstract fun notificationDao(): NotificationDao
}
