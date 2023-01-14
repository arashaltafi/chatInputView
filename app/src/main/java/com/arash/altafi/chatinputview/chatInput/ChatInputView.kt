package com.arash.altafi.chatinputview.chatInput

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.Keep
import com.arash.altafi.chatinputview.R
import com.arash.altafi.chatinputview.databinding.LayoutMessageInputBinding
import com.arash.altafi.chatinputview.ext.*
import com.arash.altafi.chatinputview.utils.PermissionUtils
import com.arash.altafi.chatinputview.utils.VibratorUtils
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class ChatInputView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attributeSet, defStyleAttr) {

    private var keyboardState: KeyboardState = KeyboardState.Default

    private var inputState: InputState = InputState.Default

    private fun updateState(newState: InputState, fromKbListener: Boolean) {
        if (inputState == newState)
            return

        inputState.logD("$TAG inputState inputState")
        newState.logD("$TAG inputState newState")
        fromKbListener.logD("$TAG inputState fromKbListener")

        when (newState) {
            InputState.Default -> {
                binding.layInput.btnEmoji.setImageResource(R.drawable.ic_emoji)

                if (fromKbListener.not() && inputState != InputState.Recording)
                    binding.layInput.etInput.hideKeyboard()
            }
            InputState.Keyboard -> {
                if (inputState == InputState.Emoji) {
                    if (fromKbListener.not())
                        binding.layInput.etInput.showKeyboard()
                } else {
                    messageInputListener?.onOpenEmoji(keyboardHeight)
                    showEmoticonView()
                }

                binding.layInput.btnEmoji.setImageResource(R.drawable.ic_emoji)

                keyboardState = KeyboardState.Keyboard
            }
            InputState.Emoji -> {
                if (inputState != InputState.Keyboard) {
                    messageInputListener?.onOpenEmoji(keyboardHeight)
                    showEmoticonView()
                }

                binding.layInput.btnEmoji.setImageResource(R.drawable.ic_keyboard)

                if (fromKbListener.not())
                    binding.layInput.etInput.hideKeyboard()

                keyboardState = KeyboardState.Emoji
            }
            InputState.Recording -> {
                // TODO: ?
            }
        }

        inputState = newState
    }

    //-------------------listeners
    private var messageInputListener: MessageInputListener? = null
    private var messageInputEditListener: MessageInputEditListener? = null
    private var messageAttachmentListener: MessageAttachmentListener? = null

    //-------------------views
    private val binding by lazy { LayoutMessageInputBinding.inflate(LayoutInflater.from(context)) }

    //    private var containerView: KeyboardDetectorRelativeLayout? = null
    private var containerView: ViewGroup? = null
    private var chatView: ViewGroup? = null
    private var emoticonView: View? = null

    //-------------------data
    private var mAttachment: InputMessageAttachment =
        InputMessageAttachment.Builder().buildNoAttach("")
        set(value) {
            field = value
            setupAttachments()
        }

    //-------------------flags
    private var isTyping = false
        set(value) {
            value.logE("$TAG isTyping")
            field = value
            if (value)
                messageInputListener?.onTyping()
            else
                messageInputListener?.onHideKeyboard()
        }

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

    private var voiceTime = 0
    private var directionOffset = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var firstX = 0f
    private var firstY = 0f
    private var cancelOffset = 0f
    private var lockOffset = 0f
    private var cancelOffsetZone = 0f
    private var isDeleting = false
    private var stopTrackingAction = true
    private var isLocked: Boolean = false
    private var userBehaviour = UserBehaviour.NONE
        set(value) {
            field = value
            value.logE("$TAG userBehaviour")
        }
    private val screenWidth = getScreenWidth()

    init {
        addView(binding.root)

        binding.layInput.etInput.setKeyImeChangeListener(object : ChatEditText.KeyImeChange {
            override fun onKeyIme(keyCode: Int, event: KeyEvent?) {
                if (KeyEvent.KEYCODE_BACK == event?.keyCode) {
                    //set delay so that emoji closes after the keyboard
                    runAfter(100, {
                        hideEmoticonView(false)
                    })
                }
            }
        })

        binding.ivMainBtn.apply {
            toHide()

            setOnClickListener { _ ->
                "ivMainBtn".logD("$TAG chatInputView onBtnClick")

                isTyping = false

                if (mAttachment.type.needInput) {
                    binding.layInput.etInput.textString().takeIf { it.isNotEmpty() }?.let {
                        if (sendMessage(mAttachment.updateMessage(it)))
                            clearAttachment()
                    }
                } else {
                    binding.layInput.etInput.textString().takeIf { it.isNotEmpty() }?.let {
                        if (sendMessage(InputMessageAttachment.Builder().buildNoAttach(it)).not())
                            return@setOnClickListener
                    }

                    sendMessage(mAttachment)

                    clearAttachment()

                }

            }
        }

        binding.ivStopBtn.apply {
            toHide()

            setOnClickListener {
                stopRecord(RecordingBehaviour.RELEASED)
                translateXRecorderBtn(0f)
                setRecorderBtnVisibility(true)
                binding.layInput.etInput.enable()
                binding.lRecorderLock.smoothHide(ANIMATION_DURATION)
            }
        }

        binding.ivRecordBtn.apply {
            toShow()

            setOnTouchListener { view, motionEvent ->
                if (motionEvent.action != MotionEvent.ACTION_MOVE) {
                    motionEvent.logD("$TAG motionEvent except:ACTION_MOVE")
                    isDeleting.logI("$TAG motionEvent isDeleting")
                    inputState.logE("$TAG motionEvent inputState")
                }

                if (isDeleting) {
                    return@setOnTouchListener true
                }

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        cancelOffsetZone = (screenWidth / 2).toFloat()
                        lockOffset = (screenWidth / 3).toFloat()
                        cancelOffset =
                            binding.ivRecordBtn.x -
                                    binding.ivRecordMic.x -
                                    (binding.ivRecordMic.width / 2)

                        if (firstX == 0f) {
                            firstX = motionEvent.rawX
                        }
                        if (firstY == 0f) {
                            firstY = motionEvent.rawY
                        }

                        if (hasMicPermission()) {
                            isTyping = false
                            startRecord()
                        } else {
                            messageInputListener?.onRecordingPermissionRequired()
                        }

                    }
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        if (stopTrackingAction.not()) {
                            translateXRecorderBtn(0f)
                            translateYRecorderBtn(0f)
                            stopRecord(RecordingBehaviour.RELEASED)
                        }
                        binding.apply {
                            ivRecordBtn.disable()
                            root.postDelayed({ ivRecordBtn.enable() }, 500)
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (stopTrackingAction) {
                            return@setOnTouchListener true
                        }

                        var direction = UserBehaviour.NONE

                        val motionX: Float = abs(firstX - motionEvent.rawX)
                        val motionY: Float = abs(firstY - motionEvent.rawY)

                        if (motionY > motionX && lastY < firstY) {
                            direction = UserBehaviour.LOCKING
                        } else if (motionY > motionX && motionY > directionOffset && lastY < firstY) {
                            direction = UserBehaviour.LOCKING
                        } else if (motionX > motionY && motionX > directionOffset && lastX < firstX && motionX > cancelOffsetZone) {
                            direction = UserBehaviour.CANCELING
                        } else if (motionX > motionY && motionX < directionOffset) {
                            direction = UserBehaviour.NONE
                        }

                        userBehaviour = direction

                        when (userBehaviour) {
                            UserBehaviour.CANCELING -> {
                                binding.layInput.etInput.enable()
                                showRecordingTrash()
                                if (userBehaviour == UserBehaviour.NONE || motionEvent.rawY + binding.ivRecordBtn.width / 2 > firstY) {
                                    translateXRecorderBtn(-(firstX - motionEvent.rawX))
                                }
                                isLocked = false
                            }
                            UserBehaviour.NONE -> {
                                binding.layInput.etInput.enable()
                                showRecordingMic()
                                translateXRecorderBtn(-(firstX - motionEvent.rawX))
                                isLocked = false
                            }
                            UserBehaviour.LOCKING -> {
                                binding.layInput.etInput.disable()
                                binding.ivRecordMic.clearAnimation()
                                if (userBehaviour == UserBehaviour.NONE || motionEvent.rawX + binding.ivRecordBtn.width / 2 > firstX) {
                                    translateYRecorderBtn(-(firstY - motionEvent.rawY))
                                }
                                isLocked = true
                            }
                        }

                        lastX = motionEvent.rawX
                        lastY = motionEvent.rawY
                    }
                }

                view.onTouchEvent(motionEvent)
                true
            }

        }

        setupInput()
        setupAttachments()
    }

    private fun hasMicPermission() =
        PermissionUtils.isGranted(context, Manifest.permission.RECORD_AUDIO)

    private fun translateXRecorderBtn(x: Float) {
        binding.lRecorderLock.translationY = 0f
        binding.ivRecordBtn.translationY = 0f
        when {
            x < 0 && abs(x) > abs(cancelOffset) -> {
                cancelRecord()
                binding.ivRecordBtn.translationX = 0f
            }
            x > 0 ->
                binding.ivRecordBtn.translationX = 0f
            else ->
                binding.ivRecordBtn.translationX = x
        }
        if (abs(x) < binding.ivRecordMic.width / 2) {
            if (binding.lRecorderLock.isShow().not()) {
                binding.lRecorderLock.smoothShow(ANIMATION_DURATION)
            }
        } else {
            if (binding.lRecorderLock.isShow()) {
                binding.lRecorderLock.smoothHide(ANIMATION_DURATION)
            }
        }
    }

    private fun translateYRecorderBtn(y: Float) {
        if (y < -lockOffset) {
            stopRecord(RecordingBehaviour.LOCKED)
            binding.ivRecordBtn.translationY = 0f
            messageInputListener?.onRecordingLocked()
            binding.ivRecordBtn.smoothHide(ANIMATION_DURATION)
            binding.ivMainBtn.smoothHide(ANIMATION_DURATION)
            binding.ivStopBtn.smoothShow(ANIMATION_DURATION)
            return
        }
        if (binding.lRecorderLock.isShow().not()) {
            binding.lRecorderLock.smoothShow(ANIMATION_DURATION)
        }
        binding.ivRecordBtn.translationY = y
        binding.lRecorderLock.translationY = y / 3
        binding.ivRecordBtn.translationX = 0f
    }

    private fun showRecordingTrash() = binding.apply {
        if (ivRecordTrash.isShow().not()) {
            "showRecordingTrash".logE("$TAG recorder")
            ivRecordMic.clearAnimation()
            ivRecordMic.smoothHide(ANIMATION_DURATION)
            ivRecordTrash.smoothShow(ANIMATION_DURATION)
        }
    }

    private fun showRecordingMic() = binding.apply {
        if (ivRecordMic.isShow().not()) {
            "showRecordingMic".logE("$TAG recorder")
            ivRecordMic.smoothShow(ANIMATION_DURATION)
            ivRecordMic.startAnimation(animationPulse)
            ivRecordTrash.smoothHide(ANIMATION_DURATION)
        }
    }

    private fun cancelRecord() {
        "canceled".logD("$TAG recorder")
//        animateDelete()
        stopTrackingAction = true
        stopRecord(RecordingBehaviour.CANCELED)
    }

    fun setMessageInputListener(listener: MessageInputListener) {
        this.messageInputListener = listener
    }

    fun setMessageInputEditListener(listener: MessageInputEditListener) {
        this.messageInputEditListener = listener
    }

    fun setMessageAttachmentListener(listener: MessageAttachmentListener) {
        this.messageAttachmentListener = listener
    }

    fun setRootView(rootView: ViewGroup) {
        containerView = rootView
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
        binding.layInput.etInput.hideKeyboard()
    }

    fun onHideKeyboard() {
        "onHideKeyboard".logD(TAG)
        isTyping = false

        if (inputState != InputState.Emoji) {
            hideEmoticonView(false)
        }

        if (inputState == InputState.Keyboard)
            updateState(InputState.Default, true)
    }

    fun setChatView(chatView: ViewGroup?) {
        this.chatView = chatView
    }

    fun setEmoticonView(emoticonView: View?) {
        this.emoticonView = emoticonView
    }

    fun setInitialKeyboardSize(height: Int) {
        this.keyboardHeight = height
    }

    fun setAttachment(attachment: InputMessageAttachment) {
        this.mAttachment = attachment
    }

    private fun clearAttachment() {
        this.mAttachment = InputMessageAttachment.Builder().buildNoAttach("")
        messageInputListener?.onClearAttachment()
    }

    private fun hideEmoticonView(animate: Boolean): Boolean {
        isShowEmoticonView.logD("$TAG hideEmoticonView isShowEmoticonView")
        messageInputListener?.onCloseEmoji()
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
        if (isLocked)
            stopRecord(RecordingBehaviour.CANCELED)
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

    fun getEdittextView(): EditText = binding.layInput.etInput

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

    //private functions----------------------------------------------------------

    private fun setupInput() = binding.layInput.apply {
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

        btnAttachment.setOnClickListener {
            messageAttachmentListener?.onClickAttachment()
        }

        etInput.afterTextChange {
            val notEmpty = it.isNotEmpty()

            notEmpty.logE("$TAG etInput afterTextChange")
            if (mAttachment.type == InputMessageAttachment.Type.NoAttachment)
                setRecorderBtnVisibility(notEmpty.not())
            isTyping = notEmpty
        }
    }

    private fun setRecorderBtnVisibility(show: Boolean) {
        if (show) {
            binding.ivRecordBtn.smoothShow(ANIMATION_DURATION)
            binding.ivMainBtn.smoothHide(ANIMATION_DURATION)
        } else {
            binding.ivMainBtn.smoothShow(ANIMATION_DURATION)
            binding.ivRecordBtn.smoothHide(ANIMATION_DURATION)
        }
    }

    private fun setupAttachments() {
        binding.layInput.etInput.setText(mAttachment.message)

        setRecorderBtnVisibility(
            mAttachment.type == InputMessageAttachment.Type.NoAttachment
        )

        when {
            mAttachment.linkPreview != null -> setupPreview()
            mAttachment.attachment != null -> setupAttachment()
            else -> binding.layEdit.root.toGone()
        }
    }

    private fun setupAttachment() = binding.layEdit.apply {
        mAttachment.attachment?.let { item ->
            tvAttachmentTitle.text = item.title
            tvAttachmentDesc.text = item.description
            ivAttachmentIcon.loadCompat(item.icon)
            ivAttachmentThumbnail.apply {
                if (item.thumbnail != null) {
                    toShow()
                    setImageURI(item.thumbnail)
                }
                else
                    toGone()
            }
            ivAttachmentClose.setOnClickListener {
                setAttachment(mAttachment.clearAttachment())
                if (mAttachment.type.clearInput)
                    binding.layInput.etInput.clear()
                messageInputEditListener?.onCancelEdit()
            }

            root.setOnClickListener {
                messageInputEditListener?.onAttachmentClick(item.payload, mAttachment.type)
            }

            root.toShow()
        } ?: run { root.toGone() }
    }

    private fun setupPreview() = binding.layEdit.apply {
        mAttachment.linkPreview?.let { item ->
            tvAttachmentTitle.text = item.title
            tvAttachmentDesc.text = item.description
            ivAttachmentIcon.loadCompat(R.drawable.ic_link)
            ivAttachmentThumbnail.loadCompat(
                item.image,
                requestOptions = RequestOptions().centerCrop()
            )

            ivAttachmentClose.setOnClickListener {
                setAttachment(mAttachment.clearLinkPreview())
            }

            root.setOnClickListener {

            }

            root.toShow()
        } ?: run { root.toGone() }
    }

    /**
     * @return true if message sent
     */
    private fun sendMessage(attachment: InputMessageAttachment): Boolean {
        attachment.attachment?.logD("$TAG chatInputView sendMessage attachment")
        attachment.type.logD("$TAG chatInputView sendMessage type")
        attachment.message.logD("$TAG chatInputView sendMessage message")

        return when (attachment.type) {
            InputMessageAttachment.Type.NoAttachment ->
                if (attachment.message.trim().isNotEmpty()) {
                    messageInputListener?.onSendMessage(attachment)
                    true
                } else {
                    false
                }
            InputMessageAttachment.Type.Reply ->
                if (attachment.message.trim().isNotEmpty()) {
                    messageInputListener?.onReplyMessage(attachment)
                    true
                } else {
                    false
                }
            InputMessageAttachment.Type.Edit ->
                if (attachment.message.trim().isNotEmpty()) {
                    messageInputListener?.onEditMessage(attachment)
                    true
                } else {
                    false
                }
            InputMessageAttachment.Type.Forward -> {
                messageInputListener?.onForwardMessage(attachment)
                true
            }
        }
    }

    private fun stopRecord(behaviour: RecordingBehaviour) = binding.apply {
        "stopRecord".logD("$TAG recorder")

        isDeleting = true
        imageViewLockArrow.clearAnimation()
        imageViewLock.clearAnimation()
        lRecorderLock.smoothHide(ANIMATION_DURATION)
        binding.lRecorderLock.translationY = 0f

        if (behaviour == RecordingBehaviour.LOCKED) return@apply

        stopTrackingAction = true
        firstX = 0f
        firstY = 0f
        lastX = 0f
        lastY = 0f

        when (behaviour) {
            RecordingBehaviour.CANCELED -> {
                messageInputListener?.onRecordingCanceled()
            }
            RecordingBehaviour.RELEASED -> {
                if (voiceTime < 2) {
                    context.toastCustom(context.getString(R.string.voice_error))
                    messageInputListener?.onRecordingCanceled()
                }
                else
                    messageInputListener?.onRecordingCompleted()
            }
            else -> {}
        }

        VibratorUtils.vibrate(context, VIBRATION_DURATION)

        ivRecordBtn.apply {
            animate().scaleX(1f).scaleY(1f).setDuration(ANIMATION_DURATION).start()
        }

        layInput.apply {
            etInput.hint = context.getString(R.string.write)

            btnEmoji.smoothShow(ANIMATION_DURATION)
            btnAttachment.smoothShow(ANIMATION_DURATION)
        }

        ivRecordMic.clearAnimation()
        ivRecordMic.smoothHide(ANIMATION_DURATION)
        ivRecordTrash.smoothHide(ANIMATION_DURATION)
        tvRecordTime.smoothHide(ANIMATION_DURATION)
        ivStopBtn.smoothHide(ANIMATION_DURATION)

        laySlideToCancel.clearAnimation()
        laySlideToCancel.smoothHide(ANIMATION_DURATION)

        when (keyboardState) {
            KeyboardState.Keyboard -> {
                updateState(InputState.Keyboard, false)
            }
            KeyboardState.Emoji -> {
                updateState(InputState.Emoji, false)
            }
            else -> {}
        }


        stopRecordTimer()
        isDeleting = false
    }

    private val animationWaveHorizontal by lazy {
        AnimationUtils.loadAnimation(context, R.anim.wave_horizontal)
    }
    private val animationPulse by lazy {
        AnimationUtils.loadAnimation(context, R.anim.pulse)
    }
    private val animJump by lazy {
        AnimationUtils.loadAnimation(context, R.anim.jump)
    }
    private val animJumpFast by lazy {
        AnimationUtils.loadAnimation(context, R.anim.jump_fast)
    }

    private fun startRecord() = binding.apply {
        "startRecord".logD("$TAG recorder")

        stopTrackingAction = false
        userBehaviour = UserBehaviour.NONE

        ivRecordBtn.apply {
            animate().scaleX(1.3f).scaleY(1.3f).setDuration(ANIMATION_DURATION).start()
        }

        VibratorUtils.vibrate(context, VIBRATION_DURATION)

        layInput.apply {
            etInput.hint = ""
            etInput.clearFocus()

            btnEmoji.smoothHide(ANIMATION_DURATION)
            btnAttachment.smoothHide(ANIMATION_DURATION)
        }

        ivRecordMic.smoothShow(ANIMATION_DURATION)
        ivRecordMic.startAnimation(animationPulse)
        tvRecordTime.smoothShow(ANIMATION_DURATION)
        laySlideToCancel.smoothShow(ANIMATION_DURATION)
        lRecorderLock.smoothShow(ANIMATION_DURATION)
        laySlideToCancel.startAnimation(animationWaveHorizontal)
        imageViewLockArrow.clearAnimation()
        imageViewLock.clearAnimation()
        imageViewLockArrow.startAnimation(animJumpFast)
        imageViewLock.startAnimation(animJump)

        startRecordTimer()

        messageInputListener?.onRecordingStarted()
    }

    fun cancelAudioRecord() {
        stopRecord(RecordingBehaviour.CANCELED)
    }

    private var audioTotalTime = 0L
    private var timerTask: TimerTask? = null
    private var audioTimer: Timer? = null
    private val timeFormatter: SimpleDateFormat by lazy {
        SimpleDateFormat("m:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private fun startRecordTimer() {
        if (audioTimer == null) {
            audioTimer = Timer()
        }

        timerTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    voiceTime += 1
                    binding.tvRecordTime.text = timeFormatter.format(Date(audioTotalTime * 1000))
                    audioTotalTime++
                }
            }
        }

        audioTotalTime = 0
        audioTimer?.schedule(timerTask, 0, 1000)
    }

    private fun stopRecordTimer() {
        audioTotalTime = 0L
        voiceTime = 0
        binding.tvRecordTime.text = timeFormatter.format(Date(0))
        audioTimer?.cancel()
        audioTimer = null
        timerTask?.cancel()
        timerTask = null
    }

    private companion object {
        const val TAG = "ChatInputView"
        const val ANIMATION_DURATION = 200L
        const val VIBRATION_DURATION = 100L
    }
}

