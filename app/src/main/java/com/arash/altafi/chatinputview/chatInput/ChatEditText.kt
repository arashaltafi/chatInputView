package com.arash.altafi.chatinputview.chatInput

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.google.android.material.textfield.TextInputEditText

class ChatEditText(context: Context, attrs: AttributeSet?) : TextInputEditText(context, attrs) {

    private var keyImeChangeListener: KeyImeChange? = null

    fun setKeyImeChangeListener(listener: KeyImeChange) {
        keyImeChangeListener = listener
    }

    interface KeyImeChange {
        fun onKeyIme(keyCode: Int, event: KeyEvent?)
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyImeChangeListener != null) {
            keyImeChangeListener?.onKeyIme(keyCode, event)
        }
        return false
    }
}