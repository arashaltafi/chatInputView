package com.arash.altafi.chatinputview.ext

fun Boolean?.orFalse(): Boolean {
    return this ?: false
}