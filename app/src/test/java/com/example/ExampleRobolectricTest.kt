package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.BoxingPhysicsEngine
import com.example.model.ActionState
import com.example.model.CombatFighter
import com.example.model.FighterCustomization
import com.example.model.PunchType
import com.example.model.RankTier
import com.example.model.RankedProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context matches app name`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Boxing Online 3D", appName)
  }

  @Test
  fun `physics engine correctly initializes and executes player punches`() {
    val physics = BoxingPhysicsEngine()
    val playerCustom = FighterCustomization(name = "BOXER_1")
    val oppCustom = FighterCustomization(name = "BOXER_2")
    physics.resetMatch(playerCustom, oppCustom)

    val player = CombatFighter(customization = playerCustom, posX = 0f, posZ = -1.5f)
    val opp = CombatFighter(customization = oppCustom, posX = 0f, posZ = 0f)

    val success = physics.triggerPlayerPunch(player, PunchType.JAB)
    assertTrue("Punch should trigger successfully", success)
    assertEquals(ActionState.PUNCHING, player.actionState)
    assertEquals(PunchType.JAB, player.activePunch)

    // Simulate punch impact frame
    physics.update(dt = 0.2f, player = player, opponent = opp, moveX = 0f, moveZ = 0f)
    assertNotNull(player.activePunch)
  }

  @Test
  fun `ranked profile calculates correct tiers`() {
    assertEquals(RankTier.BRONZE, RankedProfile.calculateTier(500))
    assertEquals(RankTier.SILVER, RankedProfile.calculateTier(1250))
    assertEquals(RankTier.GOLD, RankedProfile.calculateTier(1700))
    assertEquals(RankTier.PLATINUM, RankedProfile.calculateTier(2300))
    assertEquals(RankTier.DIAMOND, RankedProfile.calculateTier(2900))
    assertEquals(RankTier.CHAMPION, RankedProfile.calculateTier(3500))
    assertEquals(RankTier.UNDISPUTED, RankedProfile.calculateTier(4200))
  }
}
