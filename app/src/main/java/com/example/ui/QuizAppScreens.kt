package com.example.ui

import kotlinx.coroutines.delay
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlin.random.Random

// ==========================================
// CENTRALIZED MAIN APP SCREEN COORDINATOR
// ==========================================

@Composable
fun QuizAppMainScreen(viewModel: QuizViewModel) {
    androidx.compose.material3.Surface(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        color = androidx.compose.ui.graphics.Color(0xFFF5F3FF)
    ) {
        androidx.compose.ui.viewinterop.AndroidView(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            factory = { context ->
                android.webkit.WebView(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        databaseEnabled = true
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        mediaPlaybackRequiresUserGesture = false
                    }
                    webViewClient = android.webkit.WebViewClient()
                    webChromeClient = android.webkit.WebChromeClient()
                    loadUrl("file:///android_asset/Web/index.html")
                }
            }
        )
    }
}

// ==========================================
// 1. AUTHENTICATION (MOBILE + OTP SCREEN)
// ==========================================

@Composable
fun AuthScreen(viewModel: QuizViewModel) {
    var showTerms by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEDE9FE), // Light purple background start
                        Color.White,
                        Color(0xFFF3E8FF)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative glowing bubbles for premium theme feel
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color(0x11A855F7), radius = 350f, center = androidx.compose.ui.geometry.Offset(100f, 200f))
            drawCircle(color = Color(0x0FA855F7), radius = 500f, center = androidx.compose.ui.geometry.Offset(900f, 1600f))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x447C3AED), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = GlassWhite),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                // Beautiful App Branding Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary)),
                            RoundedCornerShape(20.dp)
                        )
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Extension,
                        contentDescription = "App Logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "POCKET QUIZ",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = PurplePrimary,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "Play Battles • Solve Math • Withdraw Cash",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                AnimatedContent(
                    targetState = viewModel.authStep,
                    label = "auth_step_transition"
                ) { step ->
                    when (step) {
                        "MobileInput" -> {
                            Column {
                                Text(
                                    text = "Mobile Login",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = PurpleDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = viewModel.authMobileNumber,
                                    onValueChange = { if (it.length <= 10) viewModel.authMobileNumber = it },
                                    label = { Text("10-Digit Mobile Number") },
                                    prefix = { Text("+91 ") },
                                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, "Phone") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("mobile_input_field"),
                                    shape = RoundedCornerShape(16.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                // Quick Developer Info Label
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Info, "Helper", tint = PurplePrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Sandbox active. Enter any 10 digits to login.",
                                            fontSize = 11.sp,
                                            color = PurpleDark,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Premium3DButton(
                                    text = "GET OTP CODE",
                                    onClick = { viewModel.requestOtp() },
                                    modifier = Modifier.fillMaxWidth().testTag("get_otp_btn")
                                )
                            }
                        }
                        "OtpInput" -> {
                            Column {
                                Text(
                                    text = "Verification OTP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = PurpleDark
                                )
                                Text(
                                    text = "Sent OTP to +91 ${viewModel.authMobileNumber}",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = viewModel.authOtpCode,
                                    onValueChange = { viewModel.authOtpCode = it },
                                    label = { Text("Enter 6-Digit OTP") },
                                    placeholder = { Text("Enter 123456") },
                                    leadingIcon = { Icon(Icons.Default.Lock, "OTP") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("otp_input_field"),
                                    shape = RoundedCornerShape(16.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.TipsAndUpdates, "Tip", tint = PurpleSecondary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Verification code is 123456",
                                            fontSize = 12.sp,
                                            color = PurpleSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.authStep = "MobileInput" },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1.2f).height(50.dp)
                                    ) {
                                        Text("BACK", color = PurplePrimary)
                                    }
                                    
                                    Premium3DButton(
                                        text = "VERIFY & LOGIN",
                                        onClick = { viewModel.verifyOtp() },
                                        isLoading = viewModel.isVerifyingOtp,
                                        modifier = Modifier.weight(2f).testTag("verify_otp_btn")
                                    )
                                }
                            }
                        }
                    }
                }

                viewModel.authError?.let { err ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = err, color = CoralRose, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "By signing in, you accept our standard terms of play of 100 pocket coins equivalent to ₹5.",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// ==========================================
// 2. BANNED SECURITY SCREEN
// ==========================================

@Composable
fun BannedScreen(user: UserProfile, viewModel: QuizViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF9FF))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.shadow(8.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = "Banned",
                    tint = CoralRose,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Account Locked",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hello ${user.name} (${user.id}), your mobile profile was locked by admin review due to policy limits or compliance verification.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "To request unban/support, please log in to the admin panel from another profile to manually unlock accounts.",
                    fontSize = 12.sp,
                    color = PurplePrimary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRose)
                ) {
                    Text("LOG OUT")
                }
            }
        }
    }
}

