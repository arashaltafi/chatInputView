package com.arash.altafi.chatinputview.ext

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope

fun EditText?.afterTextChange(afterTextChanged: (String) -> Unit): TextWatcher {
    var beforeText = ""
    val watcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            if (beforeText == editable.toString())
                return

            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            beforeText = s.toString()
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    this?.addTextChangedListener(watcher)

    return watcher
}

fun EditText.textString() =
    this.text.toString()

fun TextInputLayout.textString() =
    this.editText?.editableText.toString()

fun EditText.onChange(
    waitMs: Long = 800L,
    scope: CoroutineScope,
    destinationFunction: (String) -> Unit,
): TextWatcher = afterTextChange(debounce(waitMs, scope, destinationFunction))


fun SearchView.onChange(
    waitMs: Long = 800L,
    scope: CoroutineScope,
    destinationFunction: (String) -> Unit,
) {
    val f = debounceCancelable(waitMs, scope, destinationFunction)

    this.setOnQueryTextListener(object :
        SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            f.invoke(null)
            destinationFunction.invoke(query ?: "")

            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            f.invoke(newText ?: "")

            return true
        }
    })
}

fun SearchView.removeUnderLine() {
    val v = this.findViewById<View>(androidx.appcompat.R.id.search_plate)
    v.setBackgroundColor(Color.TRANSPARENT)
}