/**
 * PocketQuiz - Interactive Core PWA Script File
 */

// Global State
let state = {
  coins: 2450,
  streak: 3,
  hasClaimedStreak: false,
  profile: {
    name: "Alex Rivers",
    id: "PKT-8821"
  },
  walletPreset: {
    coins: 0,
    amount: 0
  },
  transactions: [
    { id: "TXN-4901", method: "UPI", desc: "Redeemed conversion pack", coins: 1000, rs: 50, status: "SUCCESS", timestamp: "Today, 11:24 AM" },
    { id: "TXN-2804", method: "1v1 Battle Entry", desc: "Compete live solver arena", coins: -20, rs: 0, status: "DEBIT", timestamp: "Yesterday, 04:15 PM" },
    { id: "TXN-1092", method: "1v1 Match Win", desc: "Double reward winner reward", coins: 40, rs: 0, status: "WON", timestamp: "Yesterday, 04:22 PM" }
  ]
};

// Math Game State Variables
let gameActive = false;
let gameTimer = null;
let gameSeconds = 15;
let currentRound = 1;
let playerScore = 0;
let opponentScore = 0;
let opponentSpeed = 2200; // MS to solve
let opponentAccuracy = 0.82; // Probability of right answer
let opponentName = "Sarah_0x";
let currentQuestionAnswer = 0;
let consecutiveRoundLimit = 5;
let gameTimerTracker = null;
let currentActiveView = "home";

// Web Audio API Retro 8-bit Synthesizer
const synth = {
  ctx: null,
  init() {
    if (!this.ctx) {
      this.ctx = new (window.AudioContext || window.webkitAudioContext)();
    }
    if (this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  },
  play(type, freq, duration) {
    try {
      this.init();
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = type || 'sine';
      osc.frequency.setValueAtTime(freq || 440, this.ctx.currentTime);
      gain.gain.setValueAtTime(0.15, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, this.ctx.currentTime + duration);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + duration);
    } catch (e) {
      console.warn("Audio Context error ignored", e);
    }
  },
  click() { this.play('triangle', 300, 0.08); },
  success() { this.play('square', 880, 0.15); setTimeout(() => this.play('square', 1320, 0.2), 100); },
  error() { this.play('sawtooth', 150, 0.3); },
  fanfare() {
    const notes = [523.25, 659.25, 783.99, 1046.50]; // C5, E5, G5, C6
    notes.forEach((freq, idx) => {
      setTimeout(() => this.play('sine', freq, 0.15), idx * 110);
    });
  }
};

// Local storage backup persistence
function loadLocalStorage() {
  const savedState = localStorage.getItem("pocketquiz_state");
  if (savedState) {
    try {
      state = JSON.parse(savedState);
    } catch (e) {
      console.error(e);
    }
  }
}

function saveLocalStorage() {
  localStorage.setItem("pocketquiz_state", JSON.stringify(state));
  syncUI();
}

