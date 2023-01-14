package com.arash.altafi.chatinputview.ext

import android.widget.ViewFlipper

fun ViewFlipper.go(index: Int) {
    this.displayedChild = index
}