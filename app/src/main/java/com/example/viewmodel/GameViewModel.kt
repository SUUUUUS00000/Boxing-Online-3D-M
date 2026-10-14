package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.BoxingPhysicsEngine
import com.example.model.*
import com.example.util.SoundEffects
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
  FIGHT,
  RANKED,
  CUSTOMIZATION,
  DAILY_REWARDS,
  CHAT,
  SETTINGS
}

enum class MatchmakingStatus {
  IDLE,
  SEARCHING,
  MATCH_FOUND
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = application.getSharedPreferences("boxing_online_3d_prefs", Context.MODE_PRIVATE)
  val soundEffects = SoundEffects(application)
  val physicsEngine = BoxingPhysicsEngine(soundEffects)

  private val _currentScreen = MutableStateFlow(AppScreen.FIGHT)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  // Player Customization
  private val _playerCustom = MutableStateFlow(loadPlayerCustomization())
  val playerCustom: StateFlow<FighterCustomization> = _playerCustom.asStateFlow()

  // Ranked Profile
  private val _rankedProfile = MutableStateFlow(loadRankedProfile())
  val rankedProfile: StateFlow<RankedProfile> = _rankedProfile.asStateFlow()

  // Matchmaking
  private val _matchmakingStatus = MutableStateFlow(MatchmakingStatus.IDLE)
  val matchmakingStatus: StateFlow<MatchmakingStatus> = _matchmakingStatus.asStateFlow()

  private val _searchTimeSeconds = MutableStateFlow(0)
  val searchTimeSeconds: StateFlow<Int> = _searchTimeSeconds.asStateFlow()

  private val _matchedOpponent = MutableStateFlow<FighterCustomization?>(null)
  val matchedOpponent: StateFlow<FighterCustomization?> = _matchedOpponent.asStateFlow()

  // Active Fight State
  val playerFighter = CombatFighter(
    customization = _playerCustom.value,
    posX = 0f,
    posZ = -2.4f,
    headHealth = 100f,
    bodyHealth = 100f,
    stamina = 100f
  )

  val opponentFighter = CombatFighter(
    customization = createDefaultOpponent(),
    posX = 0f,
    posZ = 2.4f,
    headHealth = 100f,
    bodyHealth = 100f,
    stamina = 100f,
    isAI = true
  )

  // Input states
  var moveStickX: Float = 0f
  var moveStickZ: Float = 0f
  val isBodyModifierActive = MutableStateFlow(false)
  val isGuardActive = MutableStateFlow(false)

  // Fight Result Modal
  private val _fightResult = MutableStateFlow<FightResult?>(null)
  val fightResult: StateFlow<FightResult?> = _fightResult.asStateFlow()

  // Chat system
  private val _chatMessages = MutableStateFlow(generateInitialChatMessages())
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  // Daily Rewards
  private val _dailyRewards = MutableStateFlow(loadDailyRewards())
  val dailyRewards: StateFlow<List<DailyRewardItem>> = _dailyRewards.asStateFlow()

  private val _currentStreakDay = MutableStateFlow(prefs.getInt("streak_day", 1))
  val currentStreakDay: StateFlow<Int> = _currentStreakDay.asStateFlow()

  private val _canClaimDaily = MutableStateFlow(prefs.getBoolean("can_claim_daily", true))
  val canClaimDaily: StateFlow<Boolean> = _canClaimDaily.asStateFlow()

  // Leaderboard
  private val _leaderboard = MutableStateFlow(generateLeaderboard())
  val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

  private var fightLoopJob: Job? = null

  init {
    startFightLoop()
  }

  fun navigateTo(screen: AppScreen) {
    _currentScreen.value = screen
    if (screen == AppScreen.FIGHT && fightLoopJob == null) {
      startFightLoop()
    }
  }

  // --- FIGHT CONTROL ACTIONS ---
  fun onPunchTriggered(punch: PunchType) {
    val actualPunch = if (isBodyModifierActive.value) {
      when (punch) {
        PunchType.JAB -> PunchType.BODY_JAB
        PunchType.CROSS -> PunchType.BODY_CROSS
        PunchType.L_HOOK -> PunchType.BODY_HOOK
        PunchType.R_HOOK -> PunchType.BODY_HOOK
        PunchType.L_UPPERCUT -> PunchType.BODY_JAB
        PunchType.R_UPPERCUT -> PunchType.BODY_CROSS
        else -> punch
      }
    } else punch

    physicsEngine.triggerPlayerPunch(playerFighter, actualPunch)
  }

