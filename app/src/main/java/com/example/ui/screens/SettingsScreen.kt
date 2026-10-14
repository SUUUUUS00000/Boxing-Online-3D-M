package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.icons.BoxingIcons
import com.example.viewmodel.GameViewModel

@Composable
fun SettingsScreen(
  viewModel: GameViewModel,
  modifier: Modifier = Modifier
) {
  var soundOn by remember { mutableStateOf(viewModel.soundEffects.soundEnabled) }
  var hapticsOn by remember { mutableStateOf(viewModel.soundEffects.hapticsEnabled) }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF090D16))
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {

    Text("PREFERENCES", color = Color(0xFF60A5FA), fontSize = 12.sp, fontWeight = FontWeight.Black)
    Text("Game Settings", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)

    Spacer(modifier = Modifier.height(16.dp))

    // AUDIO & FEEDBACK
    Text("AUDIO & HAPTICS", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Sound Effects", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Punches, blocks, referee count & ring bell", color = Color(0xFF94A3B8), fontSize = 11.sp)
          }
          Switch(
            checked = soundOn,
            onCheckedChange = {
              soundOn = it
              viewModel.soundEffects.soundEnabled = it
            }
          )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF1E293B))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Haptic Feedback", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Impact vibration on clean hits and knockdowns", color = Color(0xFF94A3B8), fontSize = 11.sp)
          }
          Switch(
            checked = hapticsOn,
            onCheckedChange = {
              hapticsOn = it
              viewModel.soundEffects.hapticsEnabled = it
            }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // HOW TO PLAY & CONTROLS GUIDE (WITH SVG VECTORS)
    Text("OB3D COMBAT GUIDE", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ControlGuideItem(
          icon = Icons.Default.SportsEsports,
          title = "Footwork & Virtual Joystick",
          desc = "Use the left analog stick to move forward, retreat, or circle around your opponent in the 3D ring."
        )
        ControlGuideItem(
          icon = Icons.Default.Shield,
          title = "High Guard & Body Block",
          desc = "Hold or toggle the Guard button. Reduces incoming strike damage by 80%. Toggle Body modifier to protect ribs."
        )
        ControlGuideItem(
          icon = BoxingIcons.Lightning,
          title = "Bob & Weave (Slips & Duck)",
          desc = "Tap SLIP L, SLIP R, or DUCK right as opponent throws a punch. Clean slips leave them wide open for a +50% Counter-Hit!"
        )
        ControlGuideItem(
          icon = BoxingIcons.BoxingGlove,
          title = "Punch Arsenal",
          desc = "JAB (fast disruptor), CROSS (devastating straight right), HOOKS (high arcing head/liver damage), UPPERCUT (lifts chin through guard)."
        )
        ControlGuideItem(
          icon = BoxingIcons.ClinchPush,
          title = "Organic Clinch & Push",
          desc = "When within close range (<1.6m), tap CLINCH to tie up your opponent, break their momentum, or shove them back against the ropes."
        )
        ControlGuideItem(
          icon = BoxingIcons.Flame,
          title = "Knockdown 10-Count & Ragdoll",
          desc = "Depleting Head or Body HP triggers a knockdown! If dropped, tap the screen rapidly to beat the referee's 10-count."
        )
        ControlGuideItem(
          icon = BoxingIcons.TrophyCup,
          title = "3-Knockdown Rule (TKO)",
          desc = "Scoring 3 knockdowns in a single round awards an instant Technical Knockout (TKO) victory!"
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // GAME INFO
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text("BOXING ONLINE 3D (OB3D)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
        Text("Mobile Edition • 60 FPS Low-Poly Physics Simulation", color = Color(0xFF94A3B8), fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Faithfully built in the style of the original OB3D with physics-based kinematics, organic clinch system, ranked multiplayer ladder, fighter locker, daily rewards, and live chat.", color = Color(0xFF64748B), fontSize = 11.sp)
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
fun ControlGuideItem(icon: ImageVector, title: String, desc: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Color(0xFF38BDF8),
      modifier = Modifier
        .size(18.dp)
        .padding(top = 2.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Column {
      Text(title, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
      Text(desc, color = Color(0xFFCBD5E1), fontSize = 11.sp)
    }
  }
}
