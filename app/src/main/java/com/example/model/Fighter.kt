package com.example.model

import androidx.compose.ui.graphics.Color

enum class Stance {
  ORTHODOX,
  SOUTHPAW
}

enum class PunchType(
  val displayName: String,
  val staminaCost: Float,
  val baseDamage: Float,
  val speed: Float,
  val isBody: Boolean,
  val isHook: Boolean,
  val isUppercut: Boolean,
  val isLeftHand: Boolean
) {
  JAB("Jab", 6f, 10f, 1.25f, false, false, false, true),
  CROSS("Cross", 12f, 18f, 1.0f, false, false, false, false),
  L_HOOK("L-Hook", 14f, 19f, 0.95f, false, true, false, true),
  R_HOOK("R-Hook", 16f, 24f, 0.9f, false, true, false, false),
  L_UPPERCUT("L-Upper", 15f, 20f, 0.9f, false, false, true, true),
  R_UPPERCUT("R-Upper", 18f, 26f, 0.85f, false, false, true, false),
  BODY_JAB("Body Jab", 8f, 12f, 1.15f, true, false, false, true),
  BODY_CROSS("Body Cross", 14f, 20f, 0.95f, true, false, false, false),
  BODY_HOOK("Liver Hook", 16f, 22f, 0.9f, true, true, false, true)
}

enum class ActionState {
  IDLE,
  WALKING,
  SLIP_LEFT,
  SLIP_RIGHT,
  DUCK,
  LEAN_BACK,
  PUNCHING,
  BLOCKING,
  CLINCHING,
  PUSHED,
  HIT_STUN,
  KNOCKED_DOWN,
  RECOVERING,
  VICTORY
}

data class FighterCustomization(
  val id: String = "player_1",
  val name: String = "ALEXEI",
  val nickname: String = "THE IRON PHANTOM",
  val country: String = "RU",
  val stance: Stance = Stance.ORTHODOX,
  val skinToneIndex: Int = 0,
  val gloveColorIndex: Int = 1, // Royal Purple like screenshot
  val shortsColorIndex: Int = 1, // Royal Purple
  val shoesColorIndex: Int = 1,
  val heightScale: Float = 1.0f,
  val reachScale: Float = 1.0f,
  val muscleScale: Float = 1.0f,
  val statPower: Int = 6,
  val statSpeed: Int = 6,
  val statStamina: Int = 6,
  val statChin: Int = 6
) {
  companion object {
    val SkinTones = listOf(
      Color(0xFFC49A76), // Classic Low-Poly Wood Tan as in OB3D!
      Color(0xFFD4A373), // Warm Sand
      Color(0xFF8D5524), // Rich Bronze
      Color(0xFFE0AC69), // Golden Tan
      Color(0xFFF1C27D), // Pale
      Color(0xFF4A3728)  // Deep Umber
    )

    val GearColors = listOf(
      Color(0xFFDC2626), // Bold Red (Opponent in screenshot)
      Color(0xFF7C3AED), // Royal Purple (Player in screenshot)
      Color(0xFF10B981), // Emerald Green (Opponent trunks in screenshot)
      Color(0xFF0284C7), // Electric Blue
      Color(0xFFF59E0B), // Championship Gold
      Color(0xFF1E293B), // Matte Onyx Black
      Color(0xFFF43F5E), // Hot Crimson
      Color(0xFF14B8A6)  // Neon Cyan
    )
  }

  fun getSkinColor(): Color = SkinTones.getOrElse(skinToneIndex) { SkinTones[0] }
  fun getGloveColor(): Color = GearColors.getOrElse(gloveColorIndex) { GearColors[0] }
  fun getShortsColor(): Color = GearColors.getOrElse(shortsColorIndex) { GearColors[1] }
  fun getShoesColor(): Color = GearColors.getOrElse(shoesColorIndex) { GearColors[1] }
}

data class CombatFighter(
  val customization: FighterCustomization,
  var posX: Float = 0f,
  var posZ: Float = 0f,
  var angleY: Float = 0f,
  var headHealth: Float = 100f,
  var maxHeadHealth: Float = 100f,
  var bodyHealth: Float = 100f,
  var maxBodyHealth: Float = 100f,
  var stamina: Float = 100f,
  var maxStamina: Float = 100f,
  var actionState: ActionState = ActionState.IDLE,
  var activePunch: PunchType? = null,
  var actionProgress: Float = 0f,
  var isBlocking: Boolean = false,
  var isBlockingBody: Boolean = false,
  var isClinching: Boolean = false,
  var clinchTimer: Float = 0f,
  var pushVelocityX: Float = 0f,
  var pushVelocityZ: Float = 0f,
  // Analog slip / weave 3D physics offsets
  var slipX: Float = 0f,
  var slipY: Float = 0f,
  var slipZ: Float = 0f,
  var targetSlipX: Float = 0f,
  var targetSlipY: Float = 0f,
  // Hit recoil physical simulation
  var hitRecoilX: Float = 0f,
  var hitRecoilY: Float = 0f,
  var hitStunDuration: Float = 0f,
  // Ragdoll Knockdown physical dynamics
  var knockdownsInRound: Int = 0,
  var totalKnockdowns: Int = 0,
  var isDown: Boolean = false,
  var downTimer: Float = 0f,
  var ragdollPitch: Float = 0f,
  var ragdollRoll: Float = 0f,
  var ragdollBounceY: Float = 0f,
  var getUpTaps: Int = 0,
  var requiredTaps: Int = 10,
  var isAI: Boolean = false,
  var aiArchetype: AIArchetype = AIArchetype.SLUGGER
)

enum class AIArchetype(val displayName: String, val aggressiveness: Float, val slipChance: Float, val bodyShotBias: Float) {
  SLUGGER("Slugger", 0.75f, 0.25f, 0.3f),
  OUT_BOXER("Out-Boxer", 0.45f, 0.65f, 0.2f),
  SWARMER("Swarmer", 0.85f, 0.4f, 0.6f),
  COUNTER_PUNCHER("Counter-Puncher", 0.5f, 0.75f, 0.4f)
}
