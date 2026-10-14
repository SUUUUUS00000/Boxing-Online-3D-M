package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine3d.MeshBuilder
import com.example.engine3d.Polygon3D
import com.example.engine3d.Renderer3D
import com.example.engine3d.Vec3
import com.example.model.ActionState
import com.example.model.PunchType
import com.example.ui.icons.BoxingIcons
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun FightScreen(
  viewModel: GameViewModel,
  modifier: Modifier = Modifier
) {
  val player = viewModel.playerFighter
  val opponent = viewModel.opponentFighter
  val physics = viewModel.physicsEngine
  val fightResult by viewModel.fightResult.collectAsState()
  val isBodyActive by viewModel.isBodyModifierActive.collectAsState()
  val isGuardActive by viewModel.isGuardActive.collectAsState()

  // Frame ticker for 3D animation
  var frameCount by remember { mutableLongStateOf(0L) }
  LaunchedEffect(Unit) {
    while (true) {
      kotlinx.coroutines.delay(16)
      frameCount++
    }
  }

  val animTime = frameCount * 0.016f

  // Dynamic Camera position: Third person isometric cinematic perspective matching OB3D screenshot
  val midX = (player.posX + opponent.posX) * 0.5f
  val midZ = (player.posZ + opponent.posZ) * 0.5f
  val dist = sqrt((player.posX - opponent.posX) * (player.posX - opponent.posX) + (player.posZ - opponent.posZ) * (player.posZ - opponent.posZ))

  val shake = physics.cameraShakeIntensity
  val shakeX = if (shake > 0f) (sin(animTime * 40f) * shake * 0.25f) else 0f
  val shakeY = if (shake > 0f) (cos(animTime * 35f) * shake * 0.25f) else 0f

  val camDist = 6.8f + (dist * 0.45f).coerceIn(0f, 3.5f)
  val cameraEye = Vec3(
    midX - 4.5f + shakeX,
    4.6f + shakeY,
    midZ - camDist
  )
  val cameraTarget = Vec3(midX, 1.4f, midZ)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF030712)) // Dark arena background as in screenshot
  ) {

    // --- 3D ARENA & FIGHTERS RENDER CANVAS ---
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .testTag("fight_3d_canvas")
    ) {
      // Rebuild dynamic polygons
      val scenePolys = ArrayList<Polygon3D>(450)
      scenePolys.addAll(MeshBuilder.buildRing())
      scenePolys.addAll(MeshBuilder.buildBoxer(player, animTime))
      scenePolys.addAll(MeshBuilder.buildBoxer(opponent, animTime))
      scenePolys.addAll(MeshBuilder.buildSparks(physics.activeSparks))

      Renderer3D.renderScene(
        drawScope = this,
        polygons = scenePolys,
        cameraEye = cameraEye,
        cameraTarget = cameraTarget,
        fovDegrees = 50f
      )
    }

    // --- AUTHENTIC OB3D TOP HUD ---
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp, start = 12.dp, end = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      // Row: 0 KNOCKDOWNS 0 (Identical to screenshot!)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Player Knockdowns Count
        Text(
          text = "${player.totalKnockdowns}",
          color = Color.White,
          fontSize = 24.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(start = 24.dp)
        )

        // Center Title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "KNOCKDOWNS",
            color = Color(0xFF93C5FD),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
          )
          Text(
            text = "ROUND ${physics.roundNumber}/${physics.totalRounds} • ${String.format("%02d:%02d", physics.roundTimeRemaining.toInt() / 60, physics.roundTimeRemaining.toInt() % 60)}",
            color = Color(0xFFFACC15),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // Opponent Knockdowns Count
        Text(
          text = "${opponent.totalKnockdowns}",
          color = Color.White,
          fontSize = 24.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(end = 24.dp)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // HEAD, BODY, STAMINA indicators (matching screenshot text "HEAD BODY STAMINA")
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Player Vitals (Left)
        FighterVitalsHud(
          name = player.customization.name,
          headHp = player.headHealth,
          bodyHp = player.bodyHealth,
          stamina = player.stamina,
          isPlayer = true
        )

        // Opponent Vitals (Right)
        FighterVitalsHud(
          name = opponent.customization.name,
          headHp = opponent.headHealth,
          bodyHp = opponent.bodyHealth,
          stamina = opponent.stamina,
          isPlayer = false
        )
      }
    }

    // --- REFEREE 10-COUNT OVERLAY ---
    if (physics.activeKnockedDownFighter != null) {
      val downFighter = physics.activeKnockedDownFighter!!
      val isPlayerDown = downFighter == player

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
          modifier = Modifier
            .padding(24.dp)
            .shadow(16.dp)
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "KNOCKDOWN!",
              color = Color(0xFFEF4444),
              fontSize = 26.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "${physics.refereeCount}",
              color = Color.White,
              fontSize = 64.sp,
              fontWeight = FontWeight.Black
            )

            if (isPlayerDown) {
              Text(
                text = "TAP SCREEN RAPIDLY TO GET UP!",
                color = Color(0xFFFBBF24),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(14.dp))

              LinearProgressIndicator(
                progress = { player.getUpTaps.toFloat() / player.requiredTaps.toFloat() },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(14.dp)
                  .clip(RoundedCornerShape(7.dp)),
                color = Color(0xFF10B981),
                trackColor = Color(0xFF334155)
              )

              Spacer(modifier = Modifier.height(14.dp))

              Button(
                onClick = { viewModel.onGetUpTap() },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(52.dp)
                  .testTag("get_up_tap_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
              ) {
                Text("GET UP! (${player.getUpTaps}/${player.requiredTaps})", fontWeight = FontWeight.Black)
              }
            } else {
              Text(
                text = "REFEREE COUNTING OPPONENT...",
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
              )
            }
          }
        }
      }
    }

    // --- CONTROLS HUD (TOUCH FRIENDLY & CONVENIENT) ---
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 20.dp),
      contentAlignment = Alignment.BottomCenter
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {

        // LEFT SIDE: Movement Virtual Pad + Slips + Guard
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Bob & Weave / Slip & Clinch Buttons Row
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SmallCombatButton("SLIP L", Color(0xFF6366F1)) {
              viewModel.onSlipTriggered(ActionState.SLIP_LEFT)
            }
            SmallCombatButton("DUCK", Color(0xFF4F46E5)) {
              viewModel.onSlipTriggered(ActionState.DUCK)
            }
            SmallCombatButton("SLIP R", Color(0xFF6366F1)) {
              viewModel.onSlipTriggered(ActionState.SLIP_RIGHT)
            }
            Button(
              onClick = { viewModel.onClinchOrPushTriggered() },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
              modifier = Modifier
                .height(34.dp)
                .testTag("clinch_push_button")
            ) {
              Icon(BoxingIcons.ClinchPush, contentDescription = "Clinch", modifier = Modifier.size(14.dp), tint = Color.White)
              Spacer(modifier = Modifier.width(3.dp))
              Text("CLINCH", fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
          }

          // Guard Button (Hold or Toggle)
          Button(
            onClick = { viewModel.setGuard(!isGuardActive) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isGuardActive) Color(0xFF059669) else Color(0xFF1E293B)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .width(170.dp)
              .height(44.dp)
              .testTag("guard_toggle_button")
          ) {
            Icon(
              if (isGuardActive) Icons.Default.Shield else Icons.Default.Security,
              contentDescription = "Guard",
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              if (isGuardActive) "GUARD: RAISED" else "GUARD: LOWERED",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Virtual Movement Joystick
          VirtualJoystick(
            onMove = { x, z ->
              viewModel.moveStickX = x
              viewModel.moveStickZ = z
            }
          )
        }

        // RIGHT SIDE: Punch Action Cluster & Body Modifier
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

          // Quick Taunt / Callout & Body Toggle
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = {
                val taunts = listOf("Nice hook!", "Too slow!", "Let's box!", "OB3D champion!")
                viewModel.sendChatMessage(taunts.random(), isTaunt = true)
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.height(38.dp)
            ) {
              Icon(Icons.Default.ChatBubble, contentDescription = "Taunt", modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("TAUNT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Body target modifier toggle
            FilterChip(
              selected = isBodyActive,
              onClick = { viewModel.toggleBodyModifier() },
              label = {
                Text(
                  if (isBodyActive) "TARGET: BODY" else "TARGET: HEAD",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Black
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFDC2626),
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("body_modifier_chip")
            )
          }

          // Punch buttons grid
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PunchActionButton("JAB", "L-Straight", Color(0xFF3B82F6)) {
              viewModel.onPunchTriggered(PunchType.JAB)
            }
            PunchActionButton("CROSS", "R-Power", Color(0xFFEF4444)) {
              viewModel.onPunchTriggered(PunchType.CROSS)
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PunchActionButton("L-HOOK", "Left Hook", Color(0xFF8B5CF6)) {
              viewModel.onPunchTriggered(PunchType.L_HOOK)
            }
            PunchActionButton("R-HOOK", "Right Hook", Color(0xFFEC4899)) {
              viewModel.onPunchTriggered(PunchType.R_HOOK)
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PunchActionButton("UPPERCUT", "Chin Lift", Color(0xFFF59E0B), isWide = true) {
              viewModel.onPunchTriggered(PunchType.R_UPPERCUT)
            }
          }
        }
      }
    }

    // --- MATCH FINISHED RESULTS DIALOG ---
    fightResult?.let { res ->
      FightResultModal(
        result = res,
        onRematch = { viewModel.startNewFight() },
        onRankedLobby = { viewModel.navigateTo(AppScreen.RANKED) }
      )
    }
  }
}

@Composable
fun FighterVitalsHud(
  name: String,
  headHp: Float,
  bodyHp: Float,
  stamina: Float,
  isPlayer: Boolean
) {
  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xCC0F172A)),
    modifier = Modifier.width(160.dp)
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Text(
        text = name,
        color = if (isPlayer) Color(0xFF60A5FA) else Color(0xFFF87171),
        fontSize = 11.sp,
        fontWeight = FontWeight.Black
      )

      Spacer(modifier = Modifier.height(4.dp))

      // HEAD
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("HEAD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE2E8F0))
        Text("${headHp.toInt()}%", fontSize = 9.sp, color = Color.White)
      }
      LinearProgressIndicator(
        progress = { (headHp / 100f).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = Color(0xFFEF4444),
        trackColor = Color(0xFF334155)
      )

      Spacer(modifier = Modifier.height(3.dp))

      // BODY
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("BODY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE2E8F0))
        Text("${bodyHp.toInt()}%", fontSize = 9.sp, color = Color.White)
      }
      LinearProgressIndicator(
        progress = { (bodyHp / 100f).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = Color(0xFFF97316),
        trackColor = Color(0xFF334155)
      )

      Spacer(modifier = Modifier.height(3.dp))

      // STAMINA
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("STAMINA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE2E8F0))
        Text("${stamina.toInt()}%", fontSize = 9.sp, color = Color.White)
      }
      LinearProgressIndicator(
        progress = { (stamina / 100f).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = Color(0xFF10B981),
        trackColor = Color(0xFF334155)
      )
    }
  }
}

@Composable
fun VirtualJoystick(
  onMove: (Float, Float) -> Unit
) {
  var thumbOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
  val maxRadius = 45f

  Box(
    modifier = Modifier
      .size(110.dp)
      .clip(CircleShape)
      .background(Color(0x661E293B))
      .border(1.5.dp, Color(0xFF475569), CircleShape)
      .pointerInput(Unit) {
        detectDragGestures(
          onDrag = { change, dragAmount ->
            change.consume()
            val newOffset = thumbOffset + dragAmount
            val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
            thumbOffset = if (dist > maxRadius) {
              androidx.compose.ui.geometry.Offset(
                (newOffset.x / dist) * maxRadius,
                (newOffset.y / dist) * maxRadius
              )
            } else {
              newOffset
            }
            onMove(thumbOffset.x / maxRadius, -thumbOffset.y / maxRadius)
          },
          onDragEnd = {
            thumbOffset = androidx.compose.ui.geometry.Offset.Zero
            onMove(0f, 0f)
          },
          onDragCancel = {
            thumbOffset = androidx.compose.ui.geometry.Offset.Zero
            onMove(0f, 0f)
          }
        )
      },
    contentAlignment = Alignment.Center
  ) {
    // Joystick Handle
    Box(
      modifier = Modifier
        .offset(
          x = (thumbOffset.x).dp,
          y = (thumbOffset.y).dp
        )
        .size(46.dp)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            listOf(Color(0xFF60A5FA), Color(0xFF1D4ED8))
          )
        )
        .border(1.dp, Color.White, CircleShape)
    )
  }
}

