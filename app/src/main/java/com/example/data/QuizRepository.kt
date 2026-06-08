package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class QuizRepository(context: Context) {

    private val db: QuizDatabase = Room.databaseBuilder(
        context.applicationContext,
        QuizDatabase::class.java,
        "pocket_quiz_db"
    ).fallbackToDestResultMigrationOnSchemaMismatch() // Safely rebuild if schemas change
        .build()

    val userDao = db.userDao()
    val questionDao = db.questionDao()
    val tournamentDao = db.tournamentDao()
    val withdrawDao = db.withdrawDao()
    val transactionDao = db.transactionDao()
    val notificationDao = db.notificationDao()

    fun userDao() = userDao
    fun questionDao() = questionDao
    fun tournamentDao() = tournamentDao
    fun withdrawDao() = withdrawDao
    fun transactionDao() = transactionDao
    fun notificationDao() = notificationDao

    // Expose flows directly
    val currentUser: Flow<UserProfile?> = userDao.getCurrentUser()
    val allQuestions: Flow<List<DbQuestion>> = questionDao.getAllQuestions()
    val allTournaments: Flow<List<DbTournament>> = tournamentDao.getAllTournaments()
    val allWithdrawRequests: Flow<List<DbWithdraw>> = withdrawDao.getAllWithdrawRequests()
    val allTransactions: Flow<List<DbTransaction>> = transactionDao.getAllTransactions()
    val allNotifications: Flow<List<DbNotification>> = notificationDao.getAllNotifications()
    val allUsers: Flow<List<UserProfile>> = userDao.getAllUsers()

    suspend fun getQuestionCount(): Int = questionDao.getQuestionCount()

    // Seeds initial mock data if there isn't any
    suspend fun seedInitialDataIfRequired() {
        // 1. Seed Questions if empty
        if (questionDao.getQuestionCount() == 0) {
            val sampleQuestions = listOf(
                DbQuestion(
                    text = "Which language is primarily used for modern Android development?",
                    optionA = "Java",
                    optionB = "Kotlin",
                    optionC = "Swift",
                    optionD = "C++",
                    correctIndex = 1,
                    category = "Technology"
                ),
                DbQuestion(
                    text = "What is the primary color hex of our 'Pocket Quiz' app theme?",
                    optionA = "#7C3AED",
                    optionB = "#FF0000",
                    optionC = "#10B981",
                    optionD = "#3B82F6",
                    correctIndex = 0,
                    category = "Trivia"
                ),
                DbQuestion(
                    text = "Which of the following is NOT a fundamental core feature of Android Jetpack?",
                    optionA = "Room Database",
                    optionB = "Jetpack Compose",
                    optionC = "Direct 3D Graphics Library",
                    optionD = "LiveData / Flow",
                    correctIndex = 2,
                    category = "Technology"
                ),
                DbQuestion(
                    text = "If 100 pocket coins are worth ₹5, how many coins are required to withdraw ₹50?",
                    optionA = "500 Coins",
                    optionB = "800 Coins",
                    optionC = "1000 Coins",
                    optionD = "2000 Coins",
                    correctIndex = 2,
                    category = "Finance"
                ),
                DbQuestion(
                    text = "What is the minimum cash withdraw limit in Pocket Quiz?",
                    optionA = "₹10",
                    optionB = "₹50",
                    optionC = "₹100",
                    optionD = "₹20",
                    correctIndex = 1,
                    category = "Finance"
                )
            )
            for (q in sampleQuestions) {
                questionDao.insert(q)
            }
        }

        // 2. Seed Tournaments if empty
        // Let's seed a couple of tournaments to show scheduled vs ended vs live
        val currentTournaments = tournamentDao.getAllTournaments().firstOrNull() ?: emptyList()
        if (currentTournaments.isEmpty()) {
            val sampleTournaments = listOf(
                DbTournament(
                    title = "Pocket Premium Blitz #103",
                    entryFee = 50,
                    prizePool = 1000,
                    maxPlayers = 100,
                    joinedPlayers = 82,
                    status = "Upcoming",
                    startTime = "Starts in 15 mins",
                    durationSeconds = 180,
                    mockLeaderboardJson = """[
                        {"rank":1,"name":"CryptoKing","score":950,"time":"82s"},
                        {"rank":2,"name":"SlayQuiz","score":900,"time":"89s"},
                        {"rank":3,"name":"MathGenius","score":850,"time":"91s"},
                        {"rank":4,"name":"QuizWiz","score":800,"time":"95s"},
                        {"rank":5,"name":"PocketPro","score":750,"time":"103s"}
                    ]"""
                ),
                DbTournament(
                    title = "Daily Super Math Champion Arena",
                    entryFee = 100,
                    prizePool = 2500,
                    maxPlayers = 50,
                    joinedPlayers = 49,
                    status = "Live",
                    startTime = "Happening Now!",
                    durationSeconds = 120,
                    mockLeaderboardJson = """[
                        {"rank":1,"name":"Riya Sharma","score":980,"time":"65s"},
                        {"rank":2,"name":"Aditya P","score":960,"time":"71s"},
                        {"rank":3,"name":"Karan Malhotra","score":920,"time":"80s"},
                        {"rank":4,"name":"Vikram S","score":890,"time":"85s"},
                        {"rank":5,"name":"Shreya J","score":850,"time":"90s"}
                    ]"""
                ),
                DbTournament(
                    title = "Weekend Grand Clash Master",
                    entryFee = 200,
                    prizePool = 5000,
                    maxPlayers = 100,
                    joinedPlayers = 100,
                    status = "Ended",
                    startTime = "Ended 2 hours ago",
                    durationSeconds = 150,
                    mockLeaderboardJson = """[
                        {"rank":1,"name":"Aditi Sen","score":1000,"time":"59s"},
                        {"rank":2,"name":"QuizMaster","score":980,"time":"64s"},
                        {"rank":3,"name":"BetaSlayer","score":950,"time":"72s"},
                        {"rank":4,"name":"Rahul99","score":940,"time":"75s"},
                        {"rank":5,"name":"Neha_Mehta","score":910,"time":"79s"}
                    ]"""
                )
            )
            for (t in sampleTournaments) {
                tournamentDao.insertOrUpdate(t)
            }
        }

        // 3. Seed Mock Opponents/Users for 1v1 battle matching and Leaderboard
        val existingUsers = userDao.getAllUsers().firstOrNull() ?: emptyList()
        // If there is only the current user or empty, seed standard mock profiles
        if (existingUsers.size < 4) {
            val mockUsers = listOf(
                UserProfile(
                    id = "PQ8741",
                    mobile = "9876543210",
                    name = "Ravi Teja",
                    coins = 1250,
                    checkInStreak = 4,
                    totalCorrectAnswers = 145,
                    totalQuizzesCompleted = 22,
                    totalContestsWon = 5,
                    isCurrentUser = false
                ),
                UserProfile(
                    id = "PQ5209",
                    mobile = "9123456780",
                    name = "Sneha Desai",
                    coins = 2450,
                    checkInStreak = 7,
                    totalCorrectAnswers = 312,
                    totalQuizzesCompleted = 45,
                    totalContestsWon = 12,
                    isCurrentUser = false
                ),
                UserProfile(
                    id = "PQ1138",
                    mobile = "8899887766",
                    name = "Alex Mercer",
                    coins = 80,
                    checkInStreak = 1,
                    totalCorrectAnswers = 35,
                    totalQuizzesCompleted = 8,
                    totalContestsWon = 1,
                    isCurrentUser = false
                )
            )
            for (u in mockUsers) {
                userDao.insertOrUpdate(u)
            }
        }

        // 4. Seed initial banners / system-notifications if empty
        val existingNotifications = db.notificationDao().getAllNotifications().firstOrNull() ?: emptyList()
        if (existingNotifications.isEmpty()) {
            val sampleNotifications = listOf(
                DbNotification(
                    title = "Welcome to Pocket Quiz! 🚀",
                    message = "Start playing 1v1 Battles or Single Math Quiz solver and earn real-cash equivalence coins!",
                    type = "Reward"
                ),
                DbNotification(
                    title = "₹50 Real Cash Withdrawable Pool",
                    message = "Earn 1000 coins and easily withdraw to your UPI wallet directly in seconds! Day-limit: 1 withdrawal/24h.",
                    type = "Withdrawal"
                ),
                DbNotification(
                    title = "Daily Super Math Champion Live!",
                    message = "Entry fee: 100 coins, prize pool: 2500 coins! Top 5 players will win guaranteed dynamic rewards.",
                    type = "Contest"
                )
            )
            for (n in sampleNotifications) {
                db.notificationDao().insert(n)
            }
        }
    }

    // Helper functions
    suspend fun saveCurrentUser(profile: UserProfile) {
        userDao.insertOrUpdate(profile)
    }

    suspend fun updateCurrentUserCoins(coinsChange: Int, description: String, userId: String) {
        val user = userDao.getUserById(userId) ?: return
        val newCoins = (user.coins + coinsChange).coerceAtLeast(0)
        val updatedUser = user.copy(coins = newCoins)
        userDao.insertOrUpdate(updatedUser)

        // Write transaction
        transactionDao.insert(
            DbTransaction(
                userId = userId,
                title = description,
                coinDiff = coinsChange
            )
        )
    }

    suspend fun banUser(userId: String) {
        val user = userDao.getUserById(userId) ?: return
        userDao.insertOrUpdate(user.copy(isBanned = true))
    }

    suspend fun unbanUser(userId: String) {
        val user = userDao.getUserById(userId) ?: return
        userDao.insertOrUpdate(user.copy(isBanned = false))
    }

    suspend fun adjustCoinsAdmin(userId: String, diff: Int) {
        val user = userDao.getUserById(userId) ?: return
        val newCoins = (user.coins + diff).coerceAtLeast(0)
        userDao.insertOrUpdate(user.copy(coins = newCoins))
        transactionDao.insert(
            DbTransaction(
                userId = userId,
                title = "Admin Coin Adjustment",
                coinDiff = diff
            )
        )
    }
}

// Safely configure Room to recreate table structure if mismatch, 
// to prevent crashing developers during incremental updates.
fun <T : RoomDatabase.Builder<X>, X : RoomDatabase> T.fallbackToDestResultMigrationOnSchemaMismatch(): T {
    this.fallbackToDestructiveMigration()
    return this
}
