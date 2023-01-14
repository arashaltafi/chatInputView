package com.arash.altafi.chatinputview.ext

import kotlin.random.Random

fun IntRange.getRandom() =
    Random.nextInt(start, endInclusive + 1)