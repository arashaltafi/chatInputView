package com.arash.altafi.chatinputview.chatInput

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.Keep
import com.arash.altafi.chatinputview.R
import com.arash.altafi.chatinputview.databinding.LayoutProfileInputBinding
import com.arash.altafi.chatinputview.ext.*
import java.util.*

@SuppressLint("ClickableViewAccessibility")
class InputView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attributeSet, defStyleAttr) {

    private companion object {
        const val TAG = "InputView"
    }

    private var keyboardState: KeyboardState = KeyboardState.Default

    private var inputState: InputState = InputState.Default

    private var emoticonView: View? = null


    private fun updateState(newState: InputState, fromKbListener: Boolean) {
        if (inputState == newState)
            return

        inputState.logD("$TAG inputState inputState")
        newState.logD("$TAG inputState newState")
        fromKbListener.logD("$TAG inputState fromKbListener")

        when (newState) {
            InputState.Default -> {
                binding.btnEmoji.setImageResource(R.drawable.ic_emoji)

                if (fromKbListener.not() && inputState != InputState.Recording)
                    binding.etInput.hideKeyboard()
            }
            InputState.Keyboard -> {
                if (inputState == InputState.Emoji) {
                    if (fromKbListener.not())
                        binding.etInput.showKeyboard()
                } else
                    showEmoticonView()

                binding.btnEmoji.setImageResource(R.drawable.ic_emoji)

                keyboardState = KeyboardState.Keyboard
            }
            InputState.Emoji -> {
//                if (inputState != InputState.Keyboard)
                showEmoticonView()

                binding.btnEmoji.setImageResource(R.drawable.ic_keyboard)

                if (fromKbListener.not())
                    binding.etInput.hideKeyboard()

                keyboardState = KeyboardState.Emoji
            }
            InputState.Recording -> {
                // TODO: ?
            }
        }

        inputState = newState
    }


    //-------------------views
    private val binding by lazy { LayoutProfileInputBinding.inflate(LayoutInflater.from(context)) }


    private val isShowEmoticonView: Boolean
        get() = emoticonView?.visibility == VISIBLE

    //-------------------variables
    private var keyboardHeight = 250
        set(value) {// set emoji view height
            field = value

            emoticonView?.measuredHeight?.logD("$TAG emoticonView.measuredHeight on set keyboardHeight")
            if (keyboardHeight != emoticonView?.measuredHeight) {
                emoticonView?.apply {
                    val p = this.layoutParams
                    p.height.logD("$TAG emoticonView.height old")
                    p.height = keyboardHeight
                    p.height.logD("$TAG emoticonView.height new")

                    this.layoutParams = p
                }
            }
        }


    init {
        addView(binding.root)

        binding.etInput.setKeyImeChangeListener(object : ChatEditText.KeyImeChange {
            override fun onKeyIme(keyCode: Int, event: KeyEvent?) {
                if (KeyEvent.KEYCODE_BACK == event?.keyCode) {
                    //set delay so that emoji closes after the keyboard
                    runAfter(100, {
                        hideEmoticonView(false)
                    })
                }
            }
        })


        setupInput()
    }

    fun onShowKeyboard(keyboardSize: Int) {
        keyboardSize.logD("$TAG onShowKeyboard keyboardSize")
        if (this.keyboardHeight != keyboardSize) {
            this.keyboardHeight = keyboardSize
        }

        updateState(InputState.Keyboard, true)
    }

    fun setHideKeyboard() {
        "setHideKeyboard".logD(TAG)
        binding.etInput.hideKeyboard()
    }

    fun onHideKeyboard() {
        "onHideKeyboard".logD(TAG)

        if (inputState != InputState.Emoji) {
            hideEmoticonView(false)
        }

        if (inputState == InputState.Keyboard)
            updateState(InputState.Default, true)
    }

    fun setEmoticonView(emoticonView: View?) {
        this.emoticonView = emoticonView
    }

    fun setInitialKeyboardSize(height: Int) {
        this.keyboardHeight = height
    }

    fun setHint(hint: String) {
        binding.etInput.hint = hint
    }

    private fun hideEmoticonView(animate: Boolean): Boolean {
        isShowEmoticonView.logD("$TAG hideEmoticonView isShowEmoticonView")
        keyboardState = KeyboardState.Default
        if (isShowEmoticonView) {
            val scaleAnim = AnimationUtils.loadAnimation(context, R.anim.translate_up_down).apply {
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        emoticonView?.toGone()
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}

                })
            }

            if (animate) {
                startAnimation(scaleAnim)
            } else {
                emoticonView?.toGone()
            }

            return true
        }
        return false
    }

    /**
     * @return can navigateUp
     */
    fun handleBackPress(): Boolean {
        return when (inputState) {
            InputState.Default -> true
            InputState.Keyboard -> {
                updateState(InputState.Default, false)
                false
            }
            InputState.Emoji -> {
                updateState(InputState.Default, false)
                hideEmoticonView(true)
                false
            }
            InputState.Recording -> {
                // TODO: ??
                true
            }
        }
    }

    fun getEdittextView(): EditText = binding.etInput

    private fun showEmoticonView() {
        isShowEmoticonView.logD("$TAG showEmoticonView isShowEmoticonView")
        if (isShowEmoticonView)
            return

        emoticonView?.apply {
            val p = layoutParams.apply {
                height = keyboardHeight
            }
            this.layoutParams = p

            val scaleAnim = AnimationUtils.loadAnimation(context, R.anim.translate_down_up)
            startAnimation(scaleAnim)
            toShow()
        }
    }

    fun isShowEmotionView(): Boolean {
        return isShowEmoticonView
    }

    //private functions----------------------------------------------------------

    private fun setupInput() = binding.apply {
        btnEmoji.setOnClickListener {
            val newState: InputState = when (inputState) {
                InputState.Default -> {
                    InputState.Emoji
                }
                InputState.Keyboard -> {
                    InputState.Emoji
                }
                InputState.Emoji -> {
                    InputState.Keyboard
                }
                InputState.Recording -> InputState.Recording
            }

            updateState(newState, false)
        }

    }


    @Keep
    enum class InputState {
        Default, // nothing show
        Keyboard,
        Emoji,
        Recording,

        ;
    }

    @Keep
    enum class KeyboardState {
        Default,
        Keyboard,
        Emoji

        ;
    }


}