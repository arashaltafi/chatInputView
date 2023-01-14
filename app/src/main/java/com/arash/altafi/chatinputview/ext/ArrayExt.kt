package com.arash.altafi.chatinputview.ext

fun <T> Array<out T>.joinToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): String {
    return joinTo(
        StringBuilder(),
        separator,
        prefix,
        postfix,
        limit,
        truncated,
        transform
    ).toString()
}