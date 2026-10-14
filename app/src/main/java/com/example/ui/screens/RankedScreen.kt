package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RankTier
import com.example.ui.icons.BoxingIcons
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.MatchmakingStatus

@Composable
fun RankedScreen(
  viewModel: GameViewModel,
  modifier: Modifier = Modifier
) {
  val rankedProfile by viewModel.rankedProfile.collectAsState()
  val custom by viewModel.playerCustom.collectAsState()
  val matchmakingStatus by viewModel.matchmakingStatus.collectAsState()
  val searchSeconds by viewModel.searchTimeSeconds.collectAsState()
  val matchedOpponent by viewModel.matchedOpponent.collectAsState()
  val leaderboard by viewModel.leaderboard.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF0B0F19))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {

      // Header Banner
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "RANKED SEASON 4",
            color = Color(0xFFF59E0B),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
          )
          Text(
            text = "Competitive Ladder",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
          )
        }

        // Coins balance pill with SVG coin icon
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF1E293B)
        ) {
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
            Text(
              text = "${rankedProfile.coins}",
              color = Color(0xFFFBBF24),
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Current Rank Tier Card
      val tierColor = Color(rankedProfile.rankTier.colorHex)
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.5.dp, tierColor.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(tierColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = rankedProfile.rankTier.getIcon(),
                  contentDescription = rankedProfile.rankTier.displayName,
                  tint = tierColor,
                  modifier = Modifier.size(26.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "${rankedProfile.rankTier.displayName.uppercase()} DIVISION",
                  color = tierColor,
                  fontWeight = FontWeight.Black,
                  fontSize = 16.sp
                )
                Text(
                  text = "${custom.name} \"${custom.nickname}\" (${custom.country})",
                  color = Color(0xFF94A3B8),
                  fontSize = 12.sp
                )
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "${rankedProfile.rp} RP",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = BoxingIcons.Flame,
                  contentDescription = "Streak",
                  tint = Color(0xFFF97316),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "Streak: ${rankedProfile.winStreak}",
                  color = Color(0xFFF97316),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Progress to Next Rank
          val nextTier = RankTier.values().getOrNull(rankedProfile.rankTier.ordinal + 1)
          val progress = if (nextTier != null) {
            val curBase = rankedProfile.rankTier.minRp
            val target = nextTier.minRp
            ((rankedProfile.rp - curBase).toFloat() / (target - curBase).toFloat()).coerceIn(0f, 1f)
          } else 1.0f

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Promotion Progress", fontSize = 11.sp, color = Color(0xFF94A3B8))
            Text(
              if (nextTier != null) "${(progress * 100).toInt()}% to ${nextTier.displayName}" else "MAX TIER REACHED",
              fontSize = 11.sp,
              color = Color(0xFF38BDF8),
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = tierColor,
            trackColor = Color(0xFF1E293B)
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            RankStatPill("RECORD", "${rankedProfile.wins}W - ${rankedProfile.losses}L")
            RankStatPill("KNOCKOUTS", "${rankedProfile.kos} KOs")
            val totalFights = (rankedProfile.wins + rankedProfile.losses).coerceAtLeast(1)
            val winRate = (rankedProfile.wins * 100) / totalFights
            RankStatPill("WIN RATE", "$winRate%")
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Matchmaking Button
      Button(
        onClick = { viewModel.startRankedMatchmaking() },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("find_ranked_match_button"),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Icon(Icons.Default.SportsMma, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "FIND RANKED MATCH",
          fontSize = 16.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 1.sp
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color(0xFF131B2E),
        contentColor = Color.White
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("GLOBAL LEADERBOARD", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("RANK DIVISIONS", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      if (selectedTab == 0) {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(leaderboard) { entry ->
            val isUser = entry.name.contains("YOU")
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color(0xFF1E3A8A) else Color(0xFF131B2E)
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "#${entry.rankNumber}",
                    color = if (entry.rankNumber <= 3) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    modifier = Modifier.width(36.dp)
                  )
                  Icon(
                    imageVector = entry.tier.getIcon(),
                    contentDescription = null,
                    tint = Color(entry.tier.colorHex),
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = entry.name,
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp
                    )
                    Text(
                      text = "${entry.record} • KO: ${entry.koPercent}",
                      color = Color(0xFF94A3B8),
                      fontSize = 11.sp
                    )
                  }
                }

                Text(
                  text = "${entry.rp} RP",
                  color = Color(entry.tier.colorHex),
                  fontWeight = FontWeight.Black,
                  fontSize = 14.sp
                )
              }
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(RankTier.values().toList().reversed()) { tier ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = tier.getIcon(),
                    contentDescription = tier.displayName,
                    tint = Color(tier.colorHex),
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      tier.displayName,
                      color = Color(tier.colorHex),
                      fontWeight = FontWeight.Black,
                      fontSize = 15.sp
                    )
                    Text(
                      "${tier.minRp}+ Rating Points",
                      color = Color(0xFF94A3B8),
                      fontSize = 12.sp
                    )
                  }
                }

                if (tier == rankedProfile.rankTier) {
                  Surface(
                    color = Color(tier.colorHex).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text(
                      "CURRENT",
                      color = Color(tier.colorHex),
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Black,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Matchmaking Popup
    if (matchmakingStatus != MatchmakingStatus.IDLE) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xD9000000)),
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
          modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            val infiniteTransition = rememberInfiniteTransition()
            val rotation by infiniteTransition.animateFloat(
              initialValue = 0f,
              targetValue = 360f,
              animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing))
            )

            Box(
              modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
                .border(2.dp, Color(0xFF38BDF8), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Radar,
                contentDescription = null,
                modifier = Modifier
                  .size(48.dp)
                  .rotate(rotation),
                tint = Color(0xFF38BDF8)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (matchmakingStatus == MatchmakingStatus.SEARCHING) {
              Text(
                text = "SEARCHING FOR OPPONENT...",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp
              )
              Text(
                text = "Division: ${rankedProfile.rankTier.displayName} • Elapsed: ${searchSeconds}s",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
              )

              Spacer(modifier = Modifier.height(20.dp))

              OutlinedButton(
                onClick = { viewModel.cancelMatchmaking() },
                shape = RoundedCornerShape(12.dp)
              ) {
                Text("CANCEL SEARCH")
              }
            } else {
              Text(
                text = "OPPONENT FOUND!",
                color = Color(0xFF10B981),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
              )
              matchedOpponent?.let { opp ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "${opp.name} \"${opp.nickname}\"",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
                Text(
                  text = "Country: ${opp.country} • Entering ring...",
                  color = Color(0xFFFBBF24),
                  fontSize = 12.sp
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun RankStatPill(label: String, value: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(label, color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
  }
}