  fun onSlipTriggered(slip: ActionState) {
    physicsEngine.triggerSlip(playerFighter, slip)
  }

  fun setGuard(active: Boolean, isBody: Boolean = isBodyModifierActive.value) {
    isGuardActive.value = active
    physicsEngine.setGuard(playerFighter, isBlocking = active, isBody = isBody)
  }

  fun toggleBodyModifier() {
    isBodyModifierActive.value = !isBodyModifierActive.value
    if (isGuardActive.value) {
      physicsEngine.setGuard(playerFighter, isBlocking = true, isBody = isBodyModifierActive.value)
    }
  }

  fun onGetUpTap() {
    physicsEngine.registerPlayerGetUpTap(playerFighter)
  }

  fun onClinchOrPushTriggered() {
    physicsEngine.triggerClinchOrPush(playerFighter, opponentFighter)
  }

  fun startNewFight(opponent: FighterCustomization? = null, archetype: AIArchetype = AIArchetype.SLUGGER) {
    _fightResult.value = null

    val opp = opponent ?: createDefaultOpponent()
    playerFighter.posX = 0f
    playerFighter.posZ = -2.5f
    playerFighter.headHealth = 100f
    playerFighter.bodyHealth = 100f
    playerFighter.stamina = 100f
    playerFighter.isDown = false
    playerFighter.actionState = ActionState.IDLE
    playerFighter.knockdownsInRound = 0
    playerFighter.totalKnockdowns = 0

    // Set opponent
    val oppFighterState = opponentFighter
    oppFighterState.posX = 0f
    oppFighterState.posZ = 2.5f
    oppFighterState.headHealth = 100f
    oppFighterState.bodyHealth = 100f
    oppFighterState.stamina = 100f
    oppFighterState.isDown = false
    oppFighterState.actionState = ActionState.IDLE
    oppFighterState.knockdownsInRound = 0
    oppFighterState.totalKnockdowns = 0
    oppFighterState.aiArchetype = archetype

    // Copy customization
    val updatedOppFighter = oppFighterState.copy(customization = opp)
    opponentFighter.headHealth = 100f
    opponentFighter.bodyHealth = 100f
    opponentFighter.stamina = 100f
    opponentFighter.isDown = false
    opponentFighter.actionState = ActionState.IDLE
    opponentFighter.aiArchetype = archetype

    physicsEngine.resetMatch(playerFighter.customization, opp, archetype)
    soundEffects.playBell()
    _currentScreen.value = AppScreen.FIGHT
  }

  private fun startFightLoop() {
    fightLoopJob?.cancel()
    fightLoopJob = viewModelScope.launch {
      var lastTime = System.nanoTime()
      while (isActive) {
        val now = System.nanoTime()
        val dt = ((now - lastTime) / 1_000_000_000.0f).coerceIn(0.005f, 0.05f)
        lastTime = now

        if (_currentScreen.value == AppScreen.FIGHT && _fightResult.value == null) {
          physicsEngine.update(
            dt = dt,
            player = playerFighter,
            opponent = opponentFighter,
            moveX = moveStickX,
            moveZ = moveStickZ
          )

          // Check for match completion
          if (physicsEngine.isMatchOver && _fightResult.value == null) {
            handleMatchFinished()
          }
        }

        delay(16) // ~60 FPS update rate
      }
    }
  }

