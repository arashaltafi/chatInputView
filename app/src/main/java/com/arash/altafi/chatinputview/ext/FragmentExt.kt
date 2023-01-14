package com.arash.altafi.chatinputview.ext

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment

fun Fragment.toast(msg: String) {
    requireContext().toast(msg)
}

private fun showItem(
    @LayoutRes layout: Int,
    rootView: View?,
    layoutInflater: LayoutInflater,
    toShow: Boolean,
    isMatchParent: Boolean = false
) {
    rootView?.cast<ViewGroup>()?.apply {
        val viewLayout = layoutInflater.inflate(layout, null)
        if (isMatchParent)
            viewLayout.setBackgroundColor(context.getAttrColor(android.R.attr.colorBackground))

        if (toShow) {
            val isAdded = addToCenter(this, viewLayout, isMatchParent)
            if (isAdded.not()) {
                "error show".logE("addView")
                throw Exception("should run on \'ConstraintLayout\' or \'RelativeLayout\' or \'ScrollView\' or \'CoordinatorLayout\'")
            }
        } else {
            this.cast<ViewGroup>()?.findViewWithTag<View>(viewLayout.tag)?.toGone()
        }

    }
}

private fun addToCenter(rootView: View, newView: View, isMatchParent: Boolean = false): Boolean =
    when (rootView) {
        is ConstraintLayout -> {
            rootView.addToCenter(newView, isMatchParent)
            true
        }
        is RelativeLayout -> {
            rootView.addToCenter(newView, isMatchParent)
            true
        }
        is CoordinatorLayout -> {
            rootView.addToCenter(newView, isMatchParent)
            true
        }
        is ScrollView -> {
            rootView.addToCenter(newView, isMatchParent)
            true
        }
        is NestedScrollView -> {
            rootView.addToCenter(newView, isMatchParent)
            true
        }
        else -> {
            false
        }
    }

fun Fragment.onBackPressed(isEnable: Boolean = false,onBackPressed: (() -> Unit)? = null){
    requireActivity().onBackPressedDispatcher.addCallback(
        this, object : OnBackPressedCallback(isEnable) {
            override fun handleOnBackPressed() {
                onBackPressed?.invoke()
            }
        })
}