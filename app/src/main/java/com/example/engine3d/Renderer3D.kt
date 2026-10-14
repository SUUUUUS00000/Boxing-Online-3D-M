package com.example.engine3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.max

object Renderer3D {

  private val lightDir = Vec3(0.35f, 0.85f, 0.4f).normalize()
  private const val AMBIENT_LIGHT = 0.52f
  private const val DIFFUSE_LIGHT = 0.48f

  fun renderScene(
    drawScope: DrawScope,
    polygons: List<Polygon3D>,
    cameraEye: Vec3,
    cameraTarget: Vec3,
    fovDegrees: Float = 55f
  ) {
    val width = drawScope.size.width
    val height = drawScope.size.height
    if (width <= 0f || height <= 0f) return

    val aspect = width / height
    val fovRad = Math.toRadians(fovDegrees.toDouble()).toFloat()

    val viewMatrix = Mat4.lookAt(cameraEye, cameraTarget, Vec3(0f, 1f, 0f))
    val projMatrix = Mat4.perspective(fovRad, aspect, 0.1f, 50f)
    val vpMatrix = projMatrix * viewMatrix

    val halfW = width * 0.5f
    val halfH = height * 0.5f

    val projectedList = ArrayList<ProjectedPoly>(polygons.size)

    for (i in polygons.indices) {
      val poly = polygons[i]
      val verts = poly.vertices
      if (verts.size < 3) continue

      var allInFront = true
      var sumZ = 0f
      val points2D = ArrayList<Offset>(verts.size)

      for (v in verts) {
        val clip = vpMatrix.transform(v)
        if (clip.w <= 0.05f) {
          allInFront = false
          break
        }
        val ndcX = clip.x / clip.w
        val ndcY = clip.y / clip.w
        val ndcZ = clip.z / clip.w

        // Convert NDC [-1, 1] to screen pixels [0, width]
        val screenX = halfW + ndcX * halfW
        val screenY = halfH - ndcY * halfH // Invert Y for screen coords

        points2D.add(Offset(screenX, screenY))
        sumZ += ndcZ
      }

      if (!allInFront || points2D.size < 3) continue

      // Compute 2D polygon area for backface culling
      var crossArea = 0f
      for (k in points2D.indices) {
        val curr = points2D[k]
        val next = points2D[(k + 1) % points2D.size]
        crossArea += (curr.x * next.y - next.x * curr.y)
      }

      // If polygon is facing away in 2D, we cull it (unless it's a double-sided floor/rope tag)
      if (crossArea < 0f && !poly.tag.startsWith("ring")) {
        continue
      }

      // Compute Lighting
      val finalColor = if (poly.isShaded) {
        val normal = poly.computeNormal()
        val intensity = (normal.dot(lightDir) * DIFFUSE_LIGHT).coerceAtLeast(0f) + AMBIENT_LIGHT
        val factor = intensity.coerceIn(0.2f, 1.15f)

        Color(
          red = (poly.color.red * factor).coerceIn(0f, 1f),
          green = (poly.color.green * factor).coerceIn(0f, 1f),
          blue = (poly.color.blue * factor).coerceIn(0f, 1f),
          alpha = poly.color.alpha
        )
      } else {
        poly.color
      }

      val avgZ = sumZ / verts.size
      projectedList.add(ProjectedPoly(points2D, avgZ, finalColor, poly.strokeColor, poly.tag))
    }

    // Sort by depth (Painter's algorithm: farthest polygons drawn first)
    projectedList.sortByDescending { it.avgZ }

    // Draw polygons to Canvas
    val path = Path()
    for (i in projectedList.indices) {
      val poly = projectedList[i]
      val pts = poly.points

      path.reset()
      path.moveTo(pts[0].x, pts[0].y)
      for (k in 1 until pts.size) {
        path.lineTo(pts[k].x, pts[k].y)
      }
      path.close()

      // Fill polygon face
      drawScope.drawPath(path, color = poly.color, style = Fill)

      // Stroke facet edges for distinct low-poly aesthetic
      poly.strokeColor?.let { strokeCol ->
        drawScope.drawPath(path, color = strokeCol, style = Stroke(width = 1.0f))
      }
    }
  }
}
