package com.example.engine3d

import androidx.compose.ui.graphics.Color
import com.example.game.HitImpactSpark
import com.example.model.ActionState
import com.example.model.CombatFighter
import com.example.model.PunchType
import com.example.model.Stance
import kotlin.math.*

object MeshBuilder {

  fun buildRing(ringSize: Float = 4.8f): List<Polygon3D> {
    val polys = mutableListOf<Polygon3D>()

    val matColor = Color(0xFFD6DEE7)
    val apronColor = Color(0xFF1E293B)

    // Central canvas floor
    polys.add(
      Polygon3D(
        vertices = listOf(
          Vec3(-ringSize, 0f, -ringSize),
          Vec3(ringSize, 0f, -ringSize),
          Vec3(ringSize, 0f, ringSize),
          Vec3(-ringSize, 0f, ringSize)
        ),
        color = matColor,
        isShaded = false,
        strokeColor = Color(0xFF94A3B8),
        tag = "ring_mat"
      )
    )

    // Floor logo: "OB3D boxing"
    val logoZ = 0.5f
    val logoX = -0.5f
    polys.add(
      Polygon3D(
        vertices = listOf(
          Vec3(logoX - 1.2f, 0.01f, logoZ - 0.4f),
          Vec3(logoX - 0.2f, 0.01f, logoZ - 0.4f),
          Vec3(logoX - 0.2f, 0.01f, logoZ + 0.4f),
          Vec3(logoX - 1.2f, 0.01f, logoZ + 0.4f)
        ),
        color = Color(0xFFEA580C),
        isShaded = false,
        tag = "logo_3d"
      )
    )
    polys.add(
      Polygon3D(
        vertices = listOf(
          Vec3(logoX - 2.4f, 0.01f, logoZ - 0.35f),
          Vec3(logoX - 1.4f, 0.01f, logoZ - 0.35f),
          Vec3(logoX - 1.4f, 0.01f, logoZ + 0.35f),
          Vec3(logoX - 2.4f, 0.01f, logoZ + 0.35f)
        ),
        color = Color(0xFF1E293B),
        isShaded = false,
        tag = "logo_ob"
      )
    )

    // Ring apron extension
    val ext = ringSize + 1.0f
    polys.add(
      Polygon3D(
        vertices = listOf(
          Vec3(-ext, -0.05f, -ext),
          Vec3(ext, -0.05f, -ext),
          Vec3(ext, -0.05f, ringSize),
          Vec3(-ext, -0.05f, ringSize)
        ),
        color = apronColor,
        isShaded = true
      )
    )

    // Corner Posts & Pads
    val postHeight = 3.2f
    val postW = 0.35f

    val cornerColors = listOf(
      Color(0xFFDC2626), // Back Red Corner (visible in screenshot!)
      Color(0xFF2563EB), // Opposite Blue Corner
      Color(0xFFF8FAFC),
      Color(0xFFF8FAFC)
    )
    val cornerCoords = listOf(
      Pair(0f, ringSize),
      Pair(0f, -ringSize),
      Pair(-ringSize, 0f),
      Pair(ringSize, 0f)
    )

    for (i in 0 until 4) {
      val (cx, cz) = cornerCoords[i]
      val col = cornerColors[i]
      polys.addAll(createBox(cx, postHeight / 2f, cz, postW, postHeight, postW, col, Color(0xFF991B1B)))
    }

    // 4 Ropes
    val ropeHeights = listOf(0.9f, 1.6f, 2.3f, 3.0f)
    val ropeColors = listOf(
      Color(0xFF1D4ED8),
      Color(0xFFDC2626),
      Color(0xFF1D4ED8),
      Color(0xFFDC2626)
    )

    for (r in 0 until 4) {
      val h = ropeHeights[r]
      val col = ropeColors[r]
      val ropeThick = 0.05f

      for (c in 0 until 4) {
        val (x1, z1) = cornerCoords[c]
        val (x2, z2) = cornerCoords[(c + 1) % 4]

        val dx = x2 - x1
        val dz = z2 - z1
        val len = sqrt(dx * dx + dz * dz)
        val nx = -dz / len * ropeThick
        val nz = dx / len * ropeThick

        polys.add(
          Polygon3D(
            vertices = listOf(
              Vec3(x1 + nx, h, z1 + nz),
              Vec3(x2 + nx, h, z2 + nz),
              Vec3(x2 - nx, h - ropeThick, z2 - nz),
              Vec3(x1 - nx, h - ropeThick, z1 - nz)
            ),
            color = col,
            isShaded = false
          )
        )
      }
    }

    return polys
  }

