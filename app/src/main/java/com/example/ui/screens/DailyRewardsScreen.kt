package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyRewardItem
import com.example.ui.icons.BoxingIcons
import com.example.viewmodel.GameViewModel

@Composable
fun DailyRewardsScreen(
  viewModel: GameViewModel,
  modifier: Modifier = Modifier
) {
  val dailyRewards by viewModel.dailyRewards.collectAsState()
  val streakDay by viewModel.currentStreakDay.collectAsState()
  val canClaim by viewModel.canClaimDaily.collectAsState()
  val rankedProfile by viewModel.rankedProfile.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF090D16))
      .padding(16.dp)
  ) {

    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("LOGIN REWARDS", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text("Daily Training Purse", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
      }

      Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF1E293B)) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = BoxingIcons.Coin,
            contentDescription = "Coins",
            tint = Color(0xFFFBBF24),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("${rankedProfile.coins}", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Streak Status Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(Color(0xFF10B981).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = BoxingIcons.Flame,
              contentDescription = "Streak",
              tint = Color(0xFFF97316),
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text("DAY $streakDay OF 7", color = Color(0xFF10B981), fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text("Login each day to unlock legendary gear", color = Color(0xFF94A3B8), fontSize = 11.sp)
          }
        }

        if (canClaim) {
          Button(
            onClick = { viewModel.claimDailyReward(streakDay) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("claim_daily_button")
          ) {
            Text("CLAIM", fontWeight = FontWeight.Black)
          }
        } else {
          Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              "CLAIMED",
              color = Color(0xFF94A3B8),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text("7-DAY CALENDAR", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    // 7-day Rewards Grid
    LazyVerticalGrid(
      columns = GridCells.Fixed(3),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(260.dp)
    ) {
      items(dailyRewards) { item ->
        DailyRewardCard(item = item, isToday = item.day == streakDay)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text("DAILY TRAINING MISSIONS", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      item {
        DailyMissionRow("Heavy Artillery", "Land 25 power punches in Ranked", 18, 25, 400)
      }
      item {
        DailyMissionRow("Untouchable", "Perform 5 successful slips or ducks", 5, 5, 300, isDone = true)
      }
      item {
        DailyMissionRow("Canvas Master", "Win 1 match by Knockout", 1, 1, 600, isDone = true)
      }
      item {
        DailyMissionRow("Ring General", "Win 2 consecutive Ranked matches", 1, 2, 800)
      }
    }
  }
}

@Composable
fun DailyRewardCard(item: DailyRewardItem, isToday: Boolean) {
  val borderCol = when {
    item.isClaimed -> Color(0xFF334155)
    isToday -> Color(0xFF10B981)
    else -> Color(0xFF1E293B)
  }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isToday) Color(0xFF1E3A8A).copy(alpha = 0.5f) else Color(0xFF131B2E)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.5.dp, borderCol, RoundedCornerShape(12.dp))
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("DAY ${item.day}", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        if (item.isClaimed) {
          Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
        } else if (!isToday && item.day > 2) {
          Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(12.dp))
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Icon(
        imageVector = if (item.specialGearName != null) BoxingIcons.BoxingGlove else BoxingIcons.Coin,
        contentDescription = null,
        tint = if (item.specialGearName != null) Color(0xFFEF4444) else Color(0xFFFBBF24),
        modifier = Modifier.size(24.dp)
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = item.rewardTitle,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        maxLines = 1
      )

      Text(
        text = "+${item.coins} Coins",
        color = Color(0xFFFBBF24),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

@Composable
fun DailyMissionRow(
  title: String,
  desc: String,
  progress: Int,
  total: Int,
  rewardCoins: Int,
  isDone: Boolean = false
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          if (isDone) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(color = Color(0xFF10B981).copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
              Text("DONE", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
          }
        }
        Text(desc, color = Color(0xFF94A3B8), fontSize = 10.sp)

        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
          progress = { (progress.toFloat() / total.toFloat()).coerceIn(0f, 1f) },
          modifier = Modifier
            .width(140.dp)
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = if (isDone) Color(0xFF10B981) else Color(0xFF38BDF8),
          trackColor = Color(0xFF1E293B)
        )
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = BoxingIcons.Coin,
            contentDescription = null,
            tint = Color(0xFFFBBF24),
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            "+$rewardCoins",
            color = Color(0xFFFBBF24),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }
      }
    }
  }
}