//------enums & interfaces & models-------------------------------------------------------------

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

@Keep
enum class UserBehaviour {
    CANCELING, LOCKING, NONE
}

@Keep
enum class RecordingBehaviour {
    CANCELED, RELEASED, LOCKED
}

interface MessageInputListener {
    fun onClearAttachment()
    fun onRecordingPermissionRequired()
    fun onRecordingStarted()
    fun onRecordingCompleted()
    fun onRecordingLocked()
    fun onRecordingCanceled()
    fun onOpenEmoji(emojiSize: Int)
    fun onCloseEmoji()
    fun onTyping()
    fun onHideKeyboard()
    fun onSendMessage(data: InputMessageAttachment)
    fun onReplyMessage(data: InputMessageAttachment)
    fun onEditMessage(data: InputMessageAttachment)
    fun onForwardMessage(data: InputMessageAttachment)
}

interface MessageInputEditListener {
    fun onCancelEdit()

    fun onAttachmentClick(
        payload: InputMessageAttachment.Payload,
        type: InputMessageAttachment.Type,
    )
}

interface MessageAttachmentListener {
    fun onClickAttachment()
}

class InputMessageAttachment private constructor(
    var message: String,
    var attachment: AttachmentModel?,
    var linkPreview: LinkPreviewModel?,
    var type: Type,
) {

    fun updateMessage(message: String): InputMessageAttachment {
        this.message = message

        return this
    }

    fun clearAttachment(): InputMessageAttachment {
        this.attachment = null
        this.type = Type.NoAttachment
        return this
    }

    fun clearLinkPreview(): InputMessageAttachment {
        this.linkPreview = null

        return this
    }

    class Builder {

        private var message: String = ""
        private var attachment: AttachmentModel? = null
        private var linkPreview: LinkPreviewModel? = null
        private var type: Type = Type.NoAttachment

        fun addLinkPreview(
            title: String,
            description: String,
            image: String,
        ) = apply {

            this.linkPreview = LinkPreviewModel(
                title = title,
                description = description,
                image = image,
            )
        }

        fun buildNoAttach(
            message: String,
        ) = run {
            this.type = Type.NoAttachment
            this.message = message

            build()
        }

        fun buildEdit(
            context: Context,
            message: String,
            thumbnail: Uri?,
            payload: Payload,
        ) = run {
            this.type = Type.Edit
            this.message = message
            this.attachment = AttachmentModel(
                title = context.getString(R.string.edit_message),
                description = message,
                icon = R.drawable.ic_edit,
                payload = payload,
                thumbnail = thumbnail,
            )

            build()
        }

        fun buildReply(
            currentMessage: String,
            message: String,
            senderName: String,
            thumbnail: Uri?,
            payload: Payload,
        ) = run {
            this.type = Type.Reply
            this.message = currentMessage
            this.attachment = AttachmentModel(
                title = senderName,
                description = message,
                icon = R.drawable.ic_reply,
                payload = payload,
                thumbnail = thumbnail,
            )

            build()
        }

        fun buildForward(
            context: Context,
            message: String,
            uri: Uri,
            thumbnail: Uri,
            fileHash: Long,
            payload: Payload,
        ) = run {
            this.type = Type.Forward
            this.message = ""
            this.attachment = AttachmentModel(
                title = context.getString(R.string.forward_n_message).applyValue(1.toString()),
                description = message,
                icon = R.drawable.ic_reply,
                payload = payload,
                uri = uri,
                thumbnail = thumbnail,
                fileHash = fileHash
            )

            build()
        }

        private fun build() =
            InputMessageAttachment(message, attachment, linkPreview, type)
    }

    @Keep
    enum class Type(
        val needInput: Boolean,
        val clearInput: Boolean,
    ) {
        NoAttachment(needInput = true, clearInput = false),
        Reply(needInput = true, clearInput = false),
        Edit(needInput = true, clearInput = true),
        Forward(needInput = false, clearInput = false),

        ;
    }

    @Keep
    data class AttachmentModel(
        val title: String,
        val description: String,
        val icon: Any,
        val payload: Payload,
        val uri: Uri? = null,
        val thumbnail: Uri? = null,
        val fileHash: Long? = null
    )

    @Keep
    data class LinkPreviewModel(
        val title: String,
        val description: String,
        val image: String,
    )

    @Keep
    data class Payload(
        val mid: Long,
        val id: Long,
    )
}