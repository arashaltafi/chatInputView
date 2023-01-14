package com.arash.altafi.chatinputview.ext

import android.annotation.TargetApi
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun TextView.clear() {
    this.text = ""
}

fun TextView.setDrawable(
    start: Int = 0, end: Int = 0,
    top: Int = 0, bottom: Int = 0
) {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(
        start, top,
        end, bottom
    )
}

fun TextView.setDrawableStart(res: Int) {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(
        res, 0,
        0, 0
    )
}

fun TextView.setDrawableStart(drawable: Drawable) {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(
        drawable, null, null, null
    )
}

fun TextView.setDrawableEnd(res: Int) {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(
        0, 0,
        res, 0
    )
}

fun TextView.setDrawableWithSize(
    @DrawableRes id: Int? = null,
    @DimenRes sizeRes: Int,
    gravity: Int = Gravity.START
) {
    val drawable =
        if (id != null) ContextCompat.getDrawable(context, id) else null
    val size = resources.getDimensionPixelSize(sizeRes)
    drawable?.setBounds(0, 0, size, size)
    when (gravity) {
        Gravity.RIGHT -> {
            this.setCompoundDrawables(null, null, drawable, null)
        }
        Gravity.LEFT -> {
            this.setCompoundDrawables(drawable, null, null, null)
        }
        Gravity.START -> {
            this.setCompoundDrawablesRelative(drawable, null, null, null)
        }
        Gravity.END -> {
            this.setCompoundDrawablesRelative(null, null, drawable, null)
        }
    }
}

fun TextView.toggleText(minLine: Int) {
    maxLines = if (maxLines == minLine) {
        lineCount
    } else {
        minLine
    }
}

fun TextView.setTextOrGone(text: String, alsoGone: View? = null) {
    if (text.isEmpty()) {
        alsoGone?.toGone()
        this.toGone()
    } else {
        alsoGone?.toShow()
        this.toShow()
        this.text = text
    }
}

fun TextView.addColon(withSpace: Boolean = false) {
    if (text.endsWith(":").not())
        if (withSpace)
            this.text = "%s :".applyValue(text)
        else
            this.text = "%s:".applyValue(text)
}

fun SpannableStringBuilder.setMultiSpan(start: Int, end: Int, vararg span: Any) {
    span.forEach {
        this.setSpan(
            it,
            start,
            end,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
    }
}

fun TextView.setTitle(
    text: String?,
    textColor: Int = context.getAttrColor(android.R.attr.textColorHint)
) {
    val titleWord: Spannable = SpannableString("$text ")

    titleWord.setSpan(
        ForegroundColorSpan(textColor),
        0,
        titleWord.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    append(titleWord)
}

fun TextView.setValue(
    text: String?,
    textColor: Int = context.getAttrColor(android.R.attr.textColor)
) {
    val valueWord: Spannable = SpannableString(text)

    valueWord.setSpan(
        ForegroundColorSpan(textColor),
        0,
        valueWord.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    append(valueWord)
}

fun TextView.setText(
    title: String?,
    value: String?,
    titleColor: Int = context.getAttrColor(android.R.attr.textColorHint),
    valueColor: Int = context.getAttrColor(android.R.attr.textColor)
) {
    val builder = SpannableStringBuilder()

    title?.let {
        val titleSpan = SpannableString("$title ")
        titleSpan.setSpan(
            ForegroundColorSpan(titleColor),
            0,
            titleSpan.length,
            SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE
        )
        builder.append(titleSpan)
    }

    value?.let {
        val valueSpan = SpannableString(value)
        valueSpan.setSpan(
            ForegroundColorSpan(valueColor),
            0,
            valueSpan.length,
            SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE
        )
        builder.append(valueSpan)
    }

    setText(builder, TextView.BufferType.SPANNABLE)
}


fun TextView.highlightAll(
    target: String,
    @ColorInt textColor: Int
) {

    val raw: Spannable = SpannableString(this.text)

    //remove background spans
    val spansBackground = raw.getSpans(
        0,
        raw.length,
        BackgroundColorSpan::class.java
    )
    for (span in spansBackground) {
        raw.removeSpan(span)
    }

    //remove foreground spans
    val spansForeground = raw.getSpans(
        0,
        raw.length,
        ForegroundColorSpan::class.java
    )
    for (span in spansForeground) {
        raw.removeSpan(span)
    }

    //set spans
    raw.toString().indexAll(target).forEach {
        raw.setSpan(
            ForegroundColorSpan(textColor),
            it,
            it + target.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    this.text = raw

}