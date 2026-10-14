package com.example.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object BoxingIcons {

  val Coin: ImageVector by lazy {
    ImageVector.Builder(
      name = "Coin",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      // Outer rim
      path(
        fill = SolidColor(Color(0xFFF59E0B)),
        fillAlpha = 1.0f
      ) {
        moveTo(12f, 2f)
        curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
        curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
        curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
        close()
      }
      // Inner circle
      path(
        fill = SolidColor(Color(0xFFFBBF24))
      ) {
        moveTo(12f, 4.5f)
        curveTo(7.86f, 4.5f, 4.5f, 7.86f, 4.5f, 12f)
        curveTo(4.5f, 16.14f, 7.86f, 19.5f, 12f, 19.5f)
        curveTo(16.14f, 19.5f, 19.5f, 16.14f, 19.5f, 12f)
        curveTo(19.5f, 7.86f, 16.14f, 4.5f, 12f, 4.5f)
        close()
      }
      // Star / Emblem in center
      path(
        fill = SolidColor(Color(0xFFD97706))
      ) {
        moveTo(12f, 7f)
        lineTo(13.2f, 10.3f)
        lineTo(16.8f, 10.5f)
        lineTo(14f, 12.6f)
        lineTo(15f, 16f)
        lineTo(12f, 14.1f)
        lineTo(9f, 16f)
        lineTo(10f, 12.6f)
        lineTo(7.2f, 10.5f)
        lineTo(10.8f, 10.3f)
        close()
      }
    }.build()
  }

  val Flame: ImageVector by lazy {
    ImageVector.Builder(
      name = "Flame",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(
        fill = SolidColor(Color(0xFFF97316))
      ) {
        moveTo(12f, 2f)
        curveTo(10f, 6f, 7f, 9f, 7f, 13f)
        curveTo(7f, 17f, 9.5f, 21f, 13.5f, 21.9f)
        curveTo(17f, 21.5f, 19f, 18f, 19f, 14f)
        curveTo(19f, 10f, 15f, 6.5f, 15f, 2f)
        curveTo(13.5f, 4f, 13f, 5.5f, 13.5f, 7f)
        curveTo(11f, 5f, 11f, 3.5f, 12f, 2f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFFFDE047))
      ) {
        moveTo(12f, 12f)
        curveTo(10.5f, 14.5f, 10.5f, 16.5f, 12f, 19.5f)
        curveTo(14.5f, 19f, 15.5f, 17f, 15.5f, 15f)
        curveTo(15.5f, 13.5f, 13.5f, 12.5f, 12f, 12f)
        close()
      }
    }.build()
  }

  val BoxingGlove: ImageVector by lazy {
    ImageVector.Builder(
      name = "BoxingGlove",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      // Main glove padding
      path(
        fill = SolidColor(Color(0xFFEF4444))
      ) {
        moveTo(9f, 2f)
        curveTo(5.5f, 2f, 3f, 5f, 3f, 8.5f)
        curveTo(3f, 12.5f, 5f, 15.5f, 8.5f, 16.5f)
        lineTo(8.5f, 19f)
        lineTo(15.5f, 19f)
        lineTo(15.5f, 16.5f)
        curveTo(18.5f, 15f, 21f, 12f, 21f, 8.5f)
        curveTo(21f, 4.5f, 18f, 2f, 14f, 2f)
        close()
      }
      // Thumb portion
      path(
        fill = SolidColor(Color(0xFFDC2626))
      ) {
        moveTo(4f, 9f)
        curveTo(2.5f, 10f, 2f, 12f, 3f, 13.5f)
        curveTo(4f, 15f, 6.5f, 15.5f, 8f, 14.5f)
        close()
      }
      // Wrist cuff
      path(
        fill = SolidColor(Color(0xFFF8FAFC))
      ) {
        moveTo(8f, 19f)
        lineTo(16f, 19f)
        lineTo(16f, 22f)
        lineTo(8f, 22f)
        close()
      }
    }.build()
  }

  val Crown: ImageVector by lazy {
    ImageVector.Builder(
      name = "Crown",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(
        fill = SolidColor(Color(0xFFF59E0B))
      ) {
        moveTo(3f, 18f)
        lineTo(21f, 18f)
        lineTo(21f, 21f)
        lineTo(3f, 21f)
        close()
        moveTo(3f, 16f)
        lineTo(5f, 7f)
        lineTo(9.5f, 12f)
        lineTo(12f, 4f)
        lineTo(14.5f, 12f)
        lineTo(19f, 7f)
        lineTo(21f, 16f)
        close()
      }
    }.build()
  }

  val DiamondGem: ImageVector by lazy {
    ImageVector.Builder(
      name = "DiamondGem",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(
        fill = SolidColor(Color(0xFF38BDF8))
      ) {
        moveTo(6f, 3f)
        lineTo(18f, 3f)
        lineTo(22f, 9f)
        lineTo(12f, 22f)
        lineTo(2f, 9f)
        close()
      }
      path(
        fill = SolidColor(Color(0xFF7DD3FC))
      ) {
        moveTo(9f, 3f)
        lineTo(15f, 3f)
        lineTo(17f, 9f)
        lineTo(12f, 20f)
        lineTo(7f, 9f)
        close()
      }
    }.build()
  }

  val TrophyCup: ImageVector by lazy {
    ImageVector.Builder(
      name = "TrophyCup",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(
        fill = SolidColor(Color(0xFFFBBF24))
      ) {
        moveTo(6f, 3f)
        lineTo(18f, 3f)
        lineTo(18f, 10f)
        curveTo(18f, 13.3f, 15.3f, 16f, 12f, 16f)
        curveTo(8.7f, 16f, 6f, 13.3f, 6f, 10f)
        close()
        // Handles
        moveTo(6f, 5f)
        lineTo(3f, 5f)
        curveTo(2f, 5f, 2f, 8f, 3f, 10f)
        curveTo(4f, 12f, 6f, 12f, 6f, 12f)
        close()
        moveTo(18f, 5f)
        lineTo(21f, 5f)
        curveTo(22f, 5f, 22f, 8f, 21f, 10f)
        curveTo(20f, 12f, 18f, 12f, 18f, 12f)
        close()
        // Base
        moveTo(10f, 16f)
        lineTo(14f, 16f)
        lineTo(14f, 19f)
        lineTo(18f, 19f)
        lineTo(18f, 21f)
        lineTo(6f, 21f)
        lineTo(6f, 19f)
        lineTo(10f, 19f)
        close()
      }
    }.build()
  }

  val MedalBadge: ImageVector by lazy {
    ImageVector.Builder(
      name = "MedalBadge",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      // Ribbons
      path(
        fill = SolidColor(Color(0xFF3B82F6))
      ) {
        moveTo(7f, 2f)
        lineTo(12f, 9f)
        lineTo(10f, 13f)
        lineTo(5f, 6f)
        close()
        moveTo(17f, 2f)
        lineTo(12f, 9f)
        lineTo(14f, 13f)
        lineTo(19f, 6f)
        close()
      }
      // Circular medal
      path(
        fill = SolidColor(Color(0xFFF59E0B))
      ) {
        moveTo(12f, 10f)
        curveTo(8.7f, 10f, 6f, 12.7f, 6f, 16f)
        curveTo(6f, 19.3f, 8.7f, 22f, 12f, 22f)
        curveTo(15.3f, 22f, 18f, 19.3f, 18f, 16f)
        curveTo(18f, 12.7f, 15.3f, 10f, 12f, 10f)
        close()
      }
    }.build()
  }

  val ClinchPush: ImageVector by lazy {
    ImageVector.Builder(
      name = "ClinchPush",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(
        fill = SolidColor(Color(0xFF38BDF8))
      ) {
        moveTo(4f, 6f)
        lineTo(9f, 6f)
        lineTo(11f, 11f)
        lineTo(8f, 14f)
        lineTo(5f, 11f)
        close()
        moveTo(20f, 6f)
        lineTo(15f, 6f)
        lineTo(13f, 11f)
        lineTo(16f, 14f)
        lineTo(19f, 11f)
        close()
        moveTo(10f, 15f)
        lineTo(14f, 15f)
        lineTo(14f, 21f)
        lineTo(10f, 21f)
        close()
      }
    }.build()
  }

  val Lightning: ImageVector by lazy {
    ImageVector.Builder(
      name = "Lightning",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(
        fill = SolidColor(Color(0xFFFBBF24))
      ) {
        moveTo(13f, 2f)
        lineTo(4f, 13f)
        lineTo(11f, 13f)
        lineTo(9f, 22f)
        lineTo(20f, 10f)
        lineTo(13f, 10f)
        close()
      }
    }.build()
  }
}