// ==========================================
// CENTRALIZED MAIN LAYOUT WITH NAVIGATION BAR
// ==========================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScaffoldWithBottomNavigation(user: UserProfile, viewModel: QuizViewModel) {
    var showNotifDialog by remember { mutableStateOf(false) }
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle(initialValue = emptyList())
    val unreadCount = notifications.filter { !it.isRead }.size

    Scaffold(
        topBar = {
            Column {
                // Header Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Badge
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PurpleDark
                            )
                            Text(
                                text = "UID: ${user.id}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Coins Balance Card & Notifications Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Golden Cash coins chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PurpleLight,
                            modifier = Modifier.border(1.dp, PurpleAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MonetizationOn,
                                    contentDescription = "Gold Coins",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${user.coins}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PurplePrimary
                                )
                            }
                        }

                        // Wallet button quick jump
                        IconButton(
                            onClick = { viewModel.currentTab = "Wallet" },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFFF3E8FF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet Link",
                                tint = PurplePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Bell notification dialog trigger
                        Box {
                            IconButton(
                                onClick = { showNotifDialog = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFFF3E8FF), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.TopEnd)
                                        .background(CoralRose, CircleShape)
                                        .border(2.dp, Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFF3E8FF))
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.shadow(8.dp)
            ) {
                val tabs = listOf(
                    NavigationTabItem("Home", Icons.Filled.Stars, Icons.Outlined.Stars, "Dash"),
                    NavigationTabItem("SingleMath", Icons.Filled.Calculate, Icons.Outlined.Calculate, "Math"),
                    NavigationTabItem("1v1", Icons.Filled.Bolt, Icons.Outlined.Bolt, "1 VS 1"),
                    NavigationTabItem("Tournaments", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "Contests"),
                    NavigationTabItem("Leaderboard", Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard, "Ranks")
                )
                tabs.forEach { tab ->
                    val selected = viewModel.currentTab == tab.id || (tab.id == "Home" && viewModel.currentTab == "Profile")
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.currentTab = tab.id },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(text = tab.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PurplePrimary,
                            selectedTextColor = PurplePrimary,
                            indicatorColor = Color(0xFFF3E8FF),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SoftBg)
        ) {
            // Render specific screen according to state
            when (viewModel.currentTab) {
                "Home" -> HomeScreen(user, viewModel)
                "SingleMath" -> MathQuizScreen(user, viewModel)
                "1v1" -> MatchmakingSelectionScreen(user, viewModel)
                "Tournaments" -> TournamentsBoardScreen(user, viewModel)
                "Wallet" -> WalletCenterScreen(user, viewModel)
                "Leaderboard" -> LeaderboardsScreen(viewModel)
                "Profile" -> ProfileInformationScreen(user, viewModel)
                "Admin" -> AdminControlDashboardScreen(viewModel)
            }
        }
    }

    // --- POPUP NOTIFICATIONS MANAGER DIALOG ---
    if (showNotifDialog) {
        AlertDialog(
            onDismissRequest = { showNotifDialog = false },
            confirmButton = {
                TextButton(onClick = { viewModel.clearNotifications() }) {
                    Text("CLEAR ALL", color = CoralRose)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotifDialog = false }) {
                    Text("CLOSE", color = PurplePrimary)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.NotificationsActive, "Alerts", tint = PurplePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pocket Notifications")
                }
            },
            text = {
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, "Empty", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No notifications yet", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { n ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F8FF)),
                                border = BorderStroke(1.dp, Color(0xFFEFE9FF))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = n.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = PurplePrimary
                                        )
                                        // Badge tag based on type
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when (n.type) {
                                                "Reward" -> Color(0xFFD1FAE5)
                                                "Withdrawal" -> Color(0xFFFEF3C7)
                                                "Contest" -> Color(0xFFE0E7FF)
                                                else -> Color(0xFFF3F4F6)
                                            }
                                        ) {
                                            Text(
                                                text = n.type,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = n.message,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

data class NavigationTabItem(
    val id: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)

// ==========================================
// 3. DASHBOARD (HOME TAB) WITH CHECK-IN
// ==========================================

@Composable
fun HomeScreen(user: UserProfile, viewModel: QuizViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Slider Layout
        BannerSliderLayout()

        // Daily Check-In horizontal streaks panel
        DailyCheckinCardLayout(user, viewModel)

        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ CHOOSE GAME ARENA",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = PurplePrimary,
                letterSpacing = 1.sp
            )

            // Switch to Admin Quick Access link
            TextButton(
                onClick = { viewModel.currentTab = "Profile" },
                modifier = Modifier.height(30.dp)
            ) {
                Text("View Stats", color = PurpleSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // BENTO GRID CARD 1: 1 VS 1 Battle (Full width card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color(0xFF7C3AED).copy(alpha = 0.2f))
                .clickable { viewModel.currentTab = "1v1" },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFECE7F4))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = Color(0xFFF3E8FF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "MULTIPLAYER BATTLE",
                            color = Color(0xFF7C3AED),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1 VS 1 BATTLE",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color(0xFF1E1B4B)
                    )
                    Text(
                        text = "Entry: 20 Coins • Prize: 40 Coins",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "Challenge other players live in real time",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Small violet shadow 3D button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF7C3AED),
                        modifier = Modifier
                            .shadow(elevation = 3.dp, shape = RoundedCornerShape(12.dp), ambientColor = Color(0xFF5B21B6))
                    ) {
                        Text(
                            text = "PLAY NOW",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Big beautiful circle on right side with emoji
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFF5F3FF), CircleShape)
                        .border(1.dp, Color(0xFFEBE9FE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚔️", fontSize = 36.sp)
                }
            }
        }

        // BENTO GRID ROW 2: Two Columns (Math Quiz + Tournaments)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Math Quiz Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .shadow(3.dp, RoundedCornerShape(20.dp), ambientColor = Color(0xFF7C3AED).copy(alpha = 0.15f))
                    .clickable { viewModel.currentTab = "SingleMath" },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFECE7F4))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Top-left orange emblem
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFFFEDD5), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🧮", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "MATH QUIZ",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            text = "Earn up to 200 🪙 daily via fast solver equations.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PLAY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7C3AED)
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFF1F5F9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "→",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Tournaments Board Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .shadow(3.dp, RoundedCornerShape(20.dp), ambientColor = Color(0xFF7C3AED).copy(alpha = 0.15f))
                    .clickable { viewModel.currentTab = "Tournaments" },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFECE7F4))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Top-left gold emblem
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🏆", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "CONTESTS",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            text = "Compete inside massive scheduled live events.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "JOIN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7C3AED)
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFF1F5F9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "→",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // BENTO GRID ROW 3: Two Columns (Leaderboard + UPI Wallet)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Leaderboard Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .shadow(3.dp, RoundedCornerShape(20.dp), ambientColor = Color(0xFF7C3AED).copy(alpha = 0.15f))
                    .clickable { viewModel.currentTab = "Leaderboard" },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFECE7F4))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Top-left blue emblem
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFDBEAFE), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🏅", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "LEADERBOARD",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            text = "Analyze top player coin ranks and winners.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7C3AED)
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFF1F5F9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "→",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Wallet Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .shadow(3.dp, RoundedCornerShape(20.dp), ambientColor = Color(0xFF7C3AED).copy(alpha = 0.15f))
                    .clickable { viewModel.currentTab = "Wallet" },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFECE7F4))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Top-left purple emblem
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFFDF4FF), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "💳", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "UPI WALLET",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            text = "Convert coin rewards to real cash bank UPI.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WALLET",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7C3AED)
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFF1F5F9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "→",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BannerSliderLayout() {
    val banners = listOf(
        BannerData("MEGA TOURNAMENT IS LIVE", "Prize Pool: 10,000 Coins • Join Live Contest Battle Today!", "🏆"),
        BannerData("1 VS 1 BATTLE MATCHES", "Compete with real opponents dynamically to double rewards.", "⚔️"),
        BannerData("INSTANT UPI WALLET", "Fast withdrawal checks enabled. Exchange coins to UPI easily.", "💰")
    )

    var currentIndex by remember { mutableStateOf(0) }

    // Swap banners continuously
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentIndex = (currentIndex + 1) % banners.size
        }
    }

    val banner = banners[currentIndex]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color(0xFF7C3AED).copy(alpha = 0.3f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFA855F7))))
                .padding(16.dp)
        ) {
            // Right-side big soft glowing decorative circle
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp)
            )

            // Right-side badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Text(text = banner.emoji, fontSize = 24.sp)
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.75f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "HOT EVENT",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = banner.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                }

                // CTA button or subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        modifier = Modifier.shadow(elevation = 3.dp, shape = RoundedCornerShape(10.dp), ambientColor = Color(0xFFC084FC))
                    ) {
                        Text(
                            text = "PLAY NOW",
                            color = Color(0xFF7C3AED),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = banner.subtitle,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Page dots at the bottom-right
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 6.dp, end = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                banners.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (idx == currentIndex) Color.White else Color.White.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

data class BannerData(val title: String, val subtitle: String, val emoji: String)

@Composable
fun DailyCheckinCardLayout(user: UserProfile, viewModel: QuizViewModel) {
    val now = System.currentTimeMillis()
    val isClaimedToday = (now - user.lastCheckInTimestamp < 24 * 60 * 60 * 1000L) && user.lastCheckInTimestamp > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF3E8FF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📅 DAILY CHECK-IN REWARD",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = PurpleDark
                    )
                    Text(
                        text = if (isClaimedToday) "Come back tomorrow for next streak!" else "Claim free daily coin multipliers",
                        fontSize = 11.sp,
                        color = if (isClaimedToday) MintGreen else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Debug helper
                IconButton(
                    onClick = { viewModel.mockCheckInOverrule(user) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Bypass clock limits (Dev mode help!)",
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stretch 7 days streak selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dailyAwards = listOf(5, 10, 15, 20, 25, 30, 50)
                for (dayNum in 1..7) {
                    val isClaimed = dayNum < user.checkInStreak || (dayNum == user.checkInStreak && isClaimedToday)
                    val isCurrent = dayNum == user.checkInStreak && !isClaimedToday

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .background(
                                color = when {
                                    isClaimed -> Color(0xFFECFDF5)
                                    isCurrent -> Color(0xFFF3E8FF)
                                    else -> Color(0xFFF9FAFB)
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = when {
                                    isClaimed -> MintGreen.copy(alpha = 0.5f)
                                    isCurrent -> PurplePrimary
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "D$dayNum",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClaimed) MintGreen else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = if (isClaimed) Icons.Default.CheckCircle else Icons.Default.MonetizationOn,
                            contentDescription = "Coin",
                            tint = if (isClaimed) MintGreen else if (isCurrent) GoldAccent else Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+${dailyAwards[dayNum-1]}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = PurpleDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3D Claim button
            if (isClaimedToday) {
                Button(
                    onClick = { },
                    enabled = false,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFFE5E7EB),
                        disabledContentColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("CLAIMED TODAY 🎉 (Streak Day ${user.checkInStreak})")
                }
            } else {
                Premium3DButton(
                    text = "CLAIM DAY ${user.checkInStreak} REWARD",
                    onClick = { viewModel.claimDailyReward(user) },
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("claim_daily_btn")
                )
            }
        }
    }
}

// ==========================================
// 4. MULTIPLAYER 1 VS 1 BATTLE
// ==========================================

@Composable
fun MatchmakingSelectionScreen(user: UserProfile, viewModel: QuizViewModel) {
    var entryFeeSelected by remember { mutableIntStateOf(20) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFF3E8FF))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Bolt, "Arena", tint = PurplePrimary, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "1v1 BATTLE MATCHMAKER",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = PurpleDark
                )
                Text(
                    text = "Instantly pay an Entry Fee, find an opponent, and solve identical 10 math challenges. Top score or fastest timer wins the prize!",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // ENTRY FEES CHOICES LIST
        Text(
            text = "Select Entry Coins Amount",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = PurpleDark,
            modifier = Modifier.align(Alignment.Start)
        )

        val entries = listOf(
            EntryOption(20, 40),
            EntryOption(50, 100),
            EntryOption(100, 200),
            EntryOption(500, 1000)
        )

        entries.forEach { opt ->
            val isSelected = entryFeeSelected == opt.fee
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { entryFeeSelected = opt.fee },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFFFAF5FF) else Color.White
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PurplePrimary else Color(0xFFE8E4F2)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { entryFeeSelected = opt.fee },
                            colors = RadioButtonDefaults.colors(selectedColor = PurplePrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Entry: ${opt.fee} Gold Coins",
                                fontWeight = FontWeight.Bold,
                                color = PurpleDark
                            )
                            Text(
                                text = "Winning Reward Pool: ${opt.winPrize} Coins",
                                fontSize = 11.sp,
                                color = PurpleSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(PurpleLight, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "2X Reward",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = PurplePrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Your Balance: ${user.coins} Coins",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (user.coins >= entryFeeSelected) MintGreen else CoralRose
        )

        Premium3DButton(
            text = "MATCH ME & START GAME",
            onClick = { viewModel.start1v1Matchmaking(user, entryFeeSelected) },
            enabled = user.coins >= entryFeeSelected,
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("start_match_btn")
        )
    }
}

data class EntryOption(val fee: Int, val winPrize: Int)

@Composable
fun BattleArenaScreen(user: UserProfile, viewModel: QuizViewModel) {
    when (viewModel.battleState) {
        "Searching" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(PurpleDark, Color(0xFF1E1B29)))),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Simulated pulsing radar scanning circle
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseRadius by infiniteTransition.animateFloat(
                        initialValue = 40f,
                        targetValue = 120f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "radius"
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "alpha"
                    )

                    Box(
                        modifier = Modifier.size(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = PurpleAccent.copy(alpha = pulseAlpha),
                                radius = pulseRadius * density
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(PurpleSecondary, PurplePrimary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Bolt, "Lightning", tint = Color.White, modifier = Modifier.size(42.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "FINDING CHALLENGER",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.battleStatusMessage,
                        fontSize = 13.sp,
                        color = PurpleLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        "Matched" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(PurplePrimary, PurpleDark))),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "MATCH FOUND!",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(35.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Card(
                            shape = CircleShape,
                            border = BorderStroke(3.dp, Color.White),
                            modifier = Modifier.size(90.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(PurpleAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.name.take(1).uppercase(), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // VERSUS BADGE
                        Text(
                            text = "VS",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        // Opponent Avatar
                        Card(
                            shape = CircleShape,
                            border = BorderStroke(3.dp, CoralRose),
                            modifier = Modifier.size(90.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (viewModel.battleOpponent?.name ?: "B").take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        text = "Challenger Name: ${viewModel.battleOpponent?.name ?: "Ninja"} (UID: ${viewModel.battleOpponent?.id})",
                        fontSize = 15.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Challenger Coins: ${viewModel.battleOpponent?.coins ?: 100}",
                        fontSize = 12.sp,
                        color = PurpleLight
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading 10 quiz equations...", color = PurpleLight, fontSize = 11.sp)
                }
            }
        }
        "Playing" -> {
            val currentQ = viewModel.battleQuestions.getOrNull(viewModel.battleQuestionIndex) ?: return
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Battle header with relative progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("YOU: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${viewModel.battleUserScore} PTS", fontSize = 12.sp, color = PurplePrimary, fontWeight = FontWeight.Black)
                        }
                    }

                    // Question Count Index Badge
                    Surface(
                        color = PurplePrimary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Q ${viewModel.battleQuestionIndex + 1} / 10",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Card {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("OPPONENT: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${viewModel.battleOpponentScore} PTS", fontSize = 12.sp, color = CoralRose, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Real-time progressive visualizers for dynamic competitive tension!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Your solved count", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${viewModel.battleUserSolvedCount}/10 tasks", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { viewModel.battleUserSolvedCount / 10f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = MintGreen
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${viewModel.battleOpponent?.name ?: "Opponent"} progress", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("${viewModel.battleOpponentSolvedCount}/10 tasks", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        LinearProgressIndicator(
                            progress = { viewModel.battleOpponentSolvedCount / 10f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = CoralRose
                        )
                    }
                }

                // Quiz Equation display board
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.5f)
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE9E5F2))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Solve fast to claim time bonus!",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentQ.expression,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = PurplePrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "= ?",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.LightGray
                            )
                        }
                    }
                }

                // 4 Choices buttons
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentQ.options.forEachIndexed { opIdx, opValue ->
                        val isSelected = viewModel.battleSelectedAnswer == opIdx
                        val isCorrect = opIdx == currentQ.correctChoiceIndex
                        val isAnswered = viewModel.isBattleQuestionAnswered

                        val bgColor = when {
                            isAnswered && isCorrect -> Color(0xFFD1FAE5) // light success green
                            isSelected -> Color(0xFFF3E8FF)
                            else -> Color.White
                        }

                        val borderOutlineColor = when {
                            isAnswered && isCorrect -> MintGreen
                            isSelected -> PurplePrimary
                            else -> Color(0xFFEFECF4)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable(enabled = !isAnswered) {
                                    viewModel.submitBattleAnswer(user, opIdx)
                                },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(if (isSelected) 2.5.dp else 1.dp, borderOutlineColor),
                            colors = CardDefaults.cardColors(containerColor = bgColor)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) PurplePrimary else Color(0xFFF3F4F6)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (opIdx) {
                                                0 -> "A"
                                                1 -> "B"
                                                2 -> "C"
                                                else -> "D"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.White else Color.DarkGray
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = opValue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = PurpleDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        "ScoreReview" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAF9FF))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFF3E8FF))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = if (viewModel.battleUserScore > viewModel.battleOpponentScore || (viewModel.battleUserScore == viewModel.battleOpponentScore && viewModel.battleTimeElapsedUser < viewModel.battleTimeElapsedOpponent)) Icons.Default.EmojiEvents else Icons.Default.Cancel,
                            contentDescription = "Result",
                            tint = if (viewModel.battleUserScore >= viewModel.battleOpponentScore) GoldAccent else CoralRose,
                            modifier = Modifier.size(72.dp)
                        )

                        Text(
                            text = viewModel.battleStatusMessage,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = PurpleDark,
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(color = Color(0xFFF3E8FF))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("YOUR SCORE", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("${viewModel.battleUserScore} Pts", fontSize = 22.sp, fontWeight = FontWeight.Black, color = PurplePrimary)
                                Text("Time: ${viewModel.battleTimeElapsedUser}s", fontSize = 11.sp, color = Color.Gray)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("OPPONENT", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("${viewModel.battleOpponentScore} Pts", fontSize = 22.sp, fontWeight = FontWeight.Black, color = CoralRose)
                                Text("Time: ${viewModel.battleTimeElapsedOpponent}s", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF3E8FF))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Match Entry Fee Refund Pool", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (viewModel.battleStatusMessage.contains("Won") || viewModel.battleStatusMessage.contains("Win")) {
                                        "+${viewModel.battleEntryFee * 2} Coins"
                                    } else {
                                        "-${viewModel.battleEntryFee} Coins"
                                    },
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = if (viewModel.battleStatusMessage.contains("Won") || viewModel.battleStatusMessage.contains("Win")) MintGreen else CoralRose
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Premium3DButton(
                            text = "FINISH MATCH",
                            onClick = { viewModel.finishBattleSession() },
                            modifier = Modifier.fillMaxWidth().testTag("finish_battle_btn")
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. SINGLE MATH QUIZ MAIN ARENA
// ==========================================

@Composable
fun MathQuizScreen(user: UserProfile, viewModel: QuizViewModel) {

    // Trigger first generation if not loaded
    LaunchedEffect(Unit) {
        if (viewModel.mathQuestionText.isEmpty()) {
            viewModel.generateMathQuestion(user)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily coins limit cap indicator progress card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFF3E8FF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Timer, "Limit cap", tint = PurplePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DAILY MATH COINS CAP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PurpleDark
                        )
                    }

                    Text(
                        text = "${user.dailyMathCoinsEarned} / 200 Coins",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = PurplePrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { user.dailyMathCoinsEarned / 200f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = PurplePrimary,
                    trackColor = Color(0xFFF3E8FF)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Correct answer: +2 coins | incorrect solve: -1 coin.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Equation display glass card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f)
                .shadow(4.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.2.dp, Color(0xFFEBE6F3))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SOLVE THIS EQUATION",
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = viewModel.mathQuestionText,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        color = PurplePrimary
                    )
                }
            }
        }

        // feedback status message
        Box(
            modifier = Modifier.fillMaxWidth().height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.mathScoreMessage.isNotEmpty()) {
                Text(
                    text = viewModel.mathScoreMessage,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = if (viewModel.mathScoreMessage.contains("Correct")) MintGreen else CoralRose
                )
            }
        }

        // 4 Option Choices
        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.mathOptions.forEachIndexed { index, option ->
                val isAnswered = viewModel.isMathQuestionAnswered
                val isSelected = viewModel.mathSelectedAnswerIndex == index
                val isCorrect = index == viewModel.mathCorrectAnswerIndex

                val outlineBorderColor = when {
                    isAnswered && isCorrect -> MintGreen
                    isSelected -> PurplePrimary
                    else -> Color(0xFFECE7F4)
                }

                val itemBg = when {
                    isAnswered && isCorrect -> Color(0xFFD1FAE5) // Success soft mint green
                    isSelected -> Color(0xFFF3E8FF)
                    else -> Color.White
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable(enabled = !isAnswered) {
                            viewModel.submitMathAnswer(user, index)
                        },
                    colors = CardDefaults.cardColors(containerColor = itemBg),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(if (isSelected) 2.5.dp else 1.dp, outlineBorderColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = option,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = PurpleDark
                        )
                    }
                }
            }
        }

        // NEXT ACTION BAR
        AnimatedVisibility(
            visible = viewModel.isMathQuestionAnswered,
            enter = fadeIn() + expandVertically()
        ) {
            Premium3DButton(
                text = "NEXT EQUATION ➡️",
                onClick = { viewModel.generateMathQuestion(user) },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("next_math_btn")
            )
        }
    }
}

