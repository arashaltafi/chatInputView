package com.arash.altafi.chatinputview.ext

fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}