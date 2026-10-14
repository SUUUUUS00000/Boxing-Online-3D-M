package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true, dynamicColor = false) {
        val gameViewModel: GameViewModel = viewModel()
        BoxingApp(viewModel = gameViewModel)
      }
    }
  }
}

@Composable
fun BoxingApp(viewModel: GameViewModel) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val canClaimReward by viewModel.canClaimDaily.collectAsState()

  // Handle hardware back button to return to fight screen
  BackHandler(enabled = currentScreen != AppScreen.FIGHT) {
    viewModel.navigateTo(AppScreen.FIGHT)
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = Color(0xFF030712),
    bottomBar = {
      NavigationBar(
        containerColor = Color(0xFF0B0F19),
        contentColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
      ) {
        NavigationItem(
          label = "Fight",
          icon = com.example.ui.icons.BoxingIcons.BoxingGlove,
          isSelected = currentScreen == AppScreen.FIGHT,
          onClick = { viewModel.navigateTo(AppScreen.FIGHT) }
        )
        NavigationItem(
          label = "Ranked",
          icon = com.example.ui.icons.BoxingIcons.TrophyCup,
          isSelected = currentScreen == AppScreen.RANKED,
          onClick = { viewModel.navigateTo(AppScreen.RANKED) }
        )
        NavigationItem(
          label = "Locker",
          icon = Icons.Default.Checkroom,
          isSelected = currentScreen == AppScreen.CUSTOMIZATION,
          onClick = { viewModel.navigateTo(AppScreen.CUSTOMIZATION) }
        )
        NavigationItem(
          label = "Rewards",
          icon = Icons.Default.CardGiftcard,
          isSelected = currentScreen == AppScreen.DAILY_REWARDS,
          hasBadge = canClaimReward,
          onClick = { viewModel.navigateTo(AppScreen.DAILY_REWARDS) }
        )
        NavigationItem(
          label = "Chat",
          icon = Icons.Default.ChatBubble,
          isSelected = currentScreen == AppScreen.CHAT,
          onClick = { viewModel.navigateTo(AppScreen.CHAT) }
        )
        NavigationItem(
          label = "Settings",
          icon = Icons.Default.Settings,
          isSelected = currentScreen == AppScreen.SETTINGS,
          onClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentScreen) {
        AppScreen.FIGHT -> FightScreen(viewModel = viewModel)
        AppScreen.RANKED -> RankedScreen(viewModel = viewModel)
        AppScreen.CUSTOMIZATION -> CustomizationScreen(viewModel = viewModel)
        AppScreen.DAILY_REWARDS -> DailyRewardsScreen(viewModel = viewModel)
        AppScreen.CHAT -> ChatScreen(viewModel = viewModel)
        AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun RowScope.NavigationItem(
  label: String,
  icon: ImageVector,
  isSelected: Boolean,
  hasBadge: Boolean = false,
  onClick: () -> Unit
) {
  NavigationBarItem(
    selected = isSelected,
    onClick = onClick,
    icon = {
      BadgedBox(badge = {
        if (hasBadge) {
          Badge(
            containerColor = Color(0xFFEF4444),
            modifier = Modifier.size(8.dp)
          )
        }
      }) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          modifier = Modifier.size(22.dp)
        )
      }
    },
    label = {
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
      )
    },
    colors = NavigationBarItemDefaults.colors(
      selectedIconColor = Color(0xFF38BDF8),
      selectedTextColor = Color(0xFF38BDF8),
      unselectedIconColor = Color(0xFF64748B),
      unselectedTextColor = Color(0xFF64748B),
      indicatorColor = Color(0xFF1E293B)
    ),
    modifier = Modifier.testTag("nav_item_${label.lowercase()}")
  )
}
