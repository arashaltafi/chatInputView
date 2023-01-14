package com.arash.altafi.chatinputview.ext

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.arash.altafi.chatinputview.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.arash.altafi.chatinputview.utils.glide.GlideUtils
import jp.wasabeef.glide.transformations.BlurTransformation
import java.io.File

fun ImageView.clear() {
    this.setImageDrawable(null)
}

fun ImageView.loadCompat(
    url: Any,
    @DrawableRes placeholderRes: Int? = R.drawable.bit_placeholder_image,
    @DrawableRes errorRes: Int? = R.drawable.bit_error_image,
    requestOptions: RequestOptions? = null
) {
    if (url is String && url.toString().contains(".svg"))
        loadSVG(url, placeholderRes, errorRes, requestOptions)
    else loadDrawable(url, placeholderRes, errorRes, requestOptions)
}

@SuppressLint("CheckResult")
private fun ImageView.loadSVG(
    url: Any,
    @DrawableRes placeholderRes: Int? = R.drawable.bit_placeholder_image,
    @DrawableRes errorRes: Int? = R.drawable.bit_error_image,
    requestOptions: RequestOptions? = null
) {
    GlideUtils(context).getSVGRequestBuilder(requestOptions)
        .load(url)
        .apply {
            placeholderRes?.let { placeholder(it) }
            errorRes?.let { error(it) }
        }.into(this)
}


private fun ImageView.loadDrawable(
    url: Any,
    @DrawableRes placeholderRes: Int? = R.drawable.bit_placeholder_image,
    @DrawableRes errorRes: Int? = R.drawable.bit_error_image,
    requestOptions: RequestOptions? = null
) {
    GlideUtils(context).getDrawableRequestBuilder(requestOptions)
        .load(url)
        .apply {
            placeholderRes?.let { placeholder(it) }
            errorRes?.let { error(it) }
        }.into(this)
}

fun Context.getBitmap(
    url: Any,
    result: ((Bitmap) -> Unit),
    @DrawableRes placeholderRes: Int? = R.drawable.bit_placeholder_image,
    @DrawableRes errorRes: Int? = R.drawable.bit_error_image,
    requestOptions: RequestOptions? = null
) {
    GlideUtils(this).getBitmapRequestBuilder(requestOptions)
        .load(url)
        .apply {
            placeholderRes?.let { placeholder(it) }
            error(errorRes)
        }
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                result.invoke(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }

        })

}

fun FloatingActionButton.setIcon(icon: Int) = setImageResource(icon)

fun FloatingActionButton.setColor(color: Int) {
    imageTintList = ColorStateList.valueOf(color)
}

fun ShapeableImageView.setColor(colorStateList: ColorStateList) {
    imageTintList = colorStateList
}

fun ImageView.setBlurImage(url: File) {
    Glide.with(this)
        .load(url)
        .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
        .into(this)
}

fun ImageView.setBlurImage(url: String) {
    Glide.with(this)
        .load(url)
        .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
        .into(this)
}

fun ImageView.setBlurImage(@DrawableRes url: Int) {
    Glide.with(this)
        .load(url)
        .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
        .into(this)
}