// UI Elements Syncing
function syncUI() {
  // Sync core numbers
  document.getElementById("coinBalanceText").innerText = Number(state.coins).toLocaleString();
  document.getElementById("walletCoinsText").innerText = Number(state.coins).toLocaleString();
  document.getElementById("equivalentRsText").innerText = `₹${(state.coins * 0.05).toFixed(2)}`;
  
  // Profile banners
  document.getElementById("userNameText").innerText = state.profile.name;
  document.getElementById("userIdBadge").innerText = `ID: ${state.profile.id}`;
  document.getElementById("profileNameBig").innerText = state.profile.name;
  document.getElementById("profileIdBig").innerText = `ID: ${state.profile.id}`;
  
  // Leaderboard Current entries
  document.getElementById("lbCurrentUserName").innerText = state.profile.name;
  document.getElementById("lbCurrentUserCoins").innerText = `${state.coins} Coins`;

  // Avatar Text
  const initials = state.profile.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  document.getElementById("avatarText").innerText = initials;
  document.getElementById("profileAvatarBig").innerText = initials;

  // Synced Forms inputs default
  document.getElementById("profileNameInput").placeholder = state.profile.name;
  document.getElementById("profileUpiInput").placeholder = state.transactions[0]?.upiId || "alex@ybl";

  // Streak logic UI sync
  const claimBtn = document.getElementById("streakClaimBtn");
  const giftIcon = document.getElementById("dayGiftIcon");
  const streakTag = document.getElementById("streakTagText");
  if (state.hasClaimedStreak) {
    claimBtn.classList.add("opacity-50", "pointer-events-none");
    giftIcon.classList.remove("animate-bounce");
    giftIcon.innerText = "🎁 CLAIMED";
    streakTag.innerText = "DAY 3 • STREAK COMPLETED! ✨";
    streakTag.className = "text-[10px] font-extrabold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full";
  }

  // Populate withdraw logs table list
  const container = document.getElementById("txLogsContainer");
  container.innerHTML = "";
  
  state.transactions.slice(0, 3).forEach(tx => {
    let statusColor, amtLabel;
    if (tx.status === "SUCCESS") {
      statusColor = "text-emerald-600 bg-emerald-50 border-emerald-100";
      amtLabel = `<span class="text-rose-600 font-bold font-mono-retro">-${tx.coins} 🪙</span>`;
    } else if (tx.status === "WON") {
      statusColor = "text-emerald-700 bg-emerald-50 border-purple-100";
      amtLabel = `<span class="text-emerald-600 font-bold font-mono-retro">+${tx.coins} 🪙</span>`;
    } else {
      statusColor = "text-slate-500 bg-slate-50 border-slate-100";
      amtLabel = `<span class="text-slate-600 font-bold font-mono-retro">${tx.coins} 🪙</span>`;
    }

    container.innerHTML += `
      <div class="bg-white border border-purple-100 p-3 rounded-xl flex justify-between items-center shadow-sm">
        <div class="flex flex-col items-start text-left">
          <span class="text-xs font-black text-slate-800">${tx.method}</span>
          <span class="text-[10px] text-slate-400 font-medium">${tx.timestamp} • ${tx.desc}</span>
        </div>
        <div class="flex flex-col items-end gap-1">
          ${amtLabel}
          <span class="text-[8px] font-black border px-1.5 py-0.2 rounded uppercase ${statusColor}">${tx.status}</span>
        </div>
      </div>
    `;
  });
}

// VIEW SWITCHING (Bottom tab bar controls)
function switchTab(tabId) {
  synth.click();
  
  // Reset all Indicators
  document.getElementById("navActiveIndicatorHome").classList.add("hidden");
  document.getElementById("navActiveIndicatorContests").classList.add("hidden");
  document.getElementById("navActiveIndicatorLeaderboard").classList.add("hidden");
  document.getElementById("navActiveIndicatorProfile").classList.add("hidden");

  // Dim buttons opacity
  document.getElementById("navBtnHome").classList.add("opacity-40");
  document.getElementById("navBtnContests").classList.add("opacity-40");
  document.getElementById("navBtnLeaderboard").classList.add("opacity-40");
  document.getElementById("navBtnProfile").classList.add("opacity-40");

  // Hide modal style viewports
  document.getElementById("tabWallet").classList.add("hidden");
  document.getElementById("tabContests").classList.add("hidden");
  document.getElementById("tabLeaderboard").classList.add("hidden");
  document.getElementById("tabProfile").classList.add("hidden");

  // Activate chosen target
  if (tabId === "home") {
    document.getElementById("navActiveIndicatorHome").classList.remove("hidden");
    document.getElementById("navBtnHome").classList.remove("opacity-40");
    currentActiveView = "home";
  } else if (tabId === "wallet") {
    document.getElementById("tabWallet").classList.remove("hidden");
    document.getElementById("tabWallet").classList.add("tab-view-slide-in");
    currentActiveView = "wallet";
  } else if (tabId === "contests") {
    document.getElementById("navActiveIndicatorContests").classList.remove("hidden");
    document.getElementById("navBtnContests").classList.remove("opacity-40");
    document.getElementById("tabContests").classList.remove("hidden");
    document.getElementById("tabContests").classList.add("tab-view-slide-in");
    currentActiveView = "contests";
  } else if (tabId === "leaderboard") {
    document.getElementById("navActiveIndicatorLeaderboard").classList.remove("hidden");
    document.getElementById("navBtnLeaderboard").classList.remove("opacity-40");
    document.getElementById("tabLeaderboard").classList.remove("hidden");
    document.getElementById("tabLeaderboard").classList.add("tab-view-slide-in");
    currentActiveView = "leaderboard";
  } else if (tabId === "profile") {
    document.getElementById("navActiveIndicatorProfile").classList.remove("hidden");
    document.getElementById("navBtnProfile").classList.remove("opacity-40");
    document.getElementById("tabProfile").classList.remove("hidden");
    document.getElementById("tabProfile").classList.add("tab-view-slide-in");
    currentActiveView = "profile";
  }
}

