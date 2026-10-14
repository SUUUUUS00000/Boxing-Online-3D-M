package com.example.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.icons.BoxingIcons

enum class RankTier(
  val displayName: String,
  val minRp: Int,
  val colorHex: Long,
  val code: String
) {
  BRONZE("Bronze", 0, 0xFFCD7F32, "BRZ"),
  SILVER("Silver", 1000, 0xFFC0C0C0, "SLV"),
  GOLD("Gold", 1600, 0xFFFFD700, "GLD"),
  PLATINUM("Platinum", 2200, 0xFF22D3EE, "PLT"),
  DIAMOND("Diamond", 2800, 0xFF818CF8, "DIA"),
  CHAMPION("Champion", 3400, 0xFFF59E0B, "CHP"),
  UNDISPUTED("Undisputed", 4000, 0xFFEC4899, "UND");

  fun getIcon(): ImageVector {
    return when (this) {
      BRONZE, SILVER, GOLD -> BoxingIcons.MedalBadge
      PLATINUM -> BoxingIcons.DiamondGem
      DIAMOND -> BoxingIcons.TrophyCup
      CHAMPION -> BoxingIcons.Crown
      UNDISPUTED -> BoxingIcons.BoxingGlove
    }
  }
}

data class RankedProfile(
  val rp: Int = 1250,
  val wins: Int = 14,
  val losses: Int = 3,
  val kos: Int = 9,
  val winStreak: Int = 4,
  val coins: Int = 3200,
  val rankTier: RankTier = RankTier.SILVER
) {
  companion object {
    fun calculateTier(rp: Int): RankTier {
      return when {
        rp >= RankTier.UNDISPUTED.minRp -> RankTier.UNDISPUTED
        rp >= RankTier.CHAMPION.minRp -> RankTier.CHAMPION
        rp >= RankTier.DIAMOND.minRp -> RankTier.DIAMOND
        rp >= RankTier.PLATINUM.minRp -> RankTier.PLATINUM
        rp >= RankTier.GOLD.minRp -> RankTier.GOLD
        rp >= RankTier.SILVER.minRp -> RankTier.SILVER
        else -> RankTier.BRONZE
      }
    }
  }
}

data class DailyRewardItem(
  val day: Int,
  val rewardTitle: String,
  val coins: Int,
  val rpBonus: Int,
  val specialGearName: String? = null,
  val isClaimed: Boolean = false,
  val isAvailableToday: Boolean = false
)

data class ChatMessage(
  val id: String,
  val senderName: String,
  val senderRank: RankTier,
  val message: String,
  val timeAgo: String,
  val isTaunt: Boolean = false,
  val isPlayer: Boolean = false
)

data class LeaderboardEntry(
  val rankNumber: Int,
  val name: String,
  val nickname: String,
  val country: String,
  val rp: Int,
  val tier: RankTier,
  val record: String,
  val koPercent: String
)

data class FightResult(
  val isVictory: Boolean,
  val outcomeText: String, // "KNOCKOUT", "TECHNICAL KNOCKOUT", "DECISION"
  val roundEnded: Int,
  val timeRemaining: String,
  val playerPunchesLanded: Int,
  val playerPunchesThrown: Int,
  val opponentPunchesLanded: Int,
  val opponentPunchesThrown: Int,
  val rpChange: Int,
  val coinsEarned: Int
)
