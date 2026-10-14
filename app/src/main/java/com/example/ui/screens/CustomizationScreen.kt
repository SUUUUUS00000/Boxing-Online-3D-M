package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine3d.MeshBuilder
import com.example.engine3d.Renderer3D
import com.example.engine3d.Vec3
import com.example.model.CombatFighter
import com.example.model.FighterCustomization
import com.example.model.Stance
import com.example.viewmodel.GameViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CustomizationScreen(
  viewModel: GameViewModel,
  modifier: Modifier = Modifier
) {
  val custom by viewModel.playerCustom.collectAsState()
  val rankedProfile by viewModel.rankedProfile.collectAsState()

  // 3D Preview rotation state
  var orbitAngle by remember { mutableFloatStateOf(180f) }

  // Editable local state
  var nameText by remember(custom.name) { mutableStateOf(custom.name) }
  var nicknameText by remember(custom.nickname) { mutableStateOf(custom.nickname) }
  var countryText by remember(custom.country) { mutableStateOf(custom.country) }

  // Preview Fighter instance
  val previewFighter = remember(custom, orbitAngle) {
    CombatFighter(
      customization = custom,
      posX = 0f,
      posZ = 0f,
      angleY = orbitAngle
    )
  }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF090D16))
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {

    // Top Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("LOCKER ROOM", color = Color(0xFF60A5FA), fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text("Fighter Customization", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
      }
      Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF1E293B)) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = com.example.ui.icons.BoxingIcons.Coin,
            contentDescription = "Coins",
            tint = Color(0xFFFBBF24),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("${rankedProfile.coins}", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 3D Interactive Fighter Preview (Drag horizontally to rotate 360 degrees!)
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
      modifier = Modifier
        .fillMaxWidth()
        .height(280.dp)
        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
        .pointerInput(Unit) {
          detectDragGestures { change, dragAmount ->
            change.consume()
            orbitAngle += dragAmount.x * 0.7f
          }
        }
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val camDist = 4.8f
          val eye = Vec3(
            -sin(Math.toRadians(0.0)).toFloat() * camDist,
            2.2f,
            -cos(Math.toRadians(0.0)).toFloat() * camDist
          )
          val target = Vec3(0f, 1.4f, 0f)

          val polys = MeshBuilder.buildBoxer(previewFighter, timeSeconds = 0f)
          Renderer3D.renderScene(
            drawScope = this,
            polygons = polys,
            cameraEye = eye,
            cameraTarget = target,
            fovDegrees = 45f
          )
        }

        // Overlay hint
        Row(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(10.dp)
            .background(Color(0x99000000), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Drag to rotate 360°", color = Color(0xFF94A3B8), fontSize = 11.sp)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // SECTION 1: Identity & Stance
    Text("IDENTITY & STANCE", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedTextField(
        value = nameText,
        onValueChange = {
          nameText = it
          viewModel.updateCustomization(custom.copy(name = it))
        },
        label = { Text("Name") },
        modifier = Modifier.weight(1.2f),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        )
      )
      OutlinedTextField(
        value = nicknameText,
        onValueChange = {
          nicknameText = it
          viewModel.updateCustomization(custom.copy(nickname = it))
        },
        label = { Text("Nickname") },
        modifier = Modifier.weight(1.5f),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        )
      )
      OutlinedTextField(
        value = countryText,
        onValueChange = {
          countryText = it.take(2).uppercase()
          viewModel.updateCustomization(custom.copy(country = countryText))
        },
        label = { Text("Flag") },
        modifier = Modifier.weight(0.7f),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        )
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Stance selector
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = { viewModel.updateCustomization(custom.copy(stance = Stance.ORTHODOX)) },
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (custom.stance == Stance.ORTHODOX) Color(0xFF2563EB) else Color(0xFF1E293B)
        ),
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("ORTHODOX (Right)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }

      Button(
        onClick = { viewModel.updateCustomization(custom.copy(stance = Stance.SOUTHPAW)) },
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (custom.stance == Stance.SOUTHPAW) Color(0xFF2563EB) else Color(0xFF1E293B)
        ),
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("SOUTHPAW (Left)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION 2: GEAR & COLORS
    Text("GEAR & AESTHETICS", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    // Skin Tone
    ColorPickerRow(
      title = "Skin / Body Material",
      colors = FighterCustomization.SkinTones,
      selectedIndex = custom.skinToneIndex,
      onSelect = { viewModel.updateCustomization(custom.copy(skinToneIndex = it)) }
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Boxing Gloves
    ColorPickerRow(
      title = "Boxing Gloves Color",
      colors = FighterCustomization.GearColors,
      selectedIndex = custom.gloveColorIndex,
      onSelect = { viewModel.updateCustomization(custom.copy(gloveColorIndex = it)) }
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Boxing Shorts / Trunks
    ColorPickerRow(
      title = "Boxing Trunks Color",
      colors = FighterCustomization.GearColors,
      selectedIndex = custom.shortsColorIndex,
      onSelect = { viewModel.updateCustomization(custom.copy(shortsColorIndex = it)) }
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Boxing Boots
    ColorPickerRow(
      title = "Boxing Boots Color",
      colors = FighterCustomization.GearColors,
      selectedIndex = custom.shoesColorIndex,
      onSelect = { viewModel.updateCustomization(custom.copy(shoesColorIndex = it)) }
    )

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION 3: PHYSIQUE SCALING
    Text("PHYSIQUE & REACH (Tactical Build)", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    ScaleSlider("Height & Leverage", custom.heightScale, 0.9f..1.15f) {
      viewModel.updateCustomization(custom.copy(heightScale = it))
    }

    ScaleSlider("Arm Reach (Punch Range)", custom.reachScale, 0.9f..1.15f) {
      viewModel.updateCustomization(custom.copy(reachScale = it))
    }

    ScaleSlider("Muscle Mass", custom.muscleScale, 0.85f..1.2f) {
      viewModel.updateCustomization(custom.copy(muscleScale = it))
    }

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION 4: STAT ALLOCATION & TRAINING
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("TRAINING & STAT UPGRADES", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = com.example.ui.icons.BoxingIcons.Coin,
          contentDescription = null,
          tint = Color(0xFFFBBF24),
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("500 Coins", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    StatUpgradeRow("POWER", "Punch damage & KO threshold", custom.statPower, Color(0xFFEF4444)) {
      viewModel.upgradeStat("POWER")
    }

    Spacer(modifier = Modifier.height(8.dp))

    StatUpgradeRow("SPEED", "Hand velocity & combo cadence", custom.statSpeed, Color(0xFF3B82F6)) {
      viewModel.upgradeStat("SPEED")
    }

    Spacer(modifier = Modifier.height(8.dp))

    StatUpgradeRow("STAMINA", "Work rate & recovery speed", custom.statStamina, Color(0xFF10B981)) {
      viewModel.upgradeStat("STAMINA")
    }

    Spacer(modifier = Modifier.height(8.dp))

    StatUpgradeRow("CHIN", "Guard durability & head resistance", custom.statChin, Color(0xFF8B5CF6)) {
      viewModel.upgradeStat("CHIN")
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
fun ColorPickerRow(
  title: String,
  colors: List<Color>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit
) {
  Column {
    Text(title, color = Color(0xFFE2E8F0), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    Spacer(modifier = Modifier.height(6.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      itemsIndexed(colors) { index, col ->
        val isSelected = index == selectedIndex
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(col)
            .border(
              width = if (isSelected) 3.dp else 1.dp,
              color = if (isSelected) Color.White else Color(0xFF475569),
              shape = CircleShape
            )
            .clickable { onSelect(index) },
          contentAlignment = Alignment.Center
        ) {
          if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}

@Composable
fun ScaleSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(label, color = Color(0xFFE2E8F0), fontSize = 12.sp)
      Text("${String.format("%.2f", value)}x", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
    Slider(
      value = value,
      onValueChange = onChange,
      valueRange = range,
      colors = SliderDefaults.colors(
        thumbColor = Color(0xFF2563EB),
        activeTrackColor = Color(0xFF3B82F6),
        inactiveTrackColor = Color(0xFF1E293B)
      )
    )
  }
}

@Composable
fun StatUpgradeRow(
  statName: String,
  desc: String,
  value: Int,
  color: Color,
  onUpgrade: () -> Unit
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
          Text(statName, color = color, fontWeight = FontWeight.Black, fontSize = 14.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Text("$value / 10", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Text(desc, color = Color(0xFF94A3B8), fontSize = 10.sp)
      }

      Button(
        onClick = onUpgrade,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("upgrade_${statName.lowercase()}")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("+1", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    }
  }
}