function closeCurrentTab() {
  switchTab("home");
}

// Marketing Slides Rotator logic
let activeSlideIndex = 0;
const totalSlides = 3;

function rotateSlides() {
  if (gameActive) return; // Keep slides static during matching/gameplay
  
  // Get active slide out
  document.getElementById(`slide${activeSlideIndex}`).classList.add("opacity-0");
  document.getElementById(`slide${activeSlideIndex}`).classList.remove("opacity-100", "z-10");
  
  // Increment index
  activeSlideIndex = (activeSlideIndex + 1) % totalSlides;
  
  // Set new active slide
  document.getElementById(`slide${activeSlideIndex}`).classList.add("opacity-100", "z-10");
  document.getElementById(`slide${activeSlideIndex}`).classList.remove("opacity-0");

  // Sync index dots indicator
  const dots = document.querySelectorAll(".slider-dot");
  dots.forEach((dot, idx) => {
    if (idx === activeSlideIndex) {
      dot.className = "slider-dot w-1.5 h-1.5 rounded-full bg-white border border-white transition-opacity";
    } else {
      dot.className = "slider-dot w-1.5 h-1.5 rounded-full bg-white/50 border border-white/50 transition-opacity";
    }
  });
}

// Trigger sliding interval
setInterval(rotateSlides, 4000);

// CLAIM DAILY STREAK REWARD
function claimStreakReward() {
  if (state.hasClaimedStreak) return;
  synth.success();
  
  state.coins += 15;
  state.hasClaimedStreak = true;
  
  // Push entry in history log
  state.transactions.unshift({
    id: "TXN-" + Math.floor(1000 + Math.random() * 9000),
    method: "Daily Streak bonus",
    desc: "Earned day 3 checklist reward",
    coins: 15,
    rs: 0,
    status: "WON",
    timestamp: "Just now"
  });
  
  showToast("🎉 Pocket Streak Reward Claimed! +15 Coins added.");
  saveLocalStorage();
}

// PROFILE CUSTOMIZATION SETTINGS
function saveUserProfile() {
  synth.click();
  const nameInput = document.getElementById("profileNameInput").value.trim();
  const upiInput = document.getElementById("profileUpiInput").value.trim();

  if (nameInput) {
    state.profile.name = nameInput;
  }
  showToast("👤 Profile details updated successfully!");
  saveLocalStorage();
  closeCurrentTab();
}

// WALLET PRESET PACK SELECTOR
function selectWalletPreset(btn, coins, amount) {
  synth.click();
  // Clear other active colors
  document.querySelectorAll(".preset-btn").forEach(b => {
    b.classList.remove("active");
  });
  // Add active style to selector
  btn.classList.add("active");
  
  state.walletPreset.coins = coins;
  state.walletPreset.amount = amount;
}

// WITHDRAWAL SUMMISSION ACTION
function submitWithdrawal() {
  const upiId = document.getElementById("upiIdInput").value.trim();
  
  if (!upiId) {
    synth.error();
    showToast("⚠️ Missing UPI ID! Please enter a valid payment identifier.");
    return;
  }
  
  if (state.walletPreset.coins <= 0) {
    synth.error();
    showToast("⚠️ Conversion Pack required. Select an exchange bundle.");
    return;
  }

  if (state.coins < state.walletPreset.coins) {
    synth.error();
    showToast("❌ Insufficient Coin Balance. Win more math battles!");
    return;
  }

  synth.success();
  // Deduct client balances
  state.coins -= state.walletPreset.coins;
  
  // Record dynamic transactions bundle
  state.transactions.unshift({
    id: "TXN-" + Math.floor(1000 + Math.random() * 9000),
    method: "UPI Redeem Cashout",
    desc: `Deducted for conversion (₹${state.walletPreset.amount})`,
    coins: state.walletPreset.coins,
    rs: state.walletPreset.amount,
    status: "SUCCESS",
    timestamp: "Just now",
    upiId: upiId
  });

  document.getElementById("upiIdInput").value = "";
  // Reset buttons selection states
  document.querySelectorAll(".preset-btn").forEach(b => b.classList.remove("active"));
  state.walletPreset.coins = 0;
  state.walletPreset.amount = 0;

  showToast(`💸 Cashout Success! ₹${state.transactions[0].rs} transferred securely to UPI.`);
  saveLocalStorage();
}