@Composable
fun PunchActionButton(
  title: String,
  subtitle: String,
  color: Color,
  isWide: Boolean = false,
  onClick: () -> Unit
) {
  Button(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(containerColor = color),
    modifier = Modifier
      .size(width = if (isWide) 168.dp else 80.dp, height = 48.dp)
      .shadow(4.dp, RoundedCornerShape(12.dp))
      .testTag("punch_${title.lowercase()}")
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(title, fontWeight = FontWeight.Black, fontSize = 13.sp)
      Text(subtitle, fontSize = 9.sp, color = Color(0xCCFFFFFF))
    }
  }
}

@Composable
fun SmallCombatButton(
  text: String,
  color: Color,
  onClick: () -> Unit
) {
  Button(
    onClick = onClick,
    shape = RoundedCornerShape(8.dp),
    colors = ButtonDefaults.buttonColors(containerColor = color),
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    modifier = Modifier.height(34.dp)
  ) {
    Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
  }
}

@Composable
fun FightResultModal(
  result: com.example.model.FightResult,
  onRematch: () -> Unit,
  onRankedLobby: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xB3000000)),
    contentAlignment = Alignment.Center
  ) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
      modifier = Modifier
        .padding(24.dp)
        .fillMaxWidth()
        .shadow(24.dp)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = if (result.isVictory) "VICTORY!" else "DEFEAT",
          color = if (result.isVictory) Color(0xFF10B981) else Color(0xFFEF4444),
          fontSize = 32.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 2.sp
        )

        Text(
          text = result.outcomeText,
          color = Color(0xFFFBBF24),
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Stats card
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            ResultStatRow("Round Ended", "${result.roundEnded} (${result.timeRemaining})")
            ResultStatRow("Punches Landed", "${result.playerPunchesLanded} / ${result.playerPunchesThrown}")
            ResultStatRow("Opponent Punches", "${result.opponentPunchesLanded} / ${result.opponentPunchesThrown}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF334155))
            ResultStatRow(
              "Ranked RP",
              if (result.rpChange >= 0) "+${result.rpChange} RP" else "${result.rpChange} RP",
              valColor = if (result.rpChange >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
            )
            ResultStatRow("Coins Won", "+${result.coinsEarned} Coins", valColor = Color(0xFFF59E0B))
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onRankedLobby,
            modifier = Modifier
              .weight(1f)
              .height(48.dp),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("LOBBY", fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = onRematch,
            modifier = Modifier
              .weight(1f)
              .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("REMATCH", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun ResultStatRow(label: String, value: String, valColor: Color = Color.White) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, color = Color(0xFF94A3B8), fontSize = 13.sp)
    Text(value, color = valColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
  }
}
