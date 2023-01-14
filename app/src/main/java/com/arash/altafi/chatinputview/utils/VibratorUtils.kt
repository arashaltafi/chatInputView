package com.arash.altafi.chatinputview.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object VibratorUtils {

    fun vibrate(context: Context, time: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        time,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            else
                vibrator.vibrate(time)
        }
    }

    /**
     * your device has support vibrator???
     */
    fun canVibrate(context: Context): Boolean {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        return vibrator.hasVibrator()
    }

}