// JOINS TOURNAMENT CONTESTS
function joinContest(name, fee, prize) {
  synth.click();
  if (state.coins < fee) {
    synth.error();
    showToast("❌ Insufficient Coin Balance to buy ticket entry.");
    return;
  }

  synth.success();
  state.coins -= fee;
  state.transactions.unshift({
    id: "TXN-" + Math.floor(1000 + Math.random() * 9000),
    method: "Contest Ticket fee",
    desc: `Joined ${name}`,
    coins: -fee,
    rs: 0,
    status: "DEBIT",
    timestamp: "Just now"
  });

  showToast(`🎖️ Entry Confirmed! Joined: ${name}. Be online in 15 mins.`);
  saveLocalStorage();
}


// DYNAMIC REAL-TIME TOASTS SYSTEM
let toastTimer = null;
function showToast(message, icon = "📢") {
  document.getElementById("toastIconText").innerText = icon;
  document.getElementById("toastMessageText").innerText = message;
  
  const box = document.getElementById("toastNotificationBox");
  box.classList.remove("hidden");
  // Small animation entry slides
  setTimeout(() => {
    box.classList.remove("translate-y-10");
  }, 10);

  // Clear past timeouts
  if (toastTimer) clearTimeout(toastTimer);

  toastTimer = setTimeout(dismissToast, 4000);
}

function dismissToast() {
  const box = document.getElementById("toastNotificationBox");
  box.classList.add("translate-y-10");
  setTimeout(() => {
    box.classList.add("hidden");
  }, 300);
}


// DYNAMIC MULTIPLAYER (1v1) MATCHMAKING SYSTEM ENGINE
function startMatching() {
  synth.init();
  synth.click();
  
  if (state.coins < 20) {
    synth.error();
    showToast("❌ Minimum 20 Coins required to join 1v1 Battle Arena.");
    return;
  }

  // Hide result window if playing again
  document.getElementById("viewMatchResultWindow").classList.add("hidden");

  // Show matchmaking modal
  document.getElementById("view1v1Matchmaking").classList.remove("hidden");
  
  // Set default initial states
  document.getElementById("matchmakingStateTitle").innerText = "Searching Opponent";
  document.getElementById("matchmakingStateDesc").innerText = "Checking solver lobbies with ping < 15ms...";
  document.getElementById("opponentSearchCard").innerHTML = `
    <div class="w-12 h-12 rounded-full bg-slate-100 border-2 border-dashed border-purple-300 flex items-center justify-center text-lg animate-spin">⏳</div>
    <span class="font-bold text-[11px] text-slate-400 animate-pulse">Searching...</span>
    <span class="text-[9px] font-bold text-slate-400">Connecting</span>
  `;

  // Dynamic interval opponent sequence
  const opponents = [
    { name: "Sarah_0x", speed: 2100, accuracy: 0.85, avatar: "👩" },
    { name: "Rajat_Math", speed: 1700, accuracy: 0.94, avatar: "🤴" },
    { name: "LionStar", speed: 2300, accuracy: 0.78, avatar: "🦁" },
    { name: "PandaSolver", speed: 2500, accuracy: 0.75, avatar: "🐼" }
  ];

  const chosen = opponents[Math.floor(Math.random() * opponents.length)];
  opponentName = chosen.name;
  opponentSpeed = chosen.speed;
  opponentAccuracy = chosen.accuracy;

  // STEP 1: Search simulation timing checkouts
  setTimeout(() => {
    document.getElementById("matchmakingStateDesc").innerText = "Coordinator matching found in Asia-West node...";
    synth.play('sine', 600, 0.1);
  }, 1200);

  // STEP 2: Match confirmed
  setTimeout(() => {
    synth.fanfare();
    document.getElementById("matchmakingStateTitle").innerText = "Match Confirmed ⚔️";
    document.getElementById("matchmakingStateDesc").style.color = "#7C3AED";
    document.getElementById("matchmakingStateDesc").innerText = "Room locked! Syncing clocks...";
    
    // Fill opponent profile
    document.getElementById("opponentSearchCard").innerHTML = `
      <div class="w-12 h-12 rounded-full bg-[#FEF3C7] flex items-center justify-center text-2xl shadow ring-2 ring-amber-300">${chosen.avatar}</div>
      <span class="font-extrabold text-[12px] text-slate-800">${chosen.name}</span>
      <span class="text-[9px] font-black text-emerald-600">ONLINE</span>
    `;
    
    // Set matching subheader in Game View too
    document.getElementById("oppGameNameSub").innerText = chosen.name.toUpperCase();
    document.getElementById("oppGameAvatar").innerText = chosen.avatar;
  }, 2600);

  // STEP 3: Enter arena gameplay
  setTimeout(() => {
    document.getElementById("view1v1Matchmaking").classList.add("hidden");
    startArenaMatchplay();
  }, 4500);
}

