package com.arash.altafi.chatinputview.ext

import android.graphics.Color
import androidx.annotation.ColorInt
import java.util.concurrent.TimeUnit


fun Int.toHexColor(): String {
    return "#" + Integer.toHexString(this).substring(2)
}

fun Int.toBoolean(): Boolean {
    return this == 1
}

@ColorInt
fun Int.withAlpha(alpha: Float): Int {
    var alphaa: Int = Color.alpha(this)
    val red: Int = Color.red(this)
    val green: Int = Color.green(this)
    val blue: Int = Color.blue(this)

    alphaa = (alpha * alphaa).toInt()

    return Color.argb(alphaa, red, green, blue)
}

fun Long.convertDurationToTime(): String {
    val convertHours = java.lang.String.format(
        "%02d", TimeUnit.MILLISECONDS.toHours(this)
    )
    val convertMinutes = java.lang.String.format(
        "%02d", TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(this)
        )
    )
    val convertSeconds = java.lang.String.format(
        "%02d", TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(this)
        )
    )
    return if (this > 3600000) "$convertHours:$convertMinutes:$convertSeconds" else "$convertMinutes:$convertSeconds"
}