  fun buildSparks(sparks: List<HitImpactSpark>): List<Polygon3D> {
    val polys = mutableListOf<Polygon3D>()
    for (s in sparks) {
      val sz = if (s.isCounter) 0.35f else 0.22f
      val col = if (s.isCounter) Color(0xFFFACC15) else Color(0xFFF97316)
      polys.add(
        Polygon3D(
          vertices = listOf(
            Vec3(s.x, s.y + sz, s.z),
            Vec3(s.x + sz, s.y, s.z),
            Vec3(s.x, s.y - sz, s.z),
            Vec3(s.x - sz, s.y, s.z)
          ),
          color = col,
          isShaded = false
        )
      )
    }
    return polys
  }

  fun buildBoxer(
    fighter: CombatFighter,
    timeSeconds: Float
  ): List<Polygon3D> {
    val polys = mutableListOf<Polygon3D>()
    val custom = fighter.customization

    val skinColor = custom.getSkinColor()
    val gloveColor = custom.getGloveColor()
    val shortsColor = custom.getShortsColor()
    val shoesColor = custom.getShoesColor()
    val outlineColor = Color(0x33000000)

    val scaleH = custom.heightScale
    val scaleR = custom.reachScale
    val scaleM = custom.muscleScale

    val px = fighter.posX
    val pz = fighter.posZ
    val facingRad = Math.toRadians(fighter.angleY.toDouble()).toFloat()

    val breath = if (!fighter.isDown) sin(timeSeconds * 4.5f) * 0.03f else 0f
    val bounceY = if (!fighter.isDown && fighter.actionState == ActionState.WALKING) {
      abs(sin(timeSeconds * 8f)) * 0.08f
    } else 0f

    // Physical ragdoll pitch & bounce
    val isDown = fighter.isDown
    val downAngle = if (isDown) fighter.ragdollPitch else 0f
    val downBounce = if (isDown) fighter.ragdollBounceY else 0f

    val recoilX = fighter.hitRecoilX
    val recoilZ = fighter.hitRecoilY * 0.5f

    val slipX = fighter.slipX
    val slipY = fighter.slipY
    val slipZ = fighter.slipZ

    val hipY = if (isDown) (0.28f + downBounce) else (1.6f * scaleH + breath + bounceY + slipY)

    fun toWorld(local: Vec3): Vec3 {
      var lx = local.x * scaleM
      var ly = local.y * scaleH
      var lz = local.z

      if (recoilX != 0f || recoilZ != 0f) {
        lx += recoilX * (ly / 2.5f)
        lz += recoilZ * (ly / 2.5f)
      }

      if (downAngle > 0f) {
        val downRad = Math.toRadians(downAngle.toDouble()).toFloat()
        val cosD = cos(downRad)
        val sinD = sin(downRad)
        val tempY = ly * cosD - lz * sinD
        val tempZ = ly * sinD + lz * cosD
        ly = tempY
        lz = tempZ
      }

      val cosF = cos(facingRad)
      val sinF = sin(facingRad)
      val wx = px + (lx * cosF - lz * sinF) + slipX
      val wz = pz + (lx * sinF + lz * cosF) + slipZ
      val wy = hipY + ly

      return Vec3(wx, max(wy, 0f), wz)
    }

    fun addBox(
      cx: Float, cy: Float, cz: Float,
      w: Float, h: Float, d: Float,
      color: Color,
      shaded: Boolean = true
    ) {
      val hw = w / 2f
      val hh = h / 2f
      val hd = d / 2f

      val v000 = toWorld(Vec3(cx - hw, cy - hh, cz - hd))
      val v100 = toWorld(Vec3(cx + hw, cy - hh, cz - hd))
      val v110 = toWorld(Vec3(cx + hw, cy + hh, cz - hd))
      val v010 = toWorld(Vec3(cx - hw, cy + hh, cz - hd))

      val v001 = toWorld(Vec3(cx - hw, cy - hh, cz + hd))
      val v101 = toWorld(Vec3(cx + hw, cy - hh, cz + hd))
      val v111 = toWorld(Vec3(cx + hw, cy + hh, cz + hd))
      val v011 = toWorld(Vec3(cx - hw, cy + hh, cz + hd))

      polys.add(Polygon3D(listOf(v001, v101, v111, v011), color, shaded, outlineColor))
      polys.add(Polygon3D(listOf(v100, v000, v010, v110), color, shaded, outlineColor))
      polys.add(Polygon3D(listOf(v010, v011, v111, v110), color, shaded, outlineColor))
      polys.add(Polygon3D(listOf(v000, v100, v101, v001), color, shaded, outlineColor))
      polys.add(Polygon3D(listOf(v000, v001, v011, v010), color, shaded, outlineColor))
      polys.add(Polygon3D(listOf(v101, v100, v110, v111), color, shaded, outlineColor))
    }

    // --- HIPS & BOXING TRUNKS ---
    val waistbandColor = Color(0xFFF8FAFC)
    addBox(0f, 0.25f, 0f, 0.76f, 0.12f, 0.52f, waistbandColor)
    addBox(0f, 0.05f, 0f, 0.82f, 0.35f, 0.56f, shortsColor)

    addBox(-0.24f, -0.2f, 0f, 0.38f, 0.32f, 0.44f, shortsColor)
    addBox(0.24f, -0.2f, 0f, 0.38f, 0.32f, 0.44f, shortsColor)

    // --- LEGS & BOOTS ---
    val legColor = skinColor
    addBox(-0.24f, -0.45f, 0f, 0.24f, 0.35f, 0.24f, legColor)
    addBox(-0.24f, -0.85f, -0.05f, 0.22f, 0.45f, 0.22f, legColor)
    addBox(-0.24f, -1.18f, 0.04f, 0.26f, 0.24f, 0.46f, shoesColor)

    val stanceZ = if (custom.stance == Stance.ORTHODOX) -0.25f else 0.25f
    addBox(0.24f, -0.45f, stanceZ, 0.24f, 0.35f, 0.24f, legColor)
    addBox(0.24f, -0.85f, stanceZ - 0.05f, 0.22f, 0.45f, 0.22f, legColor)
    addBox(0.24f, -1.18f, stanceZ + 0.04f, 0.26f, 0.24f, 0.46f, shoesColor)

    // --- TORSO ---
    addBox(0f, 0.45f, 0f, 0.68f, 0.28f, 0.42f, skinColor)
    addBox(0f, 0.75f, 0f, 0.95f, 0.36f, 0.48f, skinColor)

    // --- NECK & HEAD ---
    addBox(0f, 1.0f, 0.02f, 0.28f, 0.16f, 0.28f, skinColor)

    val headRecoilPitch = if (fighter.hitRecoilY != 0f) fighter.hitRecoilY * 0.4f else 0f
    val headRecoilYaw = if (fighter.hitRecoilX != 0f) fighter.hitRecoilX * 0.5f else 0f
    addBox(headRecoilYaw * 0.1f, 1.25f + headRecoilPitch * 0.05f, 0.05f - headRecoilPitch * 0.1f, 0.48f, 0.52f, 0.50f, skinColor)
    addBox(0f, 1.35f, 0.30f, 0.42f, 0.14f, 0.08f, Color(0x33000000), shaded = false)

    // --- ARMS & GLOVES (CLINCH, INDEPENDENT GUARD & PHYSICAL EXTENSION) ---
    val action = fighter.actionState
    val punch = fighter.activePunch
    val progress = fighter.actionProgress
    val isBlock = fighter.isBlocking
    val isClinch = fighter.isClinching

    val punchExt = if (action == ActionState.PUNCHING && progress in 0f..1f) {
      sin(progress * Math.PI.toFloat())
    } else 0f

    var lShoulder = Vec3(-0.52f, 0.85f, 0.02f)
    var lElbow = Vec3(-0.48f, 0.58f, 0.22f)
    var lGlove = Vec3(-0.22f, 0.92f, 0.48f)

    var rShoulder = Vec3(0.52f, 0.85f, 0.02f)
    var rElbow = Vec3(0.48f, 0.58f, 0.22f)
    var rGlove = Vec3(0.22f, 0.92f, 0.48f)

    if (isClinch) {
      // Over-under clinch grappling pose
      lGlove = Vec3(0.18f, 1.10f, 0.95f)
      rGlove = Vec3(-0.18f, 1.10f, 0.95f)
      lElbow = Vec3(-0.38f, 0.90f, 0.55f)
      rElbow = Vec3(0.38f, 0.90f, 0.55f)
    } else if (isDown) {
      // Ragdoll limp sprawl
      lGlove = Vec3(-0.75f, 0.2f, 0.3f)
      rGlove = Vec3(0.75f, 0.2f, 0.3f)
      lElbow = Vec3(-0.65f, 0.3f, 0.1f)
      rElbow = Vec3(0.65f, 0.3f, 0.1f)
    } else {
      if (isBlock) {
        // High Guard defense
        lGlove = Vec3(-0.14f, 1.15f, 0.42f)
        rGlove = Vec3(0.14f, 1.15f, 0.42f)
        lElbow = Vec3(-0.25f, 0.65f, 0.30f)
        rElbow = Vec3(0.25f, 0.65f, 0.30f)
      }

      // Punch Kinematics (Independent arm extension!)
      punch?.let { p ->
        val maxReach = 1.35f * scaleR
        if (p.isLeftHand) {
          when {
            p == PunchType.JAB -> {
              lGlove = Vec3(-0.08f, 1.12f, 0.48f + punchExt * maxReach)
              lElbow = Vec3(-0.30f, 0.85f, 0.25f + punchExt * (maxReach * 0.5f))
            }
            p == PunchType.L_HOOK -> {
              val hookArc = sin(progress * Math.PI.toFloat())
              val hookTurn = cos(progress * Math.PI.toFloat())
              lGlove = Vec3(
                -0.65f + hookArc * 0.85f,
                1.10f,
                0.40f + hookArc * (maxReach * 0.7f) - hookTurn * 0.2f
              )
              lElbow = Vec3(-0.75f, 1.05f, 0.35f)
            }
            p == PunchType.L_UPPERCUT -> {
              lGlove = Vec3(
                -0.12f,
                0.55f + punchExt * 0.95f,
                0.45f + punchExt * (maxReach * 0.5f)
              )
              lElbow = Vec3(-0.35f, 0.45f, 0.28f)
            }
            p.isBody -> {
              lGlove = Vec3(-0.15f, 0.52f, 0.45f + punchExt * (maxReach * 0.9f))
              lElbow = Vec3(-0.35f, 0.42f, 0.25f)
            }
          }
        } else {
          when {
            p == PunchType.CROSS -> {
              rGlove = Vec3(0.08f, 1.12f, 0.48f + punchExt * (maxReach * 1.15f))
              rElbow = Vec3(0.28f, 0.85f, 0.25f + punchExt * (maxReach * 0.5f))
            }
            p == PunchType.R_HOOK -> {
              val hookArc = sin(progress * Math.PI.toFloat())
              val hookTurn = cos(progress * Math.PI.toFloat())
              rGlove = Vec3(
                0.65f - hookArc * 0.85f,
                1.10f,
                0.40f + hookArc * (maxReach * 0.75f) - hookTurn * 0.2f
              )
              rElbow = Vec3(0.75f, 1.05f, 0.35f)
            }
            p == PunchType.R_UPPERCUT -> {
              rGlove = Vec3(
                0.12f,
                0.55f + punchExt * 0.95f,
                0.45f + punchExt * (maxReach * 0.5f)
              )
              rElbow = Vec3(0.35f, 0.45f, 0.28f)
            }
            p.isBody -> {
              rGlove = Vec3(0.15f, 0.52f, 0.45f + punchExt * (maxReach * 0.9f))
              rElbow = Vec3(0.35f, 0.42f, 0.25f)
            }
          }
        }
      }
    }

    // Left Arm Render
    addBox(lShoulder.x, lShoulder.y, lShoulder.z, 0.32f, 0.28f, 0.32f, skinColor)
    val lBicepMid = (lShoulder + lElbow) * 0.5f
    addBox(lBicepMid.x, lBicepMid.y, lBicepMid.z, 0.24f, 0.32f, 0.24f, skinColor)
    val lForeMid = (lElbow + lGlove) * 0.5f
    addBox(lForeMid.x, lForeMid.y, lForeMid.z, 0.22f, 0.30f, 0.22f, skinColor)
    addBox(lGlove.x, lGlove.y, lGlove.z, 0.38f, 0.38f, 0.38f, gloveColor)
    addBox(lGlove.x, lGlove.y - 0.12f, lGlove.z, 0.34f, 0.10f, 0.34f, Color(0xFFF8FAFC))

    // Right Arm Render
    addBox(rShoulder.x, rShoulder.y, rShoulder.z, 0.32f, 0.28f, 0.32f, skinColor)
    val rBicepMid = (rShoulder + rElbow) * 0.5f
    addBox(rBicepMid.x, rBicepMid.y, rBicepMid.z, 0.24f, 0.32f, 0.24f, skinColor)
    val rForeMid = (rElbow + rGlove) * 0.5f
    addBox(rForeMid.x, rForeMid.y, rForeMid.z, 0.22f, 0.30f, 0.22f, skinColor)
    addBox(rGlove.x, rGlove.y, rGlove.z, 0.38f, 0.38f, 0.38f, gloveColor)
    addBox(rGlove.x, rGlove.y - 0.12f, rGlove.z, 0.34f, 0.10f, 0.34f, Color(0xFFF8FAFC))

    return polys
  }