function cancelMatchmaking() {
  synth.click();
  document.getElementById("view1v1Matchmaking").classList.add("hidden");
  showToast("Matchmaking canceled by solver user.");
}


// BATTLE ARENA INDIVIDUAL RUNNING INSTANCE
function startArenaMatchplay() {
  gameActive = true;
  currentRound = 1;
  playerScore = 0;
  opponentScore = 0;
  
  // Deduct matching ticket fee dynamically 20 coins
  state.coins -= 20;
  saveLocalStorage();

  document.getElementById("view1v1GamePlayboard").classList.remove("hidden");
  loadNewRoundQuestion();
}

function stopArenaClocks() {
  if (gameTimerTracker) clearInterval(gameTimerTracker);
}

function loadNewRoundQuestion() {
  // Check completion bound limit
  if (currentRound > consecutiveRoundLimit) {
    concludeMatchOutcome();
    return;
  }

  stopArenaClocks();
  
  // Set round visual tags
  document.getElementById("arenaRoundTag").innerText = `ROUND ${currentRound} OF ${consecutiveRoundLimit}`;
  
  // Reset opponent ticker status message
  document.getElementById("tickerStatusIcon").innerText = "⚡";
  document.getElementById("tickerStatusText").innerText = `${opponentName} is studying the equation...`;

  // Generate math equation solver
  const math = generateRandomSolver();
  document.getElementById("arenaQuestionDisplay").innerText = `${math.q} = ?`;
  currentQuestionAnswer = math.a;

  // Populate answers buttons
  const options = distributeOptions(math.a);
  const btns = document.querySelectorAll(".option-btn");
  
  btns.forEach((btn, idx) => {
    btn.innerText = options[idx];
    btn.disabled = false;
    btn.className = "option-btn bg-white border border-purple-150 p-4 rounded-2xl text-base font-extrabold text-slate-800 shadow-sm hover:border-quiz-primary active:scale-95 transition-all text-center";
  });

  // Start countdown clock timers (15 seconds per round)
  gameSeconds = 15;
  document.getElementById("gameTimerValue").innerText = `${gameSeconds}s`;
  document.getElementById("gameTimerValue").style.color = "#7C3AED";

  gameTimerTracker = setInterval(() => {
    gameSeconds--;
    document.getElementById("gameTimerValue").innerText = `${gameSeconds}s`;
    
    if (gameSeconds <= 4) {
      document.getElementById("gameTimerValue").style.color = "#DC2626"; // ALERT RED
      synth.play('square', 400, 0.05);
    }

    if (gameSeconds <= 0) {
      stopArenaClocks();
      // Round timed out - counts as failed round
      synth.error();
      showToast("⏳ Round timed out! Zero points awarded.");
      
      // Auto move next round
      currentRound++;
      setTimeout(loadNewRoundQuestion, 1500);
    }
  }, 1000);

  // Simulated Opponent automated answers solver timing interval!
  const delay = Math.floor(Math.random() * 1500) + opponentSpeed - 400;
  setTimeout(() => {
    if (!gameActive || gameSeconds <= 0) return;
    
    const correct = Math.random() < opponentAccuracy;
    if (correct) {
      opponentScore += 1;
      document.getElementById("oppGameScoreText").innerText = `${opponentScore} PTS`;
      document.getElementById("tickerStatusIcon").innerText = "🎯";
      document.getElementById("tickerStatusText").innerText = `${opponentName} answered correctly!`;
      synth.play('triangle', 600, 0.1);
    } else {
      document.getElementById("tickerStatusIcon").innerText = "❌";
      document.getElementById("tickerStatusText").innerText = `${opponentName} answered incorrectly!`;
    }
  }, delay);
}

