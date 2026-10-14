package com.example.engine3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.*

data class Vec3(val x: Float, val y: Float, val z: Float) {
  operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
  operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
  operator fun times(scalar: Float) = Vec3(x * scalar, y * scalar, z * scalar)
  operator fun div(scalar: Float) = Vec3(x / scalar, y / scalar, z / scalar)

  fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z

  fun cross(other: Vec3): Vec3 = Vec3(
    y * other.z - z * other.y,
    z * other.x - x * other.z,
    x * other.y - y * other.x
  )

  fun length(): Float = sqrt(x * x + y * y + z * z)

  fun normalize(): Vec3 {
    val len = length()
    return if (len > 0.0001f) this / len else Vec3(0f, 1f, 0f)
  }
}

class Mat4(val m: FloatArray = FloatArray(16) { if (it % 5 == 0) 1f else 0f }) {
  companion object {
    fun identity(): Mat4 = Mat4()

    fun translation(tx: Float, ty: Float, tz: Float): Mat4 {
      val res = Mat4()
      res.m[12] = tx
      res.m[13] = ty
      res.m[14] = tz
      return res
    }

    fun scale(sx: Float, sy: Float, sz: Float): Mat4 {
      val res = Mat4()
      res.m[0] = sx
      res.m[5] = sy
      res.m[10] = sz
      return res
    }

    fun rotationY(angleRad: Float): Mat4 {
      val res = Mat4()
      val c = cos(angleRad)
      val s = sin(angleRad)
      res.m[0] = c
      res.m[2] = s
      res.m[8] = -s
      res.m[10] = c
      return res
    }

    fun rotationX(angleRad: Float): Mat4 {
      val res = Mat4()
      val c = cos(angleRad)
      val s = sin(angleRad)
      res.m[5] = c
      res.m[6] = -s
      res.m[9] = s
      res.m[10] = c
      return res
    }

    fun rotationZ(angleRad: Float): Mat4 {
      val res = Mat4()
      val c = cos(angleRad)
      val s = sin(angleRad)
      res.m[0] = c
      res.m[1] = -s
      res.m[4] = s
      res.m[5] = c
      return res
    }

    fun perspective(fovYRad: Float, aspect: Float, near: Float, far: Float): Mat4 {
      val res = Mat4(FloatArray(16) { 0f })
      val tanHalfFov = tan(fovYRad / 2f)
      res.m[0] = 1f / (aspect * tanHalfFov)
      res.m[5] = 1f / tanHalfFov
      res.m[10] = -(far + near) / (far - near)
      res.m[11] = -1f
      res.m[14] = -(2f * far * near) / (far - near)
      return res
    }

    fun lookAt(eye: Vec3, center: Vec3, up: Vec3): Mat4 {
      val f = (center - eye).normalize()
      val s = f.cross(up).normalize()
      val u = s.cross(f)

      val res = Mat4()
      res.m[0] = s.x
      res.m[4] = s.y
      res.m[8] = s.z
      res.m[1] = u.x
      res.m[5] = u.y
      res.m[9] = u.z
      res.m[2] = -f.x
      res.m[6] = -f.y
      res.m[10] = -f.z
      res.m[12] = -s.dot(eye)
      res.m[13] = -u.dot(eye)
      res.m[14] = f.dot(eye)
      return res
    }
  }

  operator fun times(other: Mat4): Mat4 {
    val res = FloatArray(16)
    for (row in 0..3) {
      for (col in 0..3) {
        var sum = 0f
        for (k in 0..3) {
          sum += this.m[row + k * 4] * other.m[k + col * 4]
        }
        res[row + col * 4] = sum
      }
    }
    return Mat4(res)
  }

  fun transform(v: Vec3): Vec4 {
    val x = m[0] * v.x + m[4] * v.y + m[8] * v.z + m[12]
    val y = m[1] * v.x + m[5] * v.y + m[9] * v.z + m[13]
    val z = m[2] * v.x + m[6] * v.y + m[10] * v.z + m[14]
    val w = m[3] * v.x + m[7] * v.y + m[11] * v.z + m[15]
    return Vec4(x, y, z, w)
  }
}

data class Vec4(val x: Float, val y: Float, val z: Float, val w: Float)

data class Polygon3D(
  val vertices: List<Vec3>,
  val color: Color,
  val isShaded: Boolean = true,
  val strokeColor: Color? = null,
  val tag: String = ""
) {
  fun computeNormal(): Vec3 {
    if (vertices.size < 3) return Vec3(0f, 1f, 0f)
    val v0 = vertices[0]
    val v1 = vertices[1]
    val v2 = vertices[2]
    return (v1 - v0).cross(v2 - v0).normalize()
  }
}

data class ProjectedPoly(
  val points: List<Offset>,
  val avgZ: Float,
  val color: Color,
  val strokeColor: Color?,
  val tag: String
)