  private fun handleMatchFinished() {
    val isVictorious = physicsEngine.matchWinner == playerFighter
    val rpChange = if (isVictorious) 35 + (playerFighter.customization.statPower * 2) else -18
    val coinsWon = if (isVictorious) 250 else 50

    val updatedProfile = _rankedProfile.value.let { p ->
      val newRp = (p.rp + rpChange).coerceAtLeast(0)
      val newWins = if (isVictorious) p.wins + 1 else p.wins
      val newLosses = if (!isVictorious) p.losses + 1 else p.losses
      val newKos = if (isVictorious && physicsEngine.matchEndReason.contains("KNOCKOUT")) p.kos + 1 else p.kos
      val newStreak = if (isVictorious) p.winStreak + 1 else 0
      p.copy(
        rp = newRp,
        wins = newWins,
        losses = newLosses,
        kos = newKos,
        winStreak = newStreak,
        coins = p.coins + coinsWon,
        rankTier = RankedProfile.calculateTier(newRp)
      )
    }
    _rankedProfile.value = updatedProfile
    saveRankedProfile(updatedProfile)

    _fightResult.value = FightResult(
      isVictory = isVictorious,
      outcomeText = physicsEngine.matchEndReason,
      roundEnded = physicsEngine.roundNumber,
      timeRemaining = String.format("%02d:%02d", physicsEngine.roundTimeRemaining.toInt() / 60, physicsEngine.roundTimeRemaining.toInt() % 60),
      playerPunchesLanded = physicsEngine.playerPunchesLanded,
      playerPunchesThrown = physicsEngine.playerPunchesThrown,
      opponentPunchesLanded = physicsEngine.opponentPunchesLanded,
      opponentPunchesThrown = physicsEngine.opponentPunchesThrown,
      rpChange = rpChange,
      coinsEarned = coinsWon
    )
  }

  // --- MATCHMAKING SYSTEM ---
  fun startRankedMatchmaking() {
    _matchmakingStatus.value = MatchmakingStatus.SEARCHING
    _searchTimeSeconds.value = 0

    viewModelScope.launch {
      for (i in 1..4) {
        delay(1000)
        _searchTimeSeconds.value = i
      }
      // Opponent found!
      val pool = listOf(
        FighterCustomization(id = "opp1", name = "CARLOS V.", nickname = "EL MATADOR", country = "MX", gloveColorIndex = 0, shortsColorIndex = 2, skinToneIndex = 1),
        FighterCustomization(id = "opp2", name = "MARCUS K.", nickname = "THE HAMMER", country = "US", gloveColorIndex = 4, shortsColorIndex = 0, skinToneIndex = 2),
        FighterCustomization(id = "opp3", name = "DMITRY B.", nickname = "BEAR", country = "RU", gloveColorIndex = 3, shortsColorIndex = 4, skinToneIndex = 0),
        FighterCustomization(id = "opp4", name = "KENJI S.", nickname = "VIPER", country = "JP", gloveColorIndex = 2, shortsColorIndex = 5, skinToneIndex = 3)
      )
      val opponent = pool.random()
      _matchedOpponent.value = opponent
      _matchmakingStatus.value = MatchmakingStatus.MATCH_FOUND
      delay(1800)
      _matchmakingStatus.value = MatchmakingStatus.IDLE
      startNewFight(opponent, AIArchetype.values().random())
    }
  }

  fun cancelMatchmaking() {
    _matchmakingStatus.value = MatchmakingStatus.IDLE
  }

  // --- CUSTOMIZATION SYSTEM ---
  fun updateCustomization(newCustom: FighterCustomization) {
    _playerCustom.value = newCustom
    savePlayerCustomization(newCustom)
  }

  fun upgradeStat(statName: String) {
    val currentCoins = _rankedProfile.value.coins
    val cost = 500
    if (currentCoins < cost) return

    val cur = _playerCustom.value
    val updated = when (statName) {
      "POWER" -> cur.copy(statPower = (cur.statPower + 1).coerceAtMost(10))
      "SPEED" -> cur.copy(statSpeed = (cur.statSpeed + 1).coerceAtMost(10))
      "STAMINA" -> cur.copy(statStamina = (cur.statStamina + 1).coerceAtMost(10))
      "CHIN" -> cur.copy(statChin = (cur.statChin + 1).coerceAtMost(10))
      else -> cur
    }

    _playerCustom.value = updated
    savePlayerCustomization(updated)

    val updatedProfile = _rankedProfile.value.copy(coins = currentCoins - cost)
    _rankedProfile.value = updatedProfile
    saveRankedProfile(updatedProfile)
  }