// Generate simple arithmetic math equations
function generateRandomSolver() {
  const types = ["add", "sub", "mul", "div"];
  const type = types[Math.floor(Math.random() * types.length)];
  let num1, num2, q, a;

  if (type === "add") {
    num1 = Math.floor(Math.random() * 60) + 10;
    num2 = Math.floor(Math.random() * 50) + 5;
    q = `${num1} + ${num2}`;
    a = num1 + num2;
  } else if (type === "sub") {
    num1 = Math.floor(Math.random() * 70) + 30;
    num2 = Math.floor(Math.random() * 28) + 3;
    q = `${num1} - ${num2}`;
    a = num1 - num2;
  } else if (type === "mul") {
    num1 = Math.floor(Math.random() * 12) + 2;
    num2 = Math.floor(Math.random() * 9) + 2;
    q = `${num1} × ${num2}`;
    a = num1 * num2;
  } else {
    // Div
    const divOptions = [
      { q: "45 / 5", a: 9 },
      { q: "64 / 8", a: 8 },
      { q: "90 / 6", a: 15 },
      { q: "120 / 10", a: 12 },
      { q: "84 / 4", a: 21 },
      { q: "110 / 5", a: 22 }
    ];
    const picked = divOptions[Math.floor(Math.random() * divOptions.length)];
    q = picked.q;
    a = picked.a;
  }

  return { q, a };
}

// Distribute multiple choice options
function distributeOptions(correct) {
  const results = new Set([correct]);
  while (results.size < 4) {
    const deviation = Math.floor(Math.random() * 16) - 8;
    const item = correct + (deviation === 0 ? 3 : deviation);
    if (item > 0) results.add(item);
  }
  return Array.from(results).sort(() => Math.random() - 0.5);
}

// Submit answers selection
function submitArenaAnswer(btn) {
  stopArenaClocks();
  
  // Disable options
  document.querySelectorAll(".option-btn").forEach(b => b.disabled = true);
  
  const pickedVal = parseInt(btn.innerText);
  if (pickedVal === currentQuestionAnswer) {
    synth.success();
    btn.className = "option-btn bg-emerald-500 text-white border-transparent p-4 rounded-2xl text-base font-extrabold shadow-md transform scale-102 transition-all text-center";
    
    // Add player scores
    playerScore += 1;
    document.getElementById("playerGameScoreText").innerText = `${playerScore} PTS`;
    showToast("🎯 Correct!", "🎉");
  } else {
    synth.error();
    btn.className = "option-btn bg-rose-500 text-white border-transparent p-4 rounded-2xl text-base font-extrabold shadow-md transform scale-102 transition-all text-center";
    showToast(`❌ Wrong answer! Correct is: ${currentQuestionAnswer}`);
  }

  // Increment round counters
  currentRound++;
  setTimeout(loadNewRoundQuestion, 1600);
}


