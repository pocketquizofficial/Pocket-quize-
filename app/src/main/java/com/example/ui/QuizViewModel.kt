package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuizRepository(application)

    // Reactive database streams
    val currentUser = repository.currentUser
    val allQuestions = repository.allQuestions
    val allTournaments = repository.allTournaments
    val allWithdrawRequests = repository.allWithdrawRequests
    val allTransactions = repository.allTransactions
    val allNotifications = repository.allNotifications
    val allPlayers = repository.allUsers

    // --- UI State Variables ---
    var isLoggedIn by mutableStateOf(false)
        private set

    // Auth screen states
    var authMobileNumber by mutableStateOf("")
    var authOtpCode by mutableStateOf("")
    var authStep by mutableStateOf("MobileInput") // "MobileInput", "OtpInput", "Completed"
    var authError by mutableStateOf<String?>(null)
    var isVerifyingOtp by mutableStateOf(false)

    // Current Math Quiz State
    var mathQuestionText by mutableStateOf("")
    var mathOptions by mutableStateOf<List<String>>(emptyList())
    var mathCorrectAnswerIndex by mutableStateOf(0)
    var mathSelectedAnswerIndex by mutableStateOf<Int?>(null)
    var isMathQuestionAnswered by mutableStateOf(false)
    var mathScoreMessage by mutableStateOf("")

    // 1v1 Battle Arena states
    var battleState by mutableStateOf("Idle") // "Idle", "Searching", "Matched", "Playing", "ScoreReview"
    var battleOpponent by mutableStateOf<UserProfile?>(null)
    var battleQuestionIndex by mutableStateOf(0)
    var battleQuestions by mutableStateOf<List<MathQuestion>>(emptyList())
    var battleUserScore by mutableStateOf(0)
    var battleOpponentScore by mutableStateOf(0)
    var battleUserSolvedCount by mutableStateOf(0)
    var battleOpponentSolvedCount by mutableStateOf(0)
    var battleTimeElapsedUser by mutableStateOf(0)
    var battleTimeElapsedOpponent by mutableStateOf(0)
    var battleSelectedAnswer by mutableStateOf<Int?>(null)
    var isBattleQuestionAnswered by mutableStateOf(false)
    var battleStatusMessage by mutableStateOf("")
    var battleEntryFee by mutableStateOf(20)

    // Active Tournament States
    var selectedTournament by mutableStateOf<DbTournament?>(null)
    var tournamentPlayState by mutableStateOf("Idle") // "Idle", "JoinedAlert", "Playing", "Completed"
    var tournamentQuestionIndex by mutableStateOf(0)
    var tournamentUserScore by mutableStateOf(0)
    var tournamentSelectedAnswer by mutableStateOf<Int?>(null)
    var isTournamentAnswered by mutableStateOf(false)

    // Wallet UPI Request states
    var withdrawUpiId by mutableStateOf("")
    var withdrawAmountRs by mutableStateOf(50) // Preset ₹50 (1000 coins)
    var walletMessage by mutableStateOf<String?>(null)
    var walletErrorMessage by mutableStateOf<String?>(null)

    // Navigation Active Tab state
    var currentTab by mutableStateOf("Home") // "Home", "SingleMath", "1v1", "Tournaments", "Wallet", "Leaderboard", "Profile", "Admin"

    // Notifications status
    var hasUnreadNotifications by mutableStateOf(true)

    init {
        viewModelScope.launch {
            // Pre-seed mock data
            repository.seedInitialDataIfRequired()

            // Auto Log-In check if single user profile is active
            currentUser.collect { user ->
                if (user != null) {
                    isLoggedIn = true
                }
            }
        }
    }

    // --- Authentication Actions ---
    fun requestOtp() {
        if (authMobileNumber.length < 10) {
            authError = "Please enter a valid 10-digit mobile number."
            return
        }
        authError = null
        authStep = "OtpInput"
        // Pretend default test OTP represents standard sandbox "123456"
        authOtpCode = ""
    }

    fun verifyOtp() {
        if (authOtpCode != "123456" && authOtpCode.length < 6) {
            authError = "Invalid standard Sandbox OTP code. Please use '123456'"
            return
        }
        viewModelScope.launch {
            isVerifyingOtp = true
            delay(1000) // Aesthetic delay for progress simulation
            val defaultUserId = "PQ" + Random.nextInt(100000, 999999)
            val newUser = UserProfile(
                id = defaultUserId,
                mobile = authMobileNumber,
                name = "Pocket Gamer",
                coins = 250, // Starter Coins!
                isCurrentUser = true,
                checkInStreak = 1,
                lastCheckInTimestamp = 0
            )
            repository.saveCurrentUser(newUser)
            // Add a startup transaction
            repository.transactionDao().insert(
                DbTransaction(
                    userId = defaultUserId,
                    title = "New Account Welcome Bonus",
                    coinDiff = 250
                )
            )
            authStep = "Completed"
            isLoggedIn = true
            currentTab = "Home"
            isVerifyingOtp = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Delete current user to reset simulation
            repository.userDao().deleteCurrentUser()
            authStep = "MobileInput"
            authMobileNumber = ""
            authOtpCode = ""
            isLoggedIn = false
            currentTab = "Home"
        }
    }

    // --- Daily Check-In Reward ---
    fun claimDailyReward(user: UserProfile) {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        
        // Prevent claiming reward twice under same 24 hour slot
        if (now - user.lastCheckInTimestamp < oneDayMillis && user.lastCheckInTimestamp > 0) {
            // User already checked-in recently
            return
        }

        viewModelScope.launch {
            // Determine streak day (1 to 7 resets)
            val newStreak = if (now - user.lastCheckInTimestamp < (oneDayMillis * 2)) {
                if (user.checkInStreak >= 7) 1 else user.checkInStreak + 1
            } else {
                1
            }

            val rewardCoins = when (newStreak) {
                1 -> 5
                2 -> 10
                3 -> 15
                4 -> 20
                5 -> 25
                6 -> 30
                else -> 50 // Day 7
            }

            val updatedUser = user.copy(
                coins = user.coins + rewardCoins,
                checkInStreak = newStreak,
                lastCheckInTimestamp = now,
                totalQuizzesCompleted = user.totalQuizzesCompleted + 1
            )
            repository.saveCurrentUser(updatedUser)

            // Log Transaction
            repository.transactionDao().insert(
                DbTransaction(
                    userId = user.id,
                    title = "Day $newStreak Daily Check-in Reward",
                    coinDiff = rewardCoins
                )
            )

            // Alert Notification
            repository.notificationDao().insert(
                DbNotification(
                    title = "Pocket reward Claimed! 🎁",
                    message = "You received +$rewardCoins Daily Coins. Play more to double your streak tomorrow!",
                    type = "Reward"
                )
            )
        }
    }

    // --- Single Math Quiz Generator ---
    fun generateMathQuestion(user: UserProfile) {
        mathSelectedAnswerIndex = null
        isMathQuestionAnswered = false
        mathScoreMessage = ""

        // Generate Math Addition, Subtraction or Multiplication
        val type = Random.nextInt(3) // 0: +, 1: -, 2: *
        var a = 0
        var b = 0
        var correctVal = 0
        var expression = ""

        when (type) {
            0 -> {
                a = Random.nextInt(10, 99)
                b = Random.nextInt(10, 99)
                correctVal = a + b
                expression = "$a + $b = ?"
            }
            1 -> {
                a = Random.nextInt(30, 99)
                b = Random.nextInt(10, a) // Positive result
                correctVal = a - b
                expression = "$a - $b = ?"
            }
            2 -> {
                a = Random.nextInt(2, 12)
                b = Random.nextInt(3, 15)
                correctVal = a * b
                expression = "$a × $b = ?"
            }
        }

        mathQuestionText = expression

        // Generate answers choices
        val choices = mutableSetOf(correctVal)
        while (choices.size < 4) {
            val deviation = Random.nextInt(-15, 15)
            val fakeChoice = correctVal + deviation
            if (fakeChoice != correctVal && fakeChoice > 0) {
                choices.add(fakeChoice)
            }
        }

        val sortedOptions = choices.toList().shuffled()
        mathOptions = sortedOptions.map { it.toString() }
        mathCorrectAnswerIndex = sortedOptions.indexOf(correctVal)
    }

    fun submitMathAnswer(user: UserProfile, selectedIndex: Int) {
        if (isMathQuestionAnswered) return
        mathSelectedAnswerIndex = selectedIndex
        isMathQuestionAnswered = true

        val now = System.currentTimeMillis()
        val isResetTime = (now - user.dailyMathCoinsResetTimestamp) > (24 * 60 * 60 * 1000L)
        val dailyEarned = if (isResetTime) 0 else user.dailyMathCoinsEarned

        viewModelScope.launch {
            if (selectedIndex == mathCorrectAnswerIndex) {
                // Verify 200 Coin Cap checking
                if (dailyEarned >= 200) {
                    mathScoreMessage = "Correct! But you hit your 200 Daily Math Coin Limit today."
                    val updatedUser = user.copy(
                        totalCorrectAnswers = user.totalCorrectAnswers + 1
                    )
                    repository.saveCurrentUser(updatedUser)
                } else {
                    mathScoreMessage = "Correct Answer! Earned +2 Coins 🎉"
                    val updatedUser = user.copy(
                        coins = user.coins + 2,
                        dailyMathCoinsEarned = dailyEarned + 2,
                        dailyMathCoinsResetTimestamp = if (isResetTime) now else user.dailyMathCoinsResetTimestamp,
                        totalCorrectAnswers = user.totalCorrectAnswers + 1
                    )
                    repository.saveCurrentUser(updatedUser)
                    repository.transactionDao().insert(
                        DbTransaction(
                            userId = user.id,
                            title = "Math Correct Answer (+2)",
                            coinDiff = 2
                        )
                    )
                }
            } else {
                mathScoreMessage = "Oops! Wrong answer. Deducted -1 Coin 💔"
                val updatedUser = user.copy(
                    coins = (user.coins - 1).coerceAtLeast(0)
                )
                repository.saveCurrentUser(updatedUser)
                repository.transactionDao().insert(
                    DbTransaction(
                        userId = user.id,
                        title = "Math Incorrect Penalty (-1)",
                        coinDiff = -1
                    )
                )
            }
        }
    }

    // --- 1 VS 1 Battle Arena Matchmaking & Play ---
    fun start1v1Matchmaking(user: UserProfile, entryFeeVal: Int) {
        if (user.coins < entryFeeVal) {
            battleStatusMessage = "Insufficient pocket coins for this Entry Fee!"
            return
        }
        battleEntryFee = entryFeeVal
        battleState = "Searching"
        battleStatusMessage = "Finding active online players..."
        
        viewModelScope.launch {
            // Deduct Entry Fee instantly
            val decUser = user.copy(coins = user.coins - entryFeeVal)
            repository.saveCurrentUser(decUser)
            repository.transactionDao().insert(
                DbTransaction(
                    userId = user.id,
                    title = "1v1 Match entry fee deducted",
                    coinDiff = -entryFeeVal
                )
            )

            delay(2500) // Radar scanning simulated scan duration

            // Select an opponent randomly from our seeded players
            val playersList = repository.userDao().getAllUsers().first().filter { !it.isCurrentUser }
            battleOpponent = if (playersList.isNotEmpty()) {
                playersList.random()
            } else {
                UserProfile("PQ_BOT", "0000000000", "NinjaPawn", 900, isCurrentUser = false)
            }

            battleState = "Matched"
            battleStatusMessage = "Opponent matched! Initializing battle arena..."
            delay(2000)

            // Setup 10 identical, auto-generated math equations to compete
            val questionsSet = mutableListOf<MathQuestion>()
            for (i in 1..10) {
                val operationType = Random.nextInt(3) // 0: +, 1: -, 2: *
                var mathA = 0
                var mathB = 0
                var correctAns = 0
                var formulaStr = ""
                when (operationType) {
                    0 -> {
                        mathA = Random.nextInt(5, 50)
                        mathB = Random.nextInt(5, 50)
                        correctAns = mathA + mathB
                        formulaStr = "$mathA + $mathB"
                    }
                    1 -> {
                        mathA = Random.nextInt(20, 50)
                        mathB = Random.nextInt(2, mathA)
                        correctAns = mathA - mathB
                        formulaStr = "$mathA - $mathB"
                    }
                    2 -> {
                        mathA = Random.nextInt(2, 9)
                        mathB = Random.nextInt(2, 9)
                        correctAns = mathA * mathB
                        formulaStr = "$mathA × $mathB"
                    }
                }
                
                // standard choices
                val setOfAnswers = mutableSetOf(correctAns)
                while (setOfAnswers.size < 4) {
                    val randomDev = Random.nextInt(-10, 10)
                    val fake = correctAns + randomDev
                    if (fake != correctAns && fake > 0) setOfAnswers.add(fake)
                }
                val shuffledChoices = setOfAnswers.toList().shuffled()
                questionsSet.add(
                    MathQuestion(
                        expression = formulaStr,
                        options = shuffledChoices.map { it.toString() },
                        correctChoiceIndex = shuffledChoices.indexOf(correctAns)
                    )
                )
            }

            battleQuestions = questionsSet
            battleQuestionIndex = 0
            battleUserScore = 0
            battleOpponentScore = 0
            battleUserSolvedCount = 0
            battleOpponentSolvedCount = 0
            battleTimeElapsedUser = 0
            battleTimeElapsedOpponent = 0
            battleSelectedAnswer = null
            isBattleQuestionAnswered = false
            battleState = "Playing"

            // Parallel background simulation of the matched opponent solving questions 1 by 1
            viewModelScope.launch {
                simulateOpponentProgress()
            }
        }
    }

    private suspend fun simulateOpponentProgress() {
        while (battleState == "Playing" && battleOpponentSolvedCount < 10) {
            // Simulated opponent takes between 1.5 to 3.5 seconds to answer a question
            val decisionDuration = Random.nextLong(1500, 3500)
            delay(decisionDuration)
            if (battleState != "Playing") break

            val opponentCorrect = Random.nextFloat() < 0.82f // 82% correct solve percentage for bot
            if (opponentCorrect) {
                battleOpponentScore += 10
            }
            battleOpponentSolvedCount++
            battleTimeElapsedOpponent += (decisionDuration / 1000).toInt()
        }
    }

    fun submitBattleAnswer(user: UserProfile, selection: Int) {
        if (isBattleQuestionAnswered) return
        battleSelectedAnswer = selection
        isBattleQuestionAnswered = true

        val currentQ = battleQuestions.getOrNull(battleQuestionIndex) ?: return
        if (selection == currentQ.correctChoiceIndex) {
            battleUserScore += 10
        }
        battleUserSolvedCount++
        
        // Count typical thinking time duration (e.g. random 2s per submission)
        battleTimeElapsedUser += Random.nextInt(1, 4)

        viewModelScope.launch {
            delay(800) // slight visual cue highlight
            nextBattleQuestion(user)
        }
    }

    private fun nextBattleQuestion(user: UserProfile) {
        if (battleQuestionIndex < 9) {
            battleQuestionIndex++
            battleSelectedAnswer = null
            isBattleQuestionAnswered = false
        } else {
            // User completed all 10 questions!
            viewModelScope.launch {
                battleStatusMessage = "Evaluating scores..."
                // wait for opponent to complete if they haven't finished
                while (battleOpponentSolvedCount < 10) {
                    delay(500)
                }
                evaluateBattleResults(user)
            }
        }
    }

    private suspend fun evaluateBattleResults(user: UserProfile) {
        battleState = "ScoreReview"
        val userWin: Boolean
        val prizeCoins = battleEntryFee * 2

        if (battleUserScore > battleOpponentScore) {
            userWin = true
            battleStatusMessage = "Congratulations! You Won the Match! 🏆"
        } else if (battleUserScore < battleOpponentScore) {
            userWin = false
            battleStatusMessage = "Defeat! Your opponent scored higher."
        } else {
            // Tie-breaker based on elapsed time
            if (battleTimeElapsedUser < battleTimeElapsedOpponent) {
                userWin = true
                battleStatusMessage = "Tie Breaker Win! You solved identical questions faster (User: ${battleTimeElapsedUser}s VS Opponent: ${battleTimeElapsedOpponent}s) ⚡"
            } else {
                userWin = false
                battleStatusMessage = "Tie Breaker Loss! Match score is tied, but your opponent was faster."
            }
        }

        if (userWin) {
            // Award Prize Pool!
            val updatedUser = user.copy(
                coins = user.coins + prizeCoins,
                totalContestsWon = user.totalContestsWon + 1,
                totalQuizzesCompleted = user.totalQuizzesCompleted + 1
            )
            repository.saveCurrentUser(updatedUser)
            repository.transactionDao().insert(
                DbTransaction(
                    userId = user.id,
                    title = "1v1 Battle Victory prize (+${prizeCoins})",
                    coinDiff = prizeCoins
                )
            )
        } else {
            val updatedUser = user.copy(
                totalQuizzesCompleted = user.totalQuizzesCompleted + 1
            )
            repository.saveCurrentUser(updatedUser)
        }
    }

    fun finishBattleSession() {
        battleState = "Idle"
        currentTab = "Home"
    }

    // --- Tournament Registration and Active Simulation ---
    fun selectTournamentItem(tournament: DbTournament) {
        selectedTournament = tournament
    }

    fun joinTournament(user: UserProfile, tournament: DbTournament) {
        if (user.coins < tournament.entryFee) {
            walletErrorMessage = "Insufficient Coins to registers for this tournament!"
            return
        }

        viewModelScope.launch {
            // Deduct Fee
            val updatedUser = user.copy(coins = user.coins - tournament.entryFee)
            repository.saveCurrentUser(updatedUser)
            
            val updatedTournament = tournament.copy(joinedPlayers = tournament.joinedPlayers + 1)
            repository.tournamentDao().insertOrUpdate(updatedTournament)
            selectedTournament = updatedTournament

            repository.transactionDao().insert(
                DbTransaction(
                    userId = user.id,
                    title = "Registered: ${tournament.title}",
                    coinDiff = -tournament.entryFee
                )
            )

            // Start Live tournament layout simulator instantly for demonstration play
            playTournamentSimulation(user, updatedTournament)
        }
    }

    private fun playTournamentSimulation(user: UserProfile, tournament: DbTournament) {
        tournamentPlayState = "JoinedAlert"
        viewModelScope.launch {
            delay(2000)
            tournamentUserScore = 0
            tournamentQuestionIndex = 0
            tournamentSelectedAnswer = null
            isTournamentAnswered = false
            tournamentPlayState = "Playing"
        }
    }

    fun submitTournamentAnswer(user: UserProfile, selectionIndex: Int) {
        if (isTournamentAnswered) return
        tournamentSelectedAnswer = selectionIndex
        isTournamentAnswered = true

        // mock addition answer verification
        if (selectionIndex == 1) { // pretend 1 is correct answer option
            tournamentUserScore += 100
        }

        viewModelScope.launch {
            delay(800)
            if (tournamentQuestionIndex < 4) { // 5 total challenge tasks
                tournamentQuestionIndex++
                tournamentSelectedAnswer = null
                isTournamentAnswered = false
            } else {
                // Completed! Calculate prize pools
                concludeTournamentResult(user)
            }
        }
    }

    private suspend fun concludeTournamentResult(user: UserProfile) {
        tournamentPlayState = "Completed"
        
        // Random position allocation out of 50-100 tournament entries
        val userRank = if (tournamentUserScore >= 400) {
            Random.nextInt(1, 4) // Top 5
        } else if (tournamentUserScore >= 200) {
            Random.nextInt(4, 9)
        } else {
            Random.nextInt(10, 45)
        }

        val prizeDistribution = when (userRank) {
            1 -> (selectedTournament?.prizePool ?: 1000) * 40 / 100 // 40% to 1st
            2 -> (selectedTournament?.prizePool ?: 1000) * 20 / 100
            3 -> (selectedTournament?.prizePool ?: 1000) * 15 / 100
            4 -> (selectedTournament?.prizePool ?: 1000) * 10 / 100
            5 -> (selectedTournament?.prizePool ?: 1000) * 5 / 100
            else -> 0
        }

        if (prizeDistribution > 0) {
            val updatedUser = user.copy(
                coins = user.coins + prizeDistribution,
                totalContestsWon = user.totalContestsWon + 1
            )
            repository.saveCurrentUser(updatedUser)
            repository.transactionDao().insert(
                DbTransaction(
                    userId = user.id,
                    title = "Prize: Rank $userRank in ${selectedTournament?.title}",
                    coinDiff = prizeDistribution
                )
            )

            repository.notificationDao().insert(
                DbNotification(
                    title = "Tournament Prize Winner! 🎖️",
                    message = "You ranked #$userRank in ${selectedTournament?.title} and claimed +$prizeDistribution coins!",
                    type = "Result"
                )
            )
        } else {
            // No prize but finished
            repository.notificationDao().insert(
                DbNotification(
                    title = "Tournament Result Alert",
                    message = "Rank #$userRank in ${selectedTournament?.title}. Keep playing to win next time!",
                    type = "Result"
                )
            )
        }

        // update the specific tournament status to Ended locally so it represents status
        selectedTournament?.let {
            val closedTournament = it.copy(status = "Ended")
            repository.tournamentDao().insertOrUpdate(closedTournament)
        }
    }

    fun exitTournamentBoard() {
        tournamentPlayState = "Idle"
        selectedTournament = null
        currentTab = "Tournaments"
    }

    // --- Wallet Transactions & Unified Withdraw System ---
    fun submitUPIWithdraw(user: UserProfile) {
        walletErrorMessage = null
        walletMessage = null

        // Validate UPI Id
        if (!withdrawUpiId.contains("@") || withdrawUpiId.length < 5) {
            walletErrorMessage = "Invalid UPI ID format! (e.g. user@paytm / name@okaxis)"
            return
        }

        // Standard Coin Economy rate calculations
        // 100 Coins = ₹5, 1000 Coins = ₹50, 2000 Coins = ₹100
        val coinsCostNeeded = withdrawAmountRs * 20

        if (user.coins < coinsCostNeeded) {
            walletErrorMessage = "Insufficient coins! You need $coinsCostNeeded coins to withdraw ₹$withdrawAmountRs"
            return
        }

        if (withdrawAmountRs < 50) {
            walletErrorMessage = "Minimum withdraw limit: ₹50 (1000 Gold Coins)."
            return
        }

        viewModelScope.launch {
            // Verify single daily withdrawal limit rule (No other withdraw under last 24H)
            val requests = repository.withdrawDao().getAllWithdrawRequests().first()
            val last24Hours = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
            val hasRecentWithdrawal = requests.any { it.userId == user.id && it.timestamp > last24Hours }

            if (hasRecentWithdrawal) {
                walletErrorMessage = "One withdrawal request allowed every 24 hours. Please wait."
                return@launch
            }

            // Deduct Coins immediately, status is "Pending Admin Approval"
            val updatedUser = user.copy(coins = user.coins - coinsCostNeeded)
            repository.saveCurrentUser(updatedUser)

            val request = DbWithdraw(
                userId = user.id,
                upiId = withdrawUpiId,
                amountInRs = withdrawAmountRs,
                coinCost = coinsCostNeeded,
                status = "Pending"
            )
            repository.withdrawDao().insert(request)

            repository.transactionDao().insert(
                DbTransaction(
                    userId = user.id,
                    title = "UPI Withdraw ₹$withdrawAmountRs Submission",
                    coinDiff = -coinsCostNeeded
                )
            )

            repository.notificationDao().insert(
                DbNotification(
                    title = "Withdraw Request Received",
                    message = "₹$withdrawAmountRs is submitted for admin authorization code approval.",
                    type = "Withdrawal"
                )
            )

            walletMessage = "Request Submitted! Awaiting Admin Approvals."
            withdrawUpiId = ""
        }
    }

    // --- Admin Dashboard Management Actions ---
    fun approveWithdrawRequest(request: DbWithdraw) {
        viewModelScope.launch {
            val updatedRequest = request.copy(status = "Approved")
            repository.withdrawDao().update(updatedRequest)

            // Alert target user with notification
            repository.notificationDao().insert(
                DbNotification(
                    title = "Withdraw Request Approved! 💰",
                    message = "Your cash transfer of ₹${request.amountInRs} is successfully approved and dispatched via UPI.",
                    type = "Withdrawal"
                )
            )
        }
    }

    fun rejectWithdrawRequest(request: DbWithdraw) {
        viewModelScope.launch {
            val updatedRequest = request.copy(status = "Rejected")
            repository.withdrawDao().update(updatedRequest)

            // Reverse Coins back to user
            val user = repository.userDao().getUserById(request.userId)
            if (user != null) {
                val restoredUser = user.copy(coins = user.coins + request.coinCost)
                repository.saveCurrentUser(restoredUser)

                repository.transactionDao().insert(
                    DbTransaction(
                        userId = request.userId,
                        title = "UPI Draw Rejected (Restored Coins)",
                        coinDiff = request.coinCost
                    )
                )
            }

            // Alert notify
            repository.notificationDao().insert(
                DbNotification(
                    title = "Withdraw Request Declined",
                    message = "Your request of ₹${request.amountInRs} was rejected. Coins are reversed.",
                    type = "Withdrawal"
                )
            )
        }
    }

    fun banPlayer(userId: String) {
        viewModelScope.launch {
            repository.banUser(userId)
        }
    }

    fun unbanPlayer(userId: String) {
        viewModelScope.launch {
            repository.unbanUser(userId)
        }
    }

    fun addCoinsAdmin(userId: String, coins: Int) {
        viewModelScope.launch {
            repository.adjustCoinsAdmin(userId, coins)
        }
    }

    fun removeCoinsAdmin(userId: String, coins: Int) {
        viewModelScope.launch {
            repository.adjustCoinsAdmin(userId, -coins)
        }
    }

    fun createCustomTrivia(text: String, a: String, b: String, c: String, d: String, correct: Int) {
        viewModelScope.launch {
            val q = DbQuestion(
                text = text,
                optionA = a,
                optionB = b,
                optionC = c,
                optionD = d,
                correctIndex = correct
            )
            repository.questionDao().insert(q)
        }
    }

    fun deleteCustomTrivia(id: Int) {
        viewModelScope.launch {
            repository.questionDao().deleteById(id)
        }
    }

    fun publishGlobalNotification(title: String, message: String, type: String) {
        viewModelScope.launch {
            val n = DbNotification(
                title = title,
                message = message,
                type = type
            )
            repository.notificationDao().insert(n)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.notificationDao().clearAll()
        }
    }

    fun mockCheckInOverrule(user: UserProfile) {
        // Dev assistance helper to bypass clock limits to easily test streak claim outputs
        viewModelScope.launch {
            val userReset = user.copy(lastCheckInTimestamp = 0)
            repository.saveCurrentUser(userReset)
        }
    }
}

// Simple supporting standard class for mock 1v1 battle structures
data class MathQuestion(
    val expression: String,
    val options: List<String>,
    val correctChoiceIndex: Int
)
