package com.arash.altafi.chatinputview.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewAnimationUtils
import kotlin.math.hypot

/*
    @author: amir abbas
    Reveal animations provide users visual continuity when you show or hide a group of UI elements.
    The ViewAnimationUtils.createCircularReveal() method
        enables you to animate a clipping circle to reveal or hide a view.
    This animation is provided in the ViewAnimationUtils class, which is available
        for Android 5.0 (API level 21) and higher.
        More : https://developer.android.com/training/animation/reveal-or-hide-view#Reveal
 */

enum class RevealModel {
    START,
    CENTER,
    END
}

fun View.reveal(
    duration: Long, model: RevealModel,
    endListener: (() -> Unit)? = null
) {
    val cxF = when (model) {
        RevealModel.START -> width
        RevealModel.CENTER -> width / 2
        else -> 0
    }

    val cyF = height / 2

    val radius = hypot(width.toDouble(), height.toDouble()).toFloat()

    ViewAnimationUtils.createCircularReveal(
        this, cxF, cyF, 0f, radius
    ).apply {
        setDuration(duration)
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                toShow()
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                enable()
                endListener?.invoke()
            }
        })
    }.start()
}

fun View.unReveal(
    duration: Long, model: RevealModel,
    endListener: (() -> Unit)? = null
) {

    val cxF = when (model) {
        RevealModel.START -> right
        RevealModel.CENTER -> width / 2
        else -> left
    }

    val cyF = height / 2

    val radius = hypot(width.toDouble(), height.toDouble()).toFloat()

    ViewAnimationUtils.createCircularReveal(
        this, cxF, cyF, radius, 0f
    ).apply {
        setDuration(duration)
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                disable()
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                toHide()
                endListener?.invoke()
            }
        })
    }.start()
}