// BATTLE ENDED RESULTS PANEL
function concludeMatchOutcome() {
  gameActive = false;
  synth.fanfare();
  
  // Show Result Windows
  document.getElementById("view1v1GamePlayboard").classList.add("hidden");
  document.getElementById("viewMatchResultWindow").classList.remove("hidden");

  const title = document.getElementById("resultTitleText");
  const sub = document.getElementById("resultSubtitleText");
  const winCrown = document.getElementById("resultWinnerCrown");
  const rewBalance = document.getElementById("resultRewardBalance");

  if (playerScore > opponentScore) {
    // Winner reward
    title.innerText = "YOU TRIUMPHED! 🏆";
    title.style.color = "#047857"; // Emerald Green
    sub.innerText = `${state.profile.name} (${playerScore} PTS) beats ${opponentName} (${opponentScore} PTS)`;
    winCrown.innerText = "👑";
    winCrown.className = "w-24 h-24 bg-amber-50 rounded-full border-2 border-amber-300 flex items-center justify-center text-4xl mx-auto shadow-md animate-bounce";
    
    // Grant prize money
    state.coins += 40;
    rewBalance.innerText = "+40 🪙";
    rewBalance.style.color = "#059669";

    state.transactions.unshift({
      id: "TXN-" + Math.floor(1000 + Math.random() * 9000),
      method: "1v1 Match Victor rewards",
      desc: `Beated opponent: ${opponentName}`,
      coins: 40,
      rs: 0,
      status: "WON",
      timestamp: "Just now"
    });
  } else if (playerScore === opponentScore) {
    // Tie match: Refund entries coins
    title.innerText = "STALEMATE TIE! 🤝";
    title.style.color = "#4D7C0F"; // Lime/Yellow
    sub.innerText = `Both speedsters answer equal score: ${playerScore} PTS`;
    winCrown.innerText = "🤝";
    winCrown.className = "w-24 h-24 bg-slate-50 rounded-full border border-slate-200 flex items-center justify-center text-4xl mx-auto";
    
    state.coins += 20; // Refund entry
    rewBalance.innerText = "+20 🪙 (Refund)";
    rewBalance.style.color = "#4B5563";

    state.transactions.unshift({
      id: "TXN-" + Math.floor(1000 + Math.random() * 9000),
      method: "1v1 Stalemate clock sync",
      desc: `Refunded battle entry tie: ${opponentName}`,
      coins: 20,
      rs: 0,
      status: "WON",
      timestamp: "Just now"
    });
  } else {
    // Player lost
    title.innerText = "BATTLE DEFEAT! ⚔️";
    title.style.color = "#BE123C"; // Rose Red
    sub.innerText = `${opponentName} (${opponentScore} PTS) bested yours (${playerScore} PTS)`;
    winCrown.innerText = "💀";
    winCrown.className = "w-24 h-24 bg-rose-50 rounded-full border border-rose-100 flex items-center justify-center text-4xl mx-auto";
    
    rewBalance.innerText = "0 🪙";
    rewBalance.style.color = "#E11D48";

    state.transactions.unshift({
      id: "TXN-" + Math.floor(1000 + Math.random() * 9000),
      method: "1v1 Match Defeated",
      desc: `Winner solver: ${opponentName}`,
      coins: -20,
      rs: 0,
      status: "DEBIT",
      timestamp: "Just now"
    });
  }

  saveLocalStorage();
}

function finishAndCloseResult() {
  synth.click();
  document.getElementById("viewMatchResultWindow").classList.add("hidden");
  switchTab("home");
}

// SOLO RUN MODE (Free custom play math challenges)
function openMathQuizSolo() {
  synth.init();
  synth.click();
  
  // Quick onboarding
  showToast("🧮 Solo Math Run Activated! Complete equations for free coins.", "🧠");
  
  // Transition simulation to direct 1v1 screen play solver styled as Single challenge!
  opponentName = "SYSTEM ROBOT";
  opponentSpeed = 99999; // Robot will not answer!
  opponentAccuracy = 0;
  consecutiveRoundLimit = 3; // Fast solver
  
  startArenaMatchplay();
}


// FIREBASE WEB SDK SERVICE WORKER PROTOCOLS (Boilerplate configurations sync fallback)
const firebaseConfig = {
  apiKey: "AIzaSyFakeKeyPlaceholderForPwaApps",
  authDomain: "pocketquiz-pwa.firebaseapp.com",
  projectId: "pocketquiz-pwa",
  storageBucket: "pocketquiz-pwa.appspot.com",
  messagingSenderId: "367291048",
  appId: "1:367291048:web:8a92bcdef0x12"
};

// Log Firebase initialization placeholder dynamically
console.log("PocketQuiz offline-first setup. Firebase Web CDN loading initialized successfully:", firebaseConfig.projectId);


// Initial Onboard syncs
window.addEventListener("DOMContentLoaded", () => {
  loadLocalStorage();
  syncUI();

  // Register PWA Service Worker protocols
  if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
      navigator.serviceWorker.register("service-worker.js")
        .then(reg => console.log("PWA service worker registered securely:", reg.scope))
        .catch(err => console.error("PWA registration failed:", err));
    });
  }
});