  // --- DAILY REWARDS SYSTEM ---
  fun claimDailyReward(day: Int) {
    val list = _dailyRewards.value.toMutableList()
    val index = list.indexOfFirst { it.day == day }
    if (index >= 0 && !list[index].isClaimed) {
      val item = list[index]
      list[index] = item.copy(isClaimed = true)
      _dailyRewards.value = list

      // Award coins & RP
      val profile = _rankedProfile.value
      val newCoins = profile.coins + item.coins
      val newRp = profile.rp + item.rpBonus
      val updated = profile.copy(
        coins = newCoins,
        rp = newRp,
        rankTier = RankedProfile.calculateTier(newRp)
      )
      _rankedProfile.value = updated
      saveRankedProfile(updated)

      _canClaimDaily.value = false
      prefs.edit().putBoolean("can_claim_daily", false).apply()
      soundEffects.playBell()
    }
  }

  // --- CHAT SYSTEM ---
  fun sendChatMessage(text: String, isTaunt: Boolean = false) {
    if (text.isBlank()) return
    val newMsg = ChatMessage(
      id = System.currentTimeMillis().toString(),
      senderName = _playerCustom.value.name,
      senderRank = _rankedProfile.value.rankTier,
      message = text,
      timeAgo = "Just now",
      isTaunt = isTaunt,
      isPlayer = true
    )
    _chatMessages.value = listOf(newMsg) + _chatMessages.value

    // Auto-respond with realistic online boxing player message if not taunt
    if (!isTaunt) {
      viewModelScope.launch {
        delay(2200)
        val sampleResponses = listOf(
          "Nice combo in your last ranked match!",
          "Who is ready for a championship spar?",
          "Watch out for counter hooks against out-boxers!",
          "GGs all round, climbing to Platinum today!",
          "OB3D physics on mobile feels so smooth!"
        )
        val botMsg = ChatMessage(
          id = (System.currentTimeMillis() + 1).toString(),
          senderName = listOf("GloveMaster", "TysonFan_99", "IronFist_PRO", "StaminaKing").random(),
          senderRank = listOf(RankTier.GOLD, RankTier.PLATINUM, RankTier.DIAMOND).random(),
          message = sampleResponses.random(),
          timeAgo = "Just now",
          isPlayer = false
        )
        _chatMessages.value = listOf(botMsg) + _chatMessages.value
      }
    }
  }

  // --- PERSISTENCE HELPERS ---
  private fun loadPlayerCustomization(): FighterCustomization {
    return FighterCustomization(
      id = "player_1",
      name = prefs.getString("custom_name", "ALEXEI") ?: "ALEXEI",
      nickname = prefs.getString("custom_nickname", "THE IRON PHANTOM") ?: "THE IRON PHANTOM",
      country = prefs.getString("custom_country", "RU") ?: "RU",
      stance = if (prefs.getString("custom_stance", "ORTHODOX") == "SOUTHPAW") Stance.SOUTHPAW else Stance.ORTHODOX,
      skinToneIndex = prefs.getInt("custom_skin", 0),
      gloveColorIndex = prefs.getInt("custom_glove", 1), // Purple by default like screenshot
      shortsColorIndex = prefs.getInt("custom_shorts", 1), // Purple shorts
      shoesColorIndex = prefs.getInt("custom_shoes", 1),
      heightScale = prefs.getFloat("custom_height", 1.0f),
      reachScale = prefs.getFloat("custom_reach", 1.0f),
      muscleScale = prefs.getFloat("custom_muscle", 1.0f),
      statPower = prefs.getInt("stat_power", 6),
      statSpeed = prefs.getInt("stat_speed", 6),
      statStamina = prefs.getInt("stat_stamina", 6),
      statChin = prefs.getInt("stat_chin", 6)
    )
  }

  private fun savePlayerCustomization(c: FighterCustomization) {
    prefs.edit()
      .putString("custom_name", c.name)
      .putString("custom_nickname", c.nickname)
      .putString("custom_country", c.country)
      .putString("custom_stance", c.stance.name)
      .putInt("custom_skin", c.skinToneIndex)
      .putInt("custom_glove", c.gloveColorIndex)
      .putInt("custom_shorts", c.shortsColorIndex)
      .putInt("custom_shoes", c.shoesColorIndex)
      .putFloat("custom_height", c.heightScale)
      .putFloat("custom_reach", c.reachScale)
      .putFloat("custom_muscle", c.muscleScale)
      .putInt("stat_power", c.statPower)
      .putInt("stat_speed", c.statSpeed)
      .putInt("stat_stamina", c.statStamina)
      .putInt("stat_chin", c.statChin)
      .apply()
  }

