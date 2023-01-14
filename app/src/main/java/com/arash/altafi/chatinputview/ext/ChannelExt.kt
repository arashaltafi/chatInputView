package com.arash.altafi.chatinputview.ext

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*

fun <T> mergeToFlow(
    vararg channels: ReceiveChannel<T>
) = merge(
    *channels.map {
        it.receiveAsFlow()
    }.toTypedArray()
).buffer()