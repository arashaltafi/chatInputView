package com.arash.altafi.chatinputview.ext

import android.content.res.TypedArray
import java.util.*


fun <T> Collection<T>.logListE(tag: String = "") {
    this.forEach {
        it?.logE(tag)
    }
}

fun <T> Array<T>.logArrayE(tag: String = "") {
    this.forEach {
        it?.logE(tag)
    }
}

fun <K, V> Map<*, *>.logMapE(tag: String = "") {
    this.forEach {
        this.logE("$tag${it.key}: ${it.value}")
    }
}

fun <T> List<T>?.letIfNotEmptyOrNull(let: (it: List<T>) -> Unit): List<T>? {
    this.takeIf { !it.isNullOrEmpty() }
        ?.let {
            let.invoke(it)
            return it
        }
    return null
}

fun List<Any>.isEqual(second: List<Any>): Boolean {
    if (this.size != second.size) {
        return false
    }

    return this.zip(second).all { (x, y) -> x == y }
}

inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, default: T) =
    getInt(index, -1).let {
        if (it >= 0) enumValues<T>()[it] else default
    }


fun <T> Collection<T>.toVector(): Vector<T> {
    return Vector<T>(this.size).apply {
        this.addAll(this@toVector)
    }
}

fun <T> Collection<T>.toLinkedSet(): LinkedHashSet<T> {
    return linkedSetOf<T>().apply {
        addAll(this@toLinkedSet)
    }
}