  private fun loadRankedProfile(): RankedProfile {
    val rp = prefs.getInt("ranked_rp", 1250)
    return RankedProfile(
      rp = rp,
      wins = prefs.getInt("ranked_wins", 14),
      losses = prefs.getInt("ranked_losses", 3),
      kos = prefs.getInt("ranked_kos", 9),
      winStreak = prefs.getInt("ranked_streak", 4),
      coins = prefs.getInt("ranked_coins", 3200),
      rankTier = RankedProfile.calculateTier(rp)
    )
  }

  private fun saveRankedProfile(p: RankedProfile) {
    prefs.edit()
      .putInt("ranked_rp", p.rp)
      .putInt("ranked_wins", p.wins)
      .putInt("ranked_losses", p.losses)
      .putInt("ranked_kos", p.kos)
      .putInt("ranked_streak", p.winStreak)
      .putInt("ranked_coins", p.coins)
      .apply()
  }

  private fun createDefaultOpponent(): FighterCustomization {
    return FighterCustomization(
      id = "opp_default",
      name = "VIKTOR",
      nickname = "THE GREEN CYCLONE",
      country = "KZ",
      stance = Stance.ORTHODOX,
      skinToneIndex = 0,
      gloveColorIndex = 0, // Red gloves as in screenshot!
      shortsColorIndex = 2, // Green shorts as in screenshot!
      shoesColorIndex = 2, // Green shoes as in screenshot!
      statPower = 6,
      statSpeed = 6,
      statStamina = 6,
      statChin = 6
    )
  }

  private fun loadDailyRewards(): List<DailyRewardItem> {
    return listOf(
      DailyRewardItem(1, "Bronze Purse", 500, 25, isClaimed = true),
      DailyRewardItem(2, "Energy Drink", 1000, 35, isClaimed = false, isAvailableToday = true),
      DailyRewardItem(3, "Neon Cyan Gloves", 1500, 50, specialGearName = "Neon Cyan Gloves"),
      DailyRewardItem(4, "Fighter Stipend", 2500, 60),
      DailyRewardItem(5, "Gladiator Trunks", 3000, 75, specialGearName = "Gladiator Trunks"),
      DailyRewardItem(6, "Pro Handwraps", 4000, 90),
      DailyRewardItem(7, "Champion Gold Gloves", 6000, 150, specialGearName = "Championship Gold Gloves")
    )
  }

  private fun generateInitialChatMessages(): List<ChatMessage> {
    return listOf(
      ChatMessage("1", "Tyson_Fan", RankTier.GOLD, "Who wants to spar in Gold division?", "2m ago"),
      ChatMessage("2", "KOBrawler", RankTier.PLATINUM, "Tip: Use body hooks to drain their stamina fast!", "5m ago"),
      ChatMessage("3", "GoldenHook", RankTier.CHAMPION, "That knockdown 10-count gets intense at 3 knockdowns!", "12m ago"),
      ChatMessage("4", "PhantomStriker", RankTier.DIAMOND, "Ranked ladder reset is coming next week!", "25m ago")
    )
  }

  private fun generateLeaderboard(): List<LeaderboardEntry> {
    return listOf(
      LeaderboardEntry(1, "GOLIATH_KO", "THE BEAST", "US", 4420, RankTier.UNDISPUTED, "68-2", "88%"),
      LeaderboardEntry(2, "ALEXEI (YOU)", "THE IRON PHANTOM", "RU", 1250, RankTier.SILVER, "14-3", "64%"),
      LeaderboardEntry(3, "CANELO_G", "CINNAMON", "MX", 3980, RankTier.CHAMPION, "54-4", "76%"),
      LeaderboardEntry(4, "AJ_POWER", "HEAVY ARTILLERY", "GB", 3650, RankTier.CHAMPION, "49-6", "82%"),
      LeaderboardEntry(5, "INQUE_MONSTER", "SPEED DEMON", "JP", 3320, RankTier.DIAMOND, "42-3", "90%"),
      LeaderboardEntry(6, "GGG_TRIPLE", "THUNDER JAW", "KZ", 3190, RankTier.DIAMOND, "40-5", "85%"),
      LeaderboardEntry(7, "TYSON_88", "KID DYNAMITE", "US", 2850, RankTier.PLATINUM, "36-8", "80%")
    )
  }
}
