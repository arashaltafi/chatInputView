package com.arash.altafi.chatinputview.ext

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle

/**
 * @param launcher: usually start activity
 */
fun <T> Activity.restartApp(launcher: Class<T>, bundle: Bundle? = null) {
    Intent(this, launcher).apply {
        bundle?.let {
            putExtras(it)
        }
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(this)
    }

    finish()
    Runtime.getRuntime().exit(0)
}

fun Activity.isDarkTheme(): Boolean {
    return this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}