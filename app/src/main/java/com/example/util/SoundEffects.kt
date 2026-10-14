package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural low-latency sound effects and haptics for boxing strikes, blocks,
 * ring bell, and referee counts without external asset dependencies.
 */
class SoundEffects(context: Context) {
  private val appContext = context.applicationContext
  private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  private val audioScope = CoroutineScope(Dispatchers.Default)

  var soundEnabled: Boolean = true
  var hapticsEnabled: Boolean = true

  fun playWhoosh() {
    if (!soundEnabled) return
    playSynthTone(frequency = 180f, endFrequency = 80f, durationMs = 90, volume = 0.4f, noiseMix = 0.5f)
  }

  fun playHit(isHeavy: Boolean = false) {
    if (hapticsEnabled) {
      triggerVibration(if (isHeavy) 45 else 25, if (isHeavy) 255 else 180)
    }
    if (!soundEnabled) return
    if (isHeavy) {
      playSynthTone(frequency = 120f, endFrequency = 45f, durationMs = 160, volume = 0.9f, noiseMix = 0.6f)
    } else {
      playSynthTone(frequency = 210f, endFrequency = 70f, durationMs = 110, volume = 0.7f, noiseMix = 0.4f)
    }
  }

  fun playBlock() {
    if (hapticsEnabled) {
      triggerVibration(15, 120)
    }
    if (!soundEnabled) return
    playSynthTone(frequency = 320f, endFrequency = 140f, durationMs = 80, volume = 0.5f, noiseMix = 0.2f)
  }

  fun playBell() {
    if (!soundEnabled) return
    playSynthTone(frequency = 880f, endFrequency = 860f, durationMs = 600, volume = 0.8f, decay = 0.98f)
  }

  fun playRefereeCount() {
    if (!soundEnabled) return
    playSynthTone(frequency = 440f, endFrequency = 440f, durationMs = 180, volume = 0.6f)
  }

  fun playKnockdown() {
    if (hapticsEnabled) {
      triggerVibration(120, 255)
    }
    if (!soundEnabled) return
    playSynthTone(frequency = 80f, endFrequency = 30f, durationMs = 450, volume = 1.0f, noiseMix = 0.7f)
  }

  private fun triggerVibration(durationMs: Long, amplitude: Int) {
    try {
      vibrator?.let { v ->
        if (v.hasVibrator()) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
          } else {
            @Suppress("DEPRECATION")
            v.vibrate(durationMs)
          }
        }
      }
    } catch (_: Exception) {}
  }

  private fun playSynthTone(
    frequency: Float,
    endFrequency: Float,
    durationMs: Int,
    volume: Float = 0.7f,
    noiseMix: Float = 0f,
    decay: Float = 0.94f
  ) {
    audioScope.launch {
      try {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(100)
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        var currentVol = volume

        for (i in 0 until numSamples) {
          val progress = i.toFloat() / numSamples
          val curFreq = frequency + (endFrequency - frequency) * progress
          val phaseIncrement = (2.0 * Math.PI * curFreq) / sampleRate
          phase += phaseIncrement

          val sine = sin(phase)
          val noise = if (noiseMix > 0f) (Math.random() * 2.0 - 1.0) * noiseMix else 0.0
          val sample = ((sine * (1.0 - noiseMix) + noise) * currentVol * Short.MAX_VALUE).toInt()
          buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

          currentVol *= decay
        }

        val minBufSize = AudioTrack.getMinBufferSize(
          sampleRate,
          AudioFormat.CHANNEL_OUT_MONO,
          AudioFormat.ENCODING_PCM_16BIT
        )
        val audioTrack = AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_GAME)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(minBufSize.coerceAtLeast(numSamples * 2))
          .setTransferMode(AudioTrack.MODE_STATIC)
          .build()

        audioTrack.write(buffer, 0, numSamples)
        audioTrack.play()
        kotlinx.coroutines.delay(durationMs.toLong() + 50)
        audioTrack.release()
      } catch (_: Exception) {}
    }
  }
}