// ==========================================
// 6. TOURNAMENTS WORKBOARD
// ==========================================

@Composable
fun TournamentsBoardScreen(user: UserProfile, viewModel: QuizViewModel) {
    val tournaments by viewModel.allTournaments.collectAsStateWithLifecycle(initialValue = emptyList())
    var tournamentCategoryTab by remember { mutableStateOf("LIVE") } // "LIVE", "UPCOMING", "ENDED"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFF0ECF6))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.EmojiEvents, "Cup", tint = GoldAccent, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("TOURNAMENTS HUB", fontWeight = FontWeight.Black, fontSize = 14.sp, color = PurpleDark)
                    Text("Join 50-100 player schedulers to win jackpot prize pools!", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        // Segmented Tabs Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFE9FE), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val tabs = listOf("LIVE", "UPCOMING", "ENDED")
            tabs.forEach { t ->
                val selected = tournamentCategoryTab == t
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { tournamentCategoryTab = t }
                        .background(
                            if (selected) PurplePrimary else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (selected) Color.White else Color.Gray
                    )
                }
            }
        }

        val filteredTournaments = tournaments.filter {
            it.status.uppercase() == tournamentCategoryTab
        }

        if (filteredTournaments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Dns, "Null", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Text("No $tournamentCategoryTab events are active.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTournaments) { t ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .clickable { viewModel.selectTournamentItem(t) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEBE6F3))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = t.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PurpleDark
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (t.status.uppercase()) {
                                        "LIVE" -> Color(0xFFECFDF5)
                                        "ENDED" -> Color(0xFFF3F4F6)
                                        else -> Color(0xFFEFF6FF)
                                    }
                                ) {
                                    Text(
                                        text = t.status,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (t.status.uppercase()) {
                                            "LIVE" -> MintGreen
                                            "ENDED" -> Color.Gray
                                            else -> PurpleSecondary
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Prize Pool Pool", fontSize = 10.sp, color = Color.Gray)
                                    Text("${t.prizePool} Coins", fontSize = 15.sp, fontWeight = FontWeight.Black, color = PurplePrimary)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Registered Players", fontSize = 10.sp, color = Color.Gray)
                                    Text("${t.joinedPlayers} / ${t.maxPlayers}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Schedule, "Time", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(t.startTime, fontSize = 11.sp, color = Color.Gray)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Entry: ", fontSize = 11.sp, color = Color.Gray)
                                    Text("${t.entryFee} Coins", fontSize = 12.sp, fontWeight = FontWeight.Black, color = PurpleSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DETAILED REGISTER/PLAY OVERLAY SHEET DIALOG ---
    viewModel.selectedTournament?.let { tournament ->
        AlertDialog(
            onDismissRequest = { viewModel.selectedTournament = null },
            confirmButton = {
                if (tournament.status.uppercase() == "LIVE") {
                    Premium3DButton(
                        text = "REGISTER & PLAY",
                        onClick = { viewModel.joinTournament(user, tournament) },
                        modifier = Modifier.testTag("reg_play_tournament_btn")
                    )
                } else {
                    Button(onClick = { viewModel.selectedTournament = null }) {
                        Text("CLOSE")
                    }
                }
            },
            dismissButton = {
                if (tournament.status.uppercase() == "LIVE") {
                    TextButton(onClick = { viewModel.selectedTournament = null }) {
                        Text("CANCEL")
                    }
                }
            },
            title = {
                Text(tournament.title)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Contest Rules:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("• This tournament features 5 math tasks.\n• Top scorers are ranked by answers correctness.\n• Prize Pool distributes payouts to TOP 5 ranked winners instantly.")
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tournament Entry Cost: ", fontSize = 12.sp)
                        Text("${tournament.entryFee} Gold Coins", fontWeight = FontWeight.Black, color = PurpleSecondary)
                    }

                    if (tournament.status.uppercase() == "ENDED") {
                        Text("Historical Winners List:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("1. Aditi Sen (+400 coins)\n2. QuizMaster (+200 coins)\n3. BetaSlayer (+150 coins)")
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun TournamentArenaScreen(user: UserProfile, viewModel: QuizViewModel) {
    when (viewModel.tournamentPlayState) {
        "JoinedAlert" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PurplePrimary),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.EmojiEvents, "Golden Cup", tint = GoldAccent, modifier = Modifier.size(72.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "JOINED SUCCESS!",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Synchronizing live tournament gameplay clock...",
                        color = PurpleLight,
                        fontSize = 12.sp
                    )
                }
            }
        }
        "Playing" -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tournament Quiz Run", fontWeight = FontWeight.Black, color = PurpleDark)
                    Surface(color = PurpleLight, shape = RoundedCornerShape(10.dp)) {
                        Text(
                            "Task ${viewModel.tournamentQuestionIndex + 1} / 5",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary,
                            fontSize = 11.sp
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Fast calculations!", color = Color.LightGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = when (viewModel.tournamentQuestionIndex) {
                                    0 -> "35 + 47"
                                    1 -> "18 × 3"
                                    2 -> "99 - 41"
                                    3 -> "150 - 62"
                                    else -> "12 × 12"
                                },
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = PurplePrimary
                            )
                        }
                    }
                }

                // options
                val optionsList = when (viewModel.tournamentQuestionIndex) {
                    0 -> listOf("72", "82", "92", "85")
                    1 -> listOf("48", "54", "64", "45")
                    2 -> listOf("48", "58", "68", "52")
                    3 -> listOf("78", "88", "98", "85")
                    else -> listOf("124", "144", "134", "154")
                }

                optionsList.forEachIndexed { opIdx, opVal ->
                    val isAnswered = viewModel.isTournamentAnswered
                    val isSelected = viewModel.tournamentSelectedAnswer == opIdx

                    val checkBg = when {
                        isAnswered && opIdx == 1 -> Color(0xFFD1FAE5) // preset 1 is correct index for trial
                        isSelected -> Color(0xFFF3E8FF)
                        else -> Color.White
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clickable(enabled = !isAnswered) {
                                viewModel.submitTournamentAnswer(user, opIdx)
                            },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isSelected) PurplePrimary else Color(0xFFECE7F4)),
                        colors = CardDefaults.cardColors(containerColor = checkBg)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                            Text(opVal, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
        "Completed" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(Icons.Filled.Stars, "Finish", tint = GoldAccent, modifier = Modifier.size(64.dp))
                        Text(
                            text = "CONTEST FINISHED!",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = PurpleDark
                        )
                        Text(
                            text = "You scored ${viewModel.tournamentUserScore} points on this quiz challenge.\nResults compiled. Check results alerts inside notification bell.",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Premium3DButton(
                            text = "EXIT LEADERBOARD",
                            onClick = { viewModel.exitTournamentBoard() },
                            modifier = Modifier.fillMaxWidth().testTag("exit_tournament_btn")
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. UPI WALLET & WITHDRAW SYSTEMS
// ==========================================

@Composable
fun WalletCenterScreen(user: UserProfile, viewModel: QuizViewModel) {
    val transactionHistory by viewModel.allTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val withdrawRequests by viewModel.allWithdrawRequests.collectAsStateWithLifecycle(initialValue = emptyList())
    var walletSubCategoryTab by remember { mutableStateOf("WITHDRAW") } // "WITHDRAW", "HISTORY"

    val localFocusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Outstanding Coin Gold balance panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, Color(0xFFF3E8FF))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOTAL WALLET BALANCE",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MonetizationOn, "Gold", tint = GoldAccent, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${user.coins}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = PurplePrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                val convertedRs = (user.coins * 5) / 100f
                Text(
                    text = "Cash Equivalence Rating: ₹$convertedRs",
                    fontSize = 14.sp,
                    color = PurpleSecondary,
                    fontWeight = FontWeight.Bold
                )

                // conversion rates details card
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Standard conversion economy code:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text("100 Coins = ₹5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PurpleDark)
                            Text("1000 Coins = ₹50", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PurpleDark)
                            Text("2000 Coins = ₹100", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PurpleDark)
                        }
                    }
                }
            }
        }

        // Sub Navigation categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFE9FE), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val categorizers = listOf("WITHDRAW", "HISTORY")
            categorizers.forEach { cat ->
                val selected = walletSubCategoryTab == cat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { walletSubCategoryTab = cat }
                        .background(
                            if (selected) PurplePrimary else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (selected) Color.White else Color.Gray
                    )
                }
            }
        }

        if (walletSubCategoryTab == "WITHDRAW") {
            // WITHDRAW FORM PANEL
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFF3E8FF))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Submit UPI Withdrawal Cash",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = PurpleDark
                    )

                    // Error & Info Feedback messages
                    viewModel.walletErrorMessage?.let { err ->
                        Text(text = "⚠️ $err", color = CoralRose, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    viewModel.walletMessage?.let { msg ->
                        Text(text = "✓ $msg", color = MintGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Select preset amount radio groups
                    Text("Select Withdraw Target Amount:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val amountsPreset = listOf(
                            PresetAmount(50, 1000),
                            PresetAmount(100, 2000),
                            PresetAmount(200, 4000)
                        )

                        amountsPreset.forEach { preset ->
                            val sel = viewModel.withdrawAmountRs == preset.rs
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.withdrawAmountRs = preset.rs }
                                    .background(
                                        if (sel) Color(0xFFF3E8FF) else Color(0xFFF9FAFB),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (sel) PurplePrimary else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(vertical = 8.dp)
                            ) {
                                Text("₹${preset.rs}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = PurplePrimary)
                                Text("${preset.coins} Coins", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    // UPI input box
                    OutlinedTextField(
                        value = viewModel.withdrawUpiId,
                        onValueChange = { viewModel.withdrawUpiId = it },
                        label = { Text("Enter UPI ID (e.g. mobile@ybl / upi)") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, "UPI") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upi_input_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Premium3DButton(
                        text = "SUBMIT UPI WITHDRAWAL",
                        onClick = {
                            localFocusManager.clearFocus()
                            viewModel.submitUPIWithdraw(user)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_submit_btn")
                    )

                    Text(
                        "• Minimum withdraw balance: ₹50.\n• Limit: One request allowed every 24 hours.\n• Approval requires manual authorization or Admin Panel review codes.",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }
            }

            // LIST USER RECENT WITHDRAW PETITIONS STATUSES
            Text("Outstanding UPI Withdraw Requests", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PurpleDark)

            val currentRequests = withdrawRequests.filter { it.userId == user.id }
            if (currentRequests.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No withdrawal records submitted.", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(currentRequests) { wr ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Withdrawal: ₹${wr.amountInRs}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("UPI ID: ${wr.upiId}", fontSize = 11.sp, color = Color.Gray)
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (wr.status) {
                                        "Approved" -> Color(0xFFD1FAE5)
                                        "Rejected" -> Color(0xFFFEE2E2)
                                        else -> Color(0xFFFEF3C7) // Pending
                                    }
                                ) {
                                    Text(
                                        text = wr.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (wr.status) {
                                            "Approved" -> MintGreen
                                            "Rejected" -> CoralRose
                                            else -> Color(0xFFD97706)
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // COIN TRANSACTION HISTORY
            Text("Coin history details:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PurpleDark)

            val uTxs = transactionHistory.filter { it.userId == user.id }
            if (uTxs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No coin transactions listed yet.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uTxs) { tx ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = java.text.SimpleDateFormat("dd MMM hh:mm a")
                                            .format(java.util.Date(tx.timestamp)),
                                        fontSize = 11.sp, color = Color.Gray
                                    )
                                }

                                Text(
                                    text = if (tx.coinDiff >= 0) "+${tx.coinDiff}" else "${tx.coinDiff}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (tx.coinDiff >= 0) MintGreen else CoralRose
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PresetAmount(val rs: Int, val coins: Int)

// ==========================================
// 8. LEADERBOARD SCREEN
// ==========================================

@Composable
fun LeaderboardsScreen(viewModel: QuizViewModel) {
    val allPlayers by viewModel.allPlayers.collectAsStateWithLifecycle(initialValue = emptyList())
    var timeFilter by remember { mutableStateOf("DAILY") } // "DAILY", "WEEKLY", "MONTHLY"

    // Sort players locally based on score variations to simulate leaderboards
    val sortedPlayers = allPlayers.sortedByDescending { u ->
        when (timeFilter) {
            "DAILY" -> u.coins + (u.totalCorrectAnswers * 5)
            "WEEKLY" -> u.coins * 3 + (u.totalCorrectAnswers * 15)
            else -> u.coins * 5 + (u.totalCorrectAnswers * 25)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Leaderboard, "Trophy", tint = GoldAccent, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("POCKET CHAMPIONS LIST", fontWeight = FontWeight.Black, fontSize = 14.sp, color = PurpleDark)
                    Text("Top 10 players ranked by answers score totals", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        // Leaderboard filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFE9FE), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val terms = listOf("DAILY", "WEEKLY", "MONTHLY")
            terms.forEach { term ->
                val selected = timeFilter == term
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { timeFilter = term }
                        .background(
                            if (selected) PurplePrimary else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = term,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else Color.Gray
                    )
                }
            }
        }

        // LEADERBOARD LIST
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(sortedPlayers.take(10).run { if (isEmpty()) allPlayers else this }, key = { it.id }) { u ->
                val rank = sortedPlayers.indexOfFirst { it.id == u.id } + 1
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (u.isCurrentUser) Color(0xFFF3E8FF) else Color.White
                    ),
                    border = BorderStroke(1.dp, if (u.isCurrentUser) PurplePrimary else Color(0xFFECE7F4))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Rank number / indicator
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = when (rank) {
                                            1 -> GoldAccent
                                            2 -> Color(0xFFD1D5DB)
                                            3 -> Color(0xFFCE7E00)
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (rank <= 3) {
                                    Icon(
                                        imageVector = Icons.Filled.EmojiEvents,
                                        contentDescription = "Pos",
                                        tint = if (rank == 1) Color.White else Color.DarkGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Text(
                                        text = "$rank",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = u.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PurpleDark
                                    )
                                    if (u.isCurrentUser) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            color = PurpleSecondary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "YOU",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text("UID: ${u.id}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        // Score metrics tag
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, "Rating", tint = PurpleSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${u.coins} Coins",
                                fontWeight = FontWeight.Black,
                                color = PurplePrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. PROFILE & METRICS VIEW SCREEN
// ==========================================

@Composable
fun ProfileInformationScreen(user: UserProfile, viewModel: QuizViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Detailed Profile overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFEBE6F3))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PurpleDark)
                Text("Registered Mobile: +91 ${user.mobile}", fontSize = 12.sp, color = Color.Gray)
                Text("Unique ID: ${user.id}", fontSize = 11.sp, color = Color.LightGray)

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                // User stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Correct Answers", fontSize = 10.sp, color = Color.Gray)
                        Text("${user.totalCorrectAnswers}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PurplePrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Check-In Streak", fontSize = 10.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Bolt, "Fire", tint = CoralRose, modifier = Modifier.size(16.dp))
                            Text("${user.checkInStreak} Days", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PurplePrimary)
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Contests Won", fontSize = 10.sp, color = Color.Gray)
                        Text("${user.totalContestsWon}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PurplePrimary)
                    }
                }
            }
        }

        // Section link to unlock Admin Mode directly! Highly convenient
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF8FF)),
            border = BorderStroke(1.dp, PurpleSecondary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.currentTab = "Admin" }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SettingsSystemDaydream, "Admin Panel", tint = PurplePrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("🛡️ Administrator Panel Link", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Adjust coins, ban profiles, verify UPI approvals", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Open", tint = PurplePrimary)
            }
        }

        // Achievements Section
        Text(
            text = "🏅 RECENT ACHIEVEMENTS",
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = PurplePrimary,
            letterSpacing = 1.sp
        )

        val achievementsList = listOf(
            ProfileAchievement("First Quiz Completed", "Submit answers on a math question task.", user.totalQuizzesCompleted >= 1),
            ProfileAchievement("Correct Answers Master", "Get 15 correct solutions totals. (Current: ${user.totalCorrectAnswers}/15)", user.totalCorrectAnswers >= 15),
            ProfileAchievement("Check-in 3 Day Streak", "Claim checked-in coins 3 days continuously.", user.checkInStreak >= 3),
            ProfileAchievement("Earn 1000 Coins", "Acquire pocket coins total inside wallet.", user.coins >= 1000),
            ProfileAchievement("Tournament Victor Badge", "Win at least 1 contest championship match.", user.totalContestsWon >= 1)
        )

        achievementsList.forEach { ach ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (ach.isUnlocked) Color(0xFFF0FDF4) else Color.White
                ),
                border = BorderStroke(1.dp, if (ach.isUnlocked) MintGreen.copy(alpha = 0.4f) else Color(0xFFEFE9FE))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (ach.isUnlocked) Icons.Filled.Stars else Icons.Outlined.Lock,
                            contentDescription = "Ach",
                            tint = if (ach.isUnlocked) MintGreen else Color.LightGray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = ach.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (ach.isUnlocked) Color(0xFF166534) else PurpleDark
                            )
                            Text(ach.description, fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    if (ach.isUnlocked) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "UNLOCKED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color(0xFF15803D)
                            )
                        }
                    } else {
                        Text("LOCKED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Log out button
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = CoralRose),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Filled.Logout, "Log out")
            Spacer(modifier = Modifier.width(8.dp))
            Text("LOG OUT SIMULATOR")
        }
    }
}

data class ProfileAchievement(val title: String, val description: String, val isUnlocked: Boolean)

// ==========================================
// 10. FULL UNIFIED ADMIN PANEL
// ==========================================

@Composable
fun AdminControlDashboardScreen(viewModel: QuizViewModel) {
    val usersList by viewModel.allPlayers.collectAsStateWithLifecycle(initialValue = emptyList())
    val tournamentsList by viewModel.allTournaments.collectAsStateWithLifecycle(initialValue = emptyList())
    val withdrawRequestsList by viewModel.allWithdrawRequests.collectAsStateWithLifecycle(initialValue = emptyList())

    var adminSectionTab by remember { mutableStateOf("WITHDRAWS") } // "USERS", "WITHDRAWS", "QUESTION", "NOTIFICATIONS"

    var searchQuery by remember { mutableStateOf("") }
    
    // Notifications parameters
    var notifTitle by remember { mutableStateOf("") }
    var notifMessage by remember { mutableStateOf("") }
    
    // Custom Trivia parameters
    var triviaQText by remember { mutableStateOf("") }
    var triviaA by remember { mutableStateOf("") }
    var triviaB by remember { mutableStateOf("") }
    var triviaC by remember { mutableStateOf("") }
    var triviaD by remember { mutableStateOf("") }
    var triviaCorrectIndex by remember { mutableIntStateOf(0) }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Warning Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E6)),
            border = BorderStroke(1.dp, CoralRose),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Gavel, "Sec", tint = CoralRose)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("🛡️ POCKET QUIZ ADMIN SYSTEMS", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color(0xFF9F1239))
                    Text("Develope-mode master override tools is loaded.", fontSize = 11.sp, color = Color(0xFF92400E))
                }
            }
        }

        // Horizontal selectors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(Color(0xFFEFE9FE), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val sections = listOf(
                AdminTab("WITHDRAWS", "UPI Requests"),
                AdminTab("USERS", "Player Profiles"),
                AdminTab("QUESTION", "Create Trivia"),
                AdminTab("NOTIFICATIONS", "System Alerts")
            )
            sections.forEach { s ->
                val active = adminSectionTab == s.id
                Box(
                    modifier = Modifier
                        .clickable { adminSectionTab = s.id }
                        .background(
                            if (active) PurplePrimary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = s.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (active) Color.White else Color.Gray
                    )
                }
            }
        }

        // SECTION WORKSPACE
        when (adminSectionTab) {
            "WITHDRAWS" -> {
                Text("Verify & Authorize UPI Payouts", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PurpleDark)
                
                val reqsPending = withdrawRequestsList.filter { it.status == "Pending" }
                if (reqsPending.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No pending UPI cash requests found! 👍", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        items(reqsPending) { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFECE7F3), RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Awaiting: ₹${req.amountInRs} Cash", fontWeight = FontWeight.Black, fontSize = 15.sp)
                                            Text("UPI: ${req.upiId}", fontSize = 11.sp, color = PurplePrimary, fontWeight = FontWeight.Bold)
                                            Text("User ID: ${req.userId}", fontSize = 10.sp, color = Color.Gray)
                                            Text("Coin deducted fee: ${req.coinCost} Coins", fontSize = 10.sp, color = Color.Gray)
                                        }

                                        // Status pending tag
                                        Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(8.dp)) {
                                            Text("PENDING", color = Color(0xFFD97706), fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.approveWithdrawRequest(req) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(36.dp)
                                        ) {
                                            Text("APPROVE PAY", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }

                                        Button(
                                            onClick = { viewModel.rejectWithdrawRequest(req) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CoralRose),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(36.dp)
                                        ) {
                                            Text("REJECT REVERSE", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "USERS" -> {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search player mobile or names...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Find") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                val filtered = usersList.filter {
                    it.name.contains(searchQuery, true) || it.mobile.contains(searchQuery) || it.id.contains(searchQuery, true)
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(filtered) { u ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${u.name} ${if (u.isCurrentUser) "⭐️" else ""}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("UID: ${u.id} | Mobile: +91 ${u.mobile}", fontSize = 11.sp, color = Color.Gray)
                                        Text("Balance: ${u.coins} Coins", fontSize = 12.sp, fontWeight = FontWeight.Black, color = PurplePrimary)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (u.isBanned) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = if (u.isBanned) "BANNED" else "ACTIVE",
                                            color = if (u.isBanned) CoralRose else MintGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.addCoinsAdmin(u.id, 100) },
                                        contentPadding = PaddingValues(horizontal = 6.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PurpleSecondary)
                                    ) {
                                        Text("+100 Coins", fontSize = 9.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.removeCoinsAdmin(u.id, 100) },
                                        contentPadding = PaddingValues(horizontal = 6.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                                    ) {
                                        Text("-100 Coins", fontSize = 9.sp, color = Color.DarkGray)
                                    }

                                    Button(
                                        onClick = {
                                            if (u.isBanned) viewModel.unbanPlayer(u.id) else viewModel.banPlayer(u.id)
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = if (u.isBanned) MintGreen else CoralRose)
                                    ) {
                                        Text(if (u.isBanned) "UNBAN" else "BAN LOCK", fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "QUESTION" -> {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Add Custom Quiz Trivia Question", fontWeight = FontWeight.Bold, color = PurpleDark)
                    
                    OutlinedTextField(
                        value = triviaQText,
                        onValueChange = { triviaQText = it },
                        label = { Text("Question text") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = triviaA,
                        onValueChange = { triviaA = it },
                        label = { Text("Option A") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = triviaB,
                        onValueChange = { triviaB = it },
                        label = { Text("Option B") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = triviaC,
                        onValueChange = { triviaC = it },
                        label = { Text("Option C") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = triviaD,
                        onValueChange = { triviaD = it },
                        label = { Text("Option D") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Correct Index option (0 to 3):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (i in 0..3) {
                            val active = triviaCorrectIndex == i
                            Button(
                                onClick = { triviaCorrectIndex = i },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (active) PurplePrimary else Color.LightGray
                                )
                            ) {
                                Text(
                                    text = when (i) {
                                        0 -> "A"
                                        1 -> "B"
                                        2 -> "C"
                                        else -> "D"
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (triviaQText.isNotEmpty() && triviaA.isNotEmpty()) {
                                viewModel.createCustomTrivia(triviaQText, triviaA, triviaB, triviaC, triviaD, triviaCorrectIndex)
                                triviaQText = ""
                                triviaA = ""
                                triviaB = ""
                                triviaC = ""
                                triviaD = ""
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("SAVE QUESTION")
                    }
                }
            }
            "NOTIFICATIONS" -> {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Compose Global Alert Push Notification", fontWeight = FontWeight.Bold, color = PurpleDark)

                    OutlinedTextField(
                        value = notifTitle,
                        onValueChange = { notifTitle = it },
                        label = { Text("Notification Campaign Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notifMessage,
                        onValueChange = { notifMessage = it },
                        label = { Text("Campaign Message Body") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (notifTitle.isNotEmpty() && notifMessage.isNotEmpty()) {
                                    viewModel.publishGlobalNotification(notifTitle, notifMessage, "Contest")
                                    notifTitle = ""
                                    notifMessage = ""
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleSecondary)
                        ) {
                            Text("PUSH CONTEST ALERT")
                        }

                        Button(
                            onClick = {
                                if (notifTitle.isNotEmpty() && notifMessage.isNotEmpty()) {
                                    viewModel.publishGlobalNotification(notifTitle, notifMessage, "Reward")
                                    notifTitle = ""
                                    notifMessage = ""
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MintGreen)
                        ) {
                            Text("PUSH REWARD ALERT")
                        }
                    }
                }
            }
        }
    }
}

data class AdminTab(val id: String, val label: String)

// ==========================================
// CENTRALIZED HANDMADE PREMIUM REUSABLE WIDGETS
// ==========================================

@Composable
fun Premium3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = PurplePrimary.copy(alpha = 0.5f)
            )
            .background(
                Brush.linearGradient(
                    colors = if (enabled) {
                        listOf(PurplePrimary, PurpleSecondary)
                    } else {
                        listOf(Color(0xFFE5E7EB), Color(0xFFD1D5DB))
                    }
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled && !isLoading) { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) Color.White else Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GameArenaCard(
    title: String,
    subtitle: String,
    description: String,
    backgroundColors: List<Color>,
    icon: ImageVector,
    badge: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(backgroundColors))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.72f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
            }

            // Big background decorative icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp, y = 10.dp)
            )
        }
    }
}