  private fun createBox(
    cx: Float, cy: Float, cz: Float,
    w: Float, h: Float, d: Float,
    color: Color,
    edgeColor: Color? = null
  ): List<Polygon3D> {
    val hw = w / 2f
    val hh = h / 2f
    val hd = d / 2f

    val v000 = Vec3(cx - hw, cy - hh, cz - hd)
    val v100 = Vec3(cx + hw, cy - hh, cz - hd)
    val v110 = Vec3(cx + hw, cy + hh, cz - hd)
    val v010 = Vec3(cx - hw, cy + hh, cz - hd)

    val v001 = Vec3(cx - hw, cy - hh, cz + hd)
    val v101 = Vec3(cx + hw, cy - hh, cz + hd)
    val v111 = Vec3(cx + hw, cy + hh, cz + hd)
    val v011 = Vec3(cx - hw, cy + hh, cz + hd)

    return listOf(
      Polygon3D(listOf(v001, v101, v111, v011), color, true, edgeColor),
      Polygon3D(listOf(v100, v000, v010, v110), color, true, edgeColor),
      Polygon3D(listOf(v010, v011, v111, v110), color, true, edgeColor),
      Polygon3D(listOf(v000, v100, v101, v001), color, true, edgeColor),
      Polygon3D(listOf(v000, v001, v011, v010), color, true, edgeColor),
      Polygon3D(listOf(v101, v100, v110, v111), color, true, edgeColor)
    )
  }
}
