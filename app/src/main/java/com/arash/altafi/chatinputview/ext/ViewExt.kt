package com.arash.altafi.chatinputview.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout

fun View.showKeyboard() {
    this.requestFocus()
    try {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    } catch (e: java.lang.Exception) {
        "showKeyboard failed, error: $e".logE("showKeyboard")
    }
}

fun View.hideKeyboard() {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun View.toShow() {
    this.visibility = View.VISIBLE
}

fun View.isShow(): Boolean {
    return this.visibility == View.VISIBLE
}

fun View.toHide() {
    this.visibility = View.INVISIBLE
}

fun View.isHide(): Boolean {
    return this.visibility == View.INVISIBLE
}

fun View.toGone() {
    this.visibility = View.GONE
}

fun View.isGone(): Boolean {
    return this.visibility == View.GONE
}

fun View.smoothShow(duration: Long, endListener: ((animator: Animator) -> Unit)? = null) {
    if (this.isShow().not())
        this.toHide()
    animate().alpha(1f).setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                this@smoothShow.toShow()
            }

            override fun onAnimationEnd(animation: Animator) {
                endListener?.invoke(animation)
            }
        })
}

fun View.smoothHide(duration: Long, endListener: ((animator: Animator) -> Unit)? = null) {
    animate().alpha(0f).setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                this@smoothHide.toHide()
                endListener?.invoke(animation)
            }
        })
}

fun View.runIntervalHandler(totalRun: Int, durationMS: Long, action: () -> Unit) {
    var tick = totalRun
    postDelayed({
        action()
        tick--
        if (tick > 0) runIntervalHandler(tick, durationMS, action)
    }, durationMS)
}

fun View.enable() {
    this.isEnabled = true
}

fun View.disable() {
    this.isEnabled = false
}

fun View.toggleVisibility() {
    if (this.visibility == View.VISIBLE)
        this.toHide()
    else this.toShow()
}

fun View.toggleUsable() {
    this.isEnabled = !this.isEnabled
}

/*
    @author : naqqdi@gmail.com
    create dialogSheet and setting theme
 */

fun View.createDialogSheet(@StyleRes themeRes: Int): AppCompatDialog {
    val dialog = BottomSheetDialog(context, themeRes)
    dialog.setContentView(this)
    return dialog
}


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun View.setForegroundClickable() {
    val outValue = TypedValue()
    context.theme.resolveAttribute(
        android.R.attr.selectableItemBackgroundBorderless, outValue, true
    )
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        foreground = ContextCompat.getDrawable(context, outValue.resourceId)
    }
}

fun SearchView.disableAll() {
    val clearButton: ImageView =
        this.findViewById(androidx.appcompat.R.id.search_close_btn) as ImageView

    clearButton.isEnabled = false

    this.findViewById<TextView>(androidx.appcompat.R.id.search_src_text).isEnabled = false
    this.isSubmitButtonEnabled = false
}

@SuppressLint("ClickableViewAccessibility")
fun TabLayout.disableAll() {
    this.getChildAt(0)?.cast<LinearLayout>()?.children?.forEach {
        it.setOnTouchListener { _, _ ->
            true
        }
    }
}

fun View.setMarginStart(margin: Int) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val p = layoutParams as ViewGroup.MarginLayoutParams
        p.marginStart = margin
        requestLayout()
    }
}

fun View.setMarginEnd(margin: Int) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val p = layoutParams as ViewGroup.MarginLayoutParams
        p.marginEnd = margin
        requestLayout()
    }
}

fun View.setMarginHorizontal(marginStart: Int, marginEnd: Int) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val p = layoutParams as ViewGroup.MarginLayoutParams
        p.marginStart = marginStart
        p.marginEnd = marginEnd
        requestLayout()
    }
}

fun ConstraintLayout.addToCenter(theView: View, isMatchParent: Boolean = false) {

    val oldLoadingView = this.cast<ViewGroup>()?.findViewWithTag<View>(theView.tag)

    if (oldLoadingView == null) {
        val sizeType = if (isMatchParent) ConstraintLayout.LayoutParams.MATCH_PARENT
        else ConstraintLayout.LayoutParams.WRAP_CONTENT

        val layoutParams = ConstraintLayout.LayoutParams(
            sizeType,
            sizeType
        )

        layoutParams.bottomToBottom = ConstraintSet.PARENT_ID
        layoutParams.endToEnd = ConstraintSet.PARENT_ID
        layoutParams.startToStart = ConstraintSet.PARENT_ID
        layoutParams.topToTop = ConstraintSet.PARENT_ID

        theView.layoutParams = layoutParams

        addView(theView, -1)
    } else {
        oldLoadingView.toShow()
    }

}

fun RelativeLayout.addToCenter(theView: View, isMatchParent: Boolean = false) = this.run {

    val oldLoadingView = this.cast<ViewGroup>()?.findViewWithTag<View>(theView.tag)

    if (oldLoadingView == null) {
        val sizeType = if (isMatchParent) RelativeLayout.LayoutParams.MATCH_PARENT
        else RelativeLayout.LayoutParams.WRAP_CONTENT
        val params = RelativeLayout.LayoutParams(
            sizeType,
            sizeType
        )
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)

        theView.layoutParams = params

        addView(theView, -1)
    } else {
        oldLoadingView.toShow()
    }

}

fun CoordinatorLayout.addToCenter(theView: View, isMatchParent: Boolean = false) = this.run {

    val oldLoadingView = this.cast<ViewGroup>()?.findViewWithTag<View>(theView.tag)

    if (oldLoadingView == null) {
        val sizeType = if (isMatchParent) RelativeLayout.LayoutParams.MATCH_PARENT
        else RelativeLayout.LayoutParams.WRAP_CONTENT
        val params = CoordinatorLayout.LayoutParams(
            sizeType,
            sizeType
        )
        params.gravity = Gravity.CENTER

        theView.layoutParams = params
        addView(theView, -1)
    } else {
        oldLoadingView.toShow()
    }

}

fun ScrollView.addToCenter(theView: View, isMatchParent: Boolean = false) = this.run {

    when (val firstRootLayout = this.getChildAt(0)) {
        is ConstraintLayout -> {
            firstRootLayout.addToCenter(theView, isMatchParent)
        }
        is RelativeLayout -> {
            firstRootLayout.addToCenter(theView, isMatchParent)
        }
        is CoordinatorLayout -> {
            firstRootLayout.addToCenter(theView, isMatchParent)
        }
        else -> {
            "ScrollView.addToCenter error".logE("addView")
            throw Exception("ScrollView root-view should be \'ConstraintLayout\' or \'RelativeLayout\' or \'CoordinatorLayout\'")
        }
    }

}

fun NestedScrollView.addToCenter(theView: View, isMatchParent: Boolean = false) = this.run {

    when (val firstRootLayout = this.getChildAt(0)) {
        is ConstraintLayout -> {
            firstRootLayout.addToCenter(theView, isMatchParent)
        }
        is RelativeLayout -> {
            firstRootLayout.addToCenter(theView, isMatchParent)
        }
        is CoordinatorLayout -> {
            firstRootLayout.addToCenter(theView, isMatchParent)
        }
        else -> {
            "NestedScrollView.addToCenter error".logE("addView")
            throw Exception("ScrollView root-view should be \'ConstraintLayout\' or \'RelativeLayout\' or \'CoordinatorLayout\'")
        }
    }

}