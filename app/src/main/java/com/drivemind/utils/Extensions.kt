package com.drivemind.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.animation.AnimationUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Haptic feedback: short pulse for mic tap, double pulse for save.
 */
fun Context.vibrateShort() {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(60)
    }
}

fun Context.vibrateSuccess() {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val pattern = VibrationEffect.createWaveform(longArrayOf(0, 60, 80, 60), -1)
        vibrator.vibrate(pattern)
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(longArrayOf(0, 60, 80, 60), -1)
    }
}

/**
 * Timestamp formatting helpers.
 */
fun Long.toRelativeTime(): String {
    val diffMs = System.currentTimeMillis() - this
    return when {
        diffMs < 60_000L -> "Just now"
        diffMs < 3_600_000L -> "${diffMs / 60_000} min ago"
        diffMs < 86_400_000L -> "${diffMs / 3_600_000}h ago"
        diffMs < 604_800_000L -> SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date(this))
        else -> SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(this))
    }
}

fun Long.toFullDateTime(): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).format(Date(this))

/**
 * View visibility helpers.
 */
fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.showIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

/**
 * Pulse animation for the mic button.
 */
fun View.startPulseAnimation(context: Context) {
    val pulse = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
    startAnimation(pulse)
}
