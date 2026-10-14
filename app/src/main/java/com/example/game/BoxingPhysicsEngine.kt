package com.example.game

import com.example.model.*
import com.example.util.SoundEffects
import kotlin.math.*

data class HitImpactSpark(
  val x: Float,
  val y: Float,
  val z: Float,
  var lifetime: Float = 0.25f,
  val isCounter: Boolean = false
)

class BoxingPhysicsEngine(
  private val soundEffects: SoundEffects? = null
) {
  var roundNumber: Int = 1
  val totalRounds: Int = 3
  var roundTimeRemaining: Float = 120f
  var isMatchOver: Boolean = false
  var matchWinner: CombatFighter? = null
  var matchEndReason: String = ""

  var refereeCount: Int = 0
  var refereeTimer: Float = 0f
  var activeKnockedDownFighter: CombatFighter? = null

  // Punch stats
  var playerPunchesThrown: Int = 0
  var playerPunchesLanded: Int = 0
  var opponentPunchesThrown: Int = 0
  var opponentPunchesLanded: Int = 0

  // Camera shake effect
  var cameraShakeIntensity: Float = 0f

  // Dynamic hit sparks
  val activeSparks = mutableListOf<HitImpactSpark>()

  // Ring boundary limits
  val ringBoundary = 3.8f
  private val minFighterDist = 1.35f

  fun resetMatch(
    playerCustom: FighterCustomization,
    opponentCustom: FighterCustomization,
    aiArchetype: AIArchetype = AIArchetype.SLUGGER
  ) {
    roundNumber = 1
    roundTimeRemaining = 120f
    isMatchOver = false
    matchWinner = null
    matchEndReason = ""
    refereeCount = 0
    refereeTimer = 0f
    activeKnockedDownFighter = null
    playerPunchesThrown = 0
    playerPunchesLanded = 0
    opponentPunchesThrown = 0
    opponentPunchesLanded = 0
    cameraShakeIntensity = 0f
    activeSparks.clear()
  }

  fun update(
    dt: Float,
    player: CombatFighter,
    opponent: CombatFighter,
    moveX: Float,
    moveZ: Float,
    onKnockdown: (CombatFighter) -> Unit = {}
  ) {
    if (isMatchOver) return

    // Update active hit sparks
    val it = activeSparks.iterator()
    while (it.hasNext()) {
      val s = it.next()
      s.lifetime -= dt
      if (s.lifetime <= 0f) it.remove()
    }

    // Decay camera shake
    if (cameraShakeIntensity > 0f) {
      cameraShakeIntensity = (cameraShakeIntensity - dt * 2.8f).coerceAtLeast(0f)
    }

    // 1. Referee 10-count check
    if (activeKnockedDownFighter != null) {
      updateRefereeCount(dt, player, opponent)
      updateRagdollPhysics(dt, player)
      updateRagdollPhysics(dt, opponent)
      return
    }

    // 2. Round Timer
    roundTimeRemaining -= dt
    if (roundTimeRemaining <= 0f) {
      if (roundNumber >= totalRounds) {
        handleDecision(player, opponent)
        return
      } else {
        roundNumber++
        roundTimeRemaining = 120f
        soundEffects?.playBell()
        player.posX = 0f
        player.posZ = -2.6f
        opponent.posX = 0f
        opponent.posZ = 2.6f
        player.knockdownsInRound = 0
        opponent.knockdownsInRound = 0
        player.stamina = (player.stamina + 45f).coerceAtMost(player.maxStamina)
        opponent.stamina = (opponent.stamina + 45f).coerceAtMost(opponent.maxStamina)
        player.headHealth = (player.headHealth + 20f).coerceAtMost(player.maxHeadHealth)
        opponent.headHealth = (opponent.headHealth + 20f).coerceAtMost(opponent.maxHeadHealth)
      }
    }

    // 3. Update Facing
    updateFighterFacing(player, opponent)
    updateFighterFacing(opponent, player)

    // 4. Update Push velocity & Clinch
    updatePushAndClinch(dt, player, opponent)
    updatePushAndClinch(dt, opponent, player)

    // 5. Update Movement
    if (!player.isDown && player.actionState != ActionState.HIT_STUN && !player.isClinching) {
      updatePlayerMovement(player, moveX, moveZ, dt)
    }

    // 6. Update AI
    if (opponent.isAI && !opponent.isDown && opponent.actionState != ActionState.HIT_STUN && !opponent.isClinching) {
      updateAI(dt, opponent, player)
    }

    // 7. Enforce Ring Boundary & Fighter Distance
    enforceRingCollisions(player, opponent)

    // 8. Update Actions & Strikes
    updateFighterAction(dt, player, opponent, isPlayerAttacking = true)
    updateFighterAction(dt, opponent, player, isPlayerAttacking = false)

    // 9. Update Analog Bob & Weave
    updateAnalogSlipPhysics(dt, player)
    updateAnalogSlipPhysics(dt, opponent)

    // 10. Stamina Regeneration
    regenerateStamina(dt, player)
    regenerateStamina(dt, opponent)

    // 11. Check Knockdowns
    if (!player.isDown && (player.headHealth <= 0f || player.bodyHealth <= 0f)) {
      triggerKnockdown(player, opponent, onKnockdown)
    } else if (!opponent.isDown && (opponent.headHealth <= 0f || opponent.bodyHealth <= 0f)) {
      triggerKnockdown(opponent, player, onKnockdown)
    }
  }

  private fun updatePushAndClinch(dt: Float, fighter: CombatFighter, target: CombatFighter) {
    // Apply push velocity damping
    if (abs(fighter.pushVelocityX) > 0.01f || abs(fighter.pushVelocityZ) > 0.01f) {
      fighter.posX += fighter.pushVelocityX * dt
      fighter.posZ += fighter.pushVelocityZ * dt
      fighter.pushVelocityX *= (1f - dt * 6f)
      fighter.pushVelocityZ *= (1f - dt * 6f)
    }

    // Clinch timer update
    if (fighter.isClinching) {
      fighter.clinchTimer += dt
      if (fighter.clinchTimer >= 2.5f) {
        // Referee breaks clinch
        breakClinch(fighter, target)
      }
    }
  }

  fun triggerClinchOrPush(player: CombatFighter, opponent: CombatFighter) {
    val dx = opponent.posX - player.posX
    val dz = opponent.posZ - player.posZ
    val dist = sqrt(dx * dx + dz * dz)

    if (dist < 1.7f) {
      if (player.isClinching || opponent.isClinching) {
        // Push opponent off
        breakClinch(player, opponent, pushForce = 3.5f)
        soundEffects?.playBlock()
      } else {
        // Initiate clinch lockup
        player.isClinching = true
        opponent.isClinching = true
        player.clinchTimer = 0f
        opponent.clinchTimer = 0f
        player.actionState = ActionState.CLINCHING
        opponent.actionState = ActionState.CLINCHING
        player.activePunch = null
        opponent.activePunch = null
        soundEffects?.playBlock()
      }
    }
  }

  private fun breakClinch(f1: CombatFighter, f2: CombatFighter, pushForce: Float = 2.0f) {
    f1.isClinching = false
    f2.isClinching = false
    f1.clinchTimer = 0f
    f2.clinchTimer = 0f
    f1.actionState = ActionState.IDLE
    f2.actionState = ActionState.IDLE

    val dx = f2.posX - f1.posX
    val dz = f2.posZ - f1.posZ
    val len = sqrt(dx * dx + dz * dz).coerceAtLeast(0.01f)
    val nx = dx / len
    val nz = dz / len

    f2.pushVelocityX = nx * pushForce
    f2.pushVelocityZ = nz * pushForce
    f1.pushVelocityX = -nx * 0.8f
    f1.pushVelocityZ = -nz * 0.8f
  }

  private fun updatePlayerMovement(fighter: CombatFighter, inputX: Float, inputZ: Float, dt: Float) {
    if (fighter.actionState == ActionState.PUNCHING) return

    val speed = 2.7f * (fighter.customization.statSpeed / 6f) * (if (fighter.isBlocking) 0.65f else 1.0f)
    if (abs(inputX) > 0.05f || abs(inputZ) > 0.05f) {
      fighter.actionState = ActionState.WALKING
      fighter.posX += inputX * speed * dt
      fighter.posZ += inputZ * speed * dt
    } else if (fighter.actionState == ActionState.WALKING) {
      fighter.actionState = ActionState.IDLE
    }
  }

  private fun updateFighterFacing(fighter: CombatFighter, target: CombatFighter) {
    val dx = target.posX - fighter.posX
    val dz = target.posZ - fighter.posZ
    val angle = Math.toDegrees(atan2(dx.toDouble(), dz.toDouble())).toFloat()
    fighter.angleY = angle
  }

  private fun enforceRingCollisions(f1: CombatFighter, f2: CombatFighter) {
    f1.posX = f1.posX.coerceIn(-ringBoundary, ringBoundary)
    f1.posZ = f1.posZ.coerceIn(-ringBoundary, ringBoundary)
    f2.posX = f2.posX.coerceIn(-ringBoundary, ringBoundary)
    f2.posZ = f2.posZ.coerceIn(-ringBoundary, ringBoundary)

    val dx = f2.posX - f1.posX
    val dz = f2.posZ - f1.posZ
    val dist = sqrt(dx * dx + dz * dz)
    if (dist < minFighterDist && dist > 0.001f) {
      val overlap = (minFighterDist - dist) * 0.5f
      val nx = dx / dist
      val nz = dz / dist
      f1.posX -= nx * overlap
      f1.posZ -= nz * overlap
      f2.posX += nx * overlap
      f2.posZ += nz * overlap
    }
  }

  private fun updateAnalogSlipPhysics(dt: Float, fighter: CombatFighter) {
    // Spring physics towards target slip
    val springSpeed = 12f * dt
    fighter.slipX += (fighter.targetSlipX - fighter.slipX) * springSpeed
    fighter.slipY += (fighter.targetSlipY - fighter.slipY) * springSpeed
  }

  private fun updateRagdollPhysics(dt: Float, fighter: CombatFighter) {
    if (!fighter.isDown) return
    fighter.downTimer += dt

    // Physical ragdoll collapse
    val targetPitch = 88f
    val pitchSpeed = 160f * dt
    fighter.ragdollPitch = (fighter.ragdollPitch + pitchSpeed).coerceAtMost(targetPitch)

    // Slight bounce when hitting canvas
    if (fighter.downTimer in 0.35f..0.65f) {
      fighter.ragdollBounceY = abs(sin((fighter.downTimer - 0.35f) * 10f)) * 0.12f
    } else {
      fighter.ragdollBounceY = 0f
    }
  }

  private fun regenerateStamina(dt: Float, fighter: CombatFighter) {
    if (fighter.isDown) return
    val baseRegen = 8.5f * (fighter.customization.statStamina / 6f)
    val regenMultiplier = when {
      fighter.isBlocking -> 0.4f
      fighter.actionState == ActionState.PUNCHING -> 0.15f
      fighter.actionState == ActionState.WALKING -> 0.8f
      else -> 1.0f
    }
    val effectiveMax = fighter.maxStamina * (0.5f + (fighter.bodyHealth / 200f))
    fighter.stamina = (fighter.stamina + baseRegen * regenMultiplier * dt).coerceAtMost(effectiveMax)
  }

  private fun updateFighterAction(
    dt: Float,
    attacker: CombatFighter,
    defender: CombatFighter,
    isPlayerAttacking: Boolean
  ) {
    if (attacker.hitStunDuration > 0f) {
      attacker.hitStunDuration -= dt
      attacker.hitRecoilX *= (1f - dt * 8f)
      attacker.hitRecoilY *= (1f - dt * 8f)
      if (attacker.hitStunDuration <= 0f) {
        attacker.hitStunDuration = 0f
        attacker.hitRecoilX = 0f
        attacker.hitRecoilY = 0f
        if (attacker.actionState == ActionState.HIT_STUN) {
          attacker.actionState = ActionState.IDLE
        }
      }
      return
    }

    if (attacker.actionState in listOf(ActionState.SLIP_LEFT, ActionState.SLIP_RIGHT, ActionState.DUCK, ActionState.LEAN_BACK)) {
      attacker.actionProgress += dt * 3.6f
      if (attacker.actionProgress >= 1f) {
        attacker.actionState = ActionState.IDLE
        attacker.actionProgress = 0f
        attacker.targetSlipX = 0f
        attacker.targetSlipY = 0f
        attacker.slipZ = 0f
      }
      return
    }

    if (attacker.actionState == ActionState.PUNCHING) {
      val punch = attacker.activePunch ?: return
      val speedFactor = punch.speed * (attacker.customization.statSpeed / 6f)
      val prevProgress = attacker.actionProgress
      attacker.actionProgress += dt * 3.4f * speedFactor

      if (prevProgress < 0.5f && attacker.actionProgress >= 0.5f) {
        executeHitCheck(attacker, defender, punch, isPlayerAttacking)
      }

      if (attacker.actionProgress >= 1.0f) {
        attacker.actionState = ActionState.IDLE
        attacker.activePunch = null
        attacker.actionProgress = 0f
      }
    }
  }

  private fun executeHitCheck(
    attacker: CombatFighter,
    defender: CombatFighter,
    punch: PunchType,
    isPlayerAttacking: Boolean
  ) {
    if (isPlayerAttacking) playerPunchesThrown++ else opponentPunchesThrown++

    val dx = defender.posX - attacker.posX
    val dz = defender.posZ - attacker.posZ
    val dist = sqrt(dx * dx + dz * dz)
    val maxReach = 1.95f * attacker.customization.reachScale

    if (dist > maxReach) {
      soundEffects?.playWhoosh()
      return
    }

    // Defensive evasions
    var evaded = false
    when (defender.actionState) {
      ActionState.SLIP_LEFT, ActionState.SLIP_RIGHT -> {
        if (!punch.isHook && !punch.isUppercut) evaded = true
      }
      ActionState.DUCK -> {
        if (punch.isHook && !punch.isBody) evaded = true
      }
      ActionState.LEAN_BACK -> {
        if (dist > 1.45f) evaded = true
      }
      else -> {}
    }

    if (evaded) {
      soundEffects?.playWhoosh()
      return
    }

    val isBlocked = if (punch.isBody) {
      defender.isBlocking && defender.isBlockingBody
    } else {
      defender.isBlocking && !defender.isBlockingBody
    }

    val isCounter = (defender.actionState == ActionState.PUNCHING)
    val counterBonus = if (isCounter) 1.55f else 1.0f
    val rawPower = punch.baseDamage * (attacker.customization.statPower / 6f) * counterBonus

    // Spawn 3D impact spark at mid-point
    val hitY = if (punch.isBody) 0.65f else 1.35f
    activeSparks.add(HitImpactSpark((attacker.posX + defender.posX) * 0.5f, hitY, (attacker.posZ + defender.posZ) * 0.5f, isCounter = isCounter))

    if (isBlocked) {
      val chipDamage = rawPower * 0.16f
      val stamDrain = rawPower * 0.82f

      if (punch.isBody) {
        defender.bodyHealth = (defender.bodyHealth - chipDamage).coerceAtLeast(0f)
      } else {
        defender.headHealth = (defender.headHealth - chipDamage).coerceAtLeast(0f)
      }
      defender.stamina = (defender.stamina - stamDrain).coerceAtLeast(0f)

      soundEffects?.playBlock()
      cameraShakeIntensity = 0.18f
    } else {
      if (isPlayerAttacking) playerPunchesLanded++ else opponentPunchesLanded++

      val chinDefense = defender.customization.statChin / 6f
      val finalDamage = (rawPower / chinDefense).coerceAtLeast(4f)

      if (punch.isBody) {
        defender.bodyHealth = (defender.bodyHealth - finalDamage).coerceAtLeast(0f)
        defender.stamina = (defender.stamina - finalDamage * 1.25f).coerceAtLeast(0f)
      } else {
        defender.headHealth = (defender.headHealth - finalDamage).coerceAtLeast(0f)
      }

      val recoilStrength = (finalDamage / 22f).coerceIn(0.25f, 0.95f)
      val recoilDir = if (punch.isLeftHand) 1f else -1f
      defender.hitRecoilX = if (punch.isHook) recoilDir * recoilStrength else (Math.random().toFloat() - 0.5f) * 0.35f
      defender.hitRecoilY = if (punch.isUppercut) -recoilStrength else (recoilStrength * 0.65f)
      defender.hitStunDuration = if (isCounter) 0.38f else 0.22f
      defender.actionState = ActionState.HIT_STUN

      // Push back impulse
      val pushLen = sqrt(dx * dx + dz * dz).coerceAtLeast(0.01f)
      defender.pushVelocityX = (dx / pushLen) * (finalDamage * 0.08f)
      defender.pushVelocityZ = (dz / pushLen) * (finalDamage * 0.08f)

      soundEffects?.playHit(isHeavy = punch.isHook || punch.isUppercut || isCounter)
      cameraShakeIntensity = if (isCounter) 0.6f else 0.35f
    }
  }

  fun triggerPlayerPunch(player: CombatFighter, punch: PunchType): Boolean {
    if (player.isDown || player.actionState == ActionState.PUNCHING || player.actionState == ActionState.HIT_STUN || player.isClinching) {
      return false
    }
    if (player.stamina < punch.staminaCost * 0.5f) {
      return false
    }

    player.stamina = (player.stamina - punch.staminaCost).coerceAtLeast(0f)
    player.actionState = ActionState.PUNCHING
    player.activePunch = punch
    player.actionProgress = 0f
    return true
  }

  fun triggerSlip(player: CombatFighter, slipType: ActionState) {
    if (player.isDown || player.actionState == ActionState.PUNCHING || player.actionState == ActionState.HIT_STUN || player.isClinching) return
    if (player.stamina < 6f) return

    player.stamina = (player.stamina - 6f).coerceAtLeast(0f)
    player.actionState = slipType
    player.actionProgress = 0f

    when (slipType) {
      ActionState.SLIP_LEFT -> { player.targetSlipX = -0.42f; player.targetSlipY = -0.06f }
      ActionState.SLIP_RIGHT -> { player.targetSlipX = 0.42f; player.targetSlipY = -0.06f }
      ActionState.DUCK -> { player.targetSlipY = -0.38f }
      ActionState.LEAN_BACK -> { player.slipZ = -0.35f }
      else -> {}
    }
  }

  fun setGuard(player: CombatFighter, isBlocking: Boolean, isBody: Boolean = false) {
    if (player.isDown || player.isClinching) return
    player.isBlocking = isBlocking
    player.isBlockingBody = isBody
  }

  private fun triggerKnockdown(
    downFighter: CombatFighter,
    standFighter: CombatFighter,
    onKnockdown: (CombatFighter) -> Unit
  ) {
    downFighter.isDown = true
    downFighter.actionState = ActionState.KNOCKED_DOWN
    downFighter.knockdownsInRound++
    downFighter.totalKnockdowns++
    downFighter.downTimer = 0f
    downFighter.ragdollPitch = 0f
    downFighter.ragdollBounceY = 0f
    downFighter.getUpTaps = 0
    downFighter.requiredTaps = 8 + downFighter.totalKnockdowns * 4

    standFighter.actionState = ActionState.IDLE
    standFighter.activePunch = null
    standFighter.isClinching = false

    activeKnockedDownFighter = downFighter
    refereeCount = 0
    refereeTimer = 0f
    soundEffects?.playKnockdown()
    cameraShakeIntensity = 0.8f
    onKnockdown(downFighter)

    if (downFighter.knockdownsInRound >= 3) {
      isMatchOver = true
      matchWinner = standFighter
      matchEndReason = "TKO (3 KNOCKDOWNS IN ROUND)"
      soundEffects?.playBell()
    }
  }

  private fun updateRefereeCount(dt: Float, player: CombatFighter, opponent: CombatFighter) {
    val downFighter = activeKnockedDownFighter ?: return
    refereeTimer += dt

    if (refereeTimer >= 1.0f) {
      refereeTimer -= 1.0f
      refereeCount++
      soundEffects?.playRefereeCount()

      if (downFighter.isAI) {
        val getUpChance = 0.35f - (downFighter.totalKnockdowns * 0.1f)
        if (refereeCount in 5..8 && Math.random() < getUpChance) {
          standFighterUp(downFighter)
          return
        }
      }

      if (refereeCount >= 10) {
        isMatchOver = true
        matchWinner = if (downFighter == player) opponent else player
        matchEndReason = "KNOCKOUT (10-COUNT)"
        soundEffects?.playBell()
      }
    }
  }

  fun registerPlayerGetUpTap(player: CombatFighter) {
    if (activeKnockedDownFighter != player || !player.isDown) return
    player.getUpTaps++
    if (player.getUpTaps >= player.requiredTaps) {
      standFighterUp(player)
    }
  }

  private fun standFighterUp(fighter: CombatFighter) {
    fighter.isDown = false
    fighter.actionState = ActionState.IDLE
    fighter.headHealth = (fighter.maxHeadHealth * 0.45f).coerceAtLeast(30f)
    fighter.bodyHealth = (fighter.maxBodyHealth * 0.45f).coerceAtLeast(30f)
    fighter.stamina = (fighter.maxStamina * 0.5f).coerceAtLeast(40f)
    fighter.ragdollPitch = 0f
    fighter.ragdollBounceY = 0f
    activeKnockedDownFighter = null
    refereeCount = 0
    refereeTimer = 0f
  }

  private fun handleDecision(player: CombatFighter, opponent: CombatFighter) {
    isMatchOver = true
    soundEffects?.playBell()
    val playerScore = (playerPunchesLanded * 2) + (opponent.totalKnockdowns * 25) + player.headHealth.toInt()
    val opponentScore = (opponentPunchesLanded * 2) + (player.totalKnockdowns * 25) + opponent.headHealth.toInt()

    matchWinner = if (playerScore >= opponentScore) player else opponent
    matchEndReason = "DECISION ($playerScore vs $opponentScore)"
  }

  // --- AI LOGIC ---
  private var aiActionTimer = 0f

  private fun updateAI(dt: Float, ai: CombatFighter, player: CombatFighter) {
    aiActionTimer -= dt
    val dx = player.posX - ai.posX
    val dz = player.posZ - ai.posZ
    val dist = sqrt(dx * dx + dz * dz)

    val archetype = ai.aiArchetype
    val idealDist = when (archetype) {
      AIArchetype.OUT_BOXER -> 2.1f
      AIArchetype.SWARMER -> 1.35f
      AIArchetype.COUNTER_PUNCHER -> 1.75f
      AIArchetype.SLUGGER -> 1.55f
    }

    val moveSpeed = 2.4f * dt
    if (dist > idealDist + 0.3f) {
      ai.posX += (dx / dist) * moveSpeed
      ai.posZ += (dz / dist) * moveSpeed
      ai.actionState = ActionState.WALKING
    } else if (dist < idealDist - 0.25f) {
      ai.posX -= (dx / dist) * moveSpeed
      ai.posZ -= (dz / dist) * moveSpeed
      ai.actionState = ActionState.WALKING
    } else {
      val sideX = -dz / dist
      val sideZ = dx / dist
      ai.posX += sideX * moveSpeed * 0.5f
      ai.posZ += sideZ * moveSpeed * 0.5f
    }

    // Defensive Reaction
    if (player.actionState == ActionState.PUNCHING && player.actionProgress < 0.4f) {
      if (Math.random() < archetype.slipChance) {
        val slipRoll = Math.random()
        when {
          slipRoll < 0.4 -> triggerSlip(ai, ActionState.SLIP_LEFT)
          slipRoll < 0.8 -> triggerSlip(ai, ActionState.SLIP_RIGHT)
          else -> triggerSlip(ai, ActionState.DUCK)
        }
        return
      } else if (Math.random() < 0.65) {
        setGuard(ai, isBlocking = true, isBody = player.activePunch?.isBody == true)
        return
      }
    } else {
      ai.isBlocking = false
    }

    // Clinch check for swarmer if stuck in pocket
    if (dist < 1.4f && Math.random() < 0.05 && !ai.isClinching) {
      triggerClinchOrPush(ai, player)
      return
    }

    // Attack Action
    if (aiActionTimer <= 0f && dist <= 2.0f && ai.actionState != ActionState.PUNCHING && !ai.isClinching) {
      aiActionTimer = (0.55f + Math.random().toFloat() * 0.7f) / archetype.aggressiveness

      val isBody = Math.random() < archetype.bodyShotBias
      val punches = if (isBody) {
        listOf(PunchType.BODY_JAB, PunchType.BODY_CROSS, PunchType.BODY_HOOK)
      } else {
        when (archetype) {
          AIArchetype.SLUGGER -> listOf(PunchType.CROSS, PunchType.R_HOOK, PunchType.R_UPPERCUT)
          AIArchetype.OUT_BOXER -> listOf(PunchType.JAB, PunchType.CROSS, PunchType.L_HOOK)
          AIArchetype.SWARMER -> listOf(PunchType.L_HOOK, PunchType.R_HOOK, PunchType.L_UPPERCUT)
          AIArchetype.COUNTER_PUNCHER -> listOf(PunchType.CROSS, PunchType.R_HOOK, PunchType.JAB)
        }
      }

      val chosenPunch = punches.random()
      triggerPlayerPunch(ai, chosenPunch)
    }
  }
}
