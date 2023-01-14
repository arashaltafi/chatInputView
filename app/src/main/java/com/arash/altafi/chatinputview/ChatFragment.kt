package com.arash.altafi.chatinputview

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.arash.altafi.chatinputview.chatInput.InputMessageAttachment
import com.arash.altafi.chatinputview.chatInput.MessageAttachmentListener
import com.arash.altafi.chatinputview.chatInput.MessageInputEditListener
import com.arash.altafi.chatinputview.chatInput.MessageInputListener
import com.arash.altafi.chatinputview.databinding.FragmentChatBinding
import com.arash.altafi.chatinputview.emojiView.EmojiViewListener
import com.arash.altafi.chatinputview.ext.*
import com.arash.altafi.chatinputview.model.ChatModel
import com.arash.altafi.chatinputview.utils.AMRAudioRecorder
import com.arash.altafi.chatinputview.utils.PermissionUtils
import com.arash.altafi.chatinputview.utils.file.FileSuffixType
import com.arash.altafi.chatinputview.utils.file.FileUtils
import java.io.File

class ChatFragment : Fragment(), MessageInputListener,
    MessageInputEditListener, EmojiViewListener, MessageAttachmentListener {

    private val binding by lazy {
        FragmentChatBinding.inflate(layoutInflater)
    }

    private val getVoiceTempFile by lazy {
        FileUtils.getTempPath("amr")
    }

    private var closeForward: Boolean = false
    private var bottomBarSize = 0
    private var flagRecording = false
    private var amrAudioRecorder: AMRAudioRecorder? = null
    private var chatAdapter = ChatAdapter()
    private var chatModel: ArrayList<ChatModel> = arrayListOf()

    private val registerResultMic = PermissionUtils.register(this,
        object : PermissionUtils.PermissionListener {
            override fun observe(permissions: Map<String, Boolean>) {
                if (permissions[PERMISSION_MIC] == true) {
                    toast("PERMISSION MIC OK")
                }
            }
        })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindViews()
        return binding.root
    }

    private fun bindViews() = binding.apply {
        rvChat.adapter = chatAdapter
    }

    override fun onClearAttachment() {
        "onClearAttachment".logI(TAG)
        setBottomBarMargin()
    }

    override fun onRecordingPermissionRequired() {
        "onRecordingPermissionRequired".logI(TAG)
        requestPermissionMic()
    }

    override fun onRecordingStarted() {
        "onRecordingStarted".logI(TAG)
        amrAudioRecorder?.clear()
        if (requestPermissionMic()) {
            initializeAmrAudio()
            amrAudioRecorder?.start()
            if (flagRecording.not()) {
                "sendTyping".logD(TAG)
                flagRecording = true
            }
        } else {
            binding.bottomBar.messageInputView.cancelAudioRecord()
        }
    }

    override fun onRecordingCompleted() {
        "onRecordingCompleted".logI(TAG)
        amrAudioRecorder?.stop()
        val uri = amrAudioRecorder?.audioFilePath
        uri?.toUri()?.logE("onRecordingCompleted")
        uri?.let {
            checkFileSize(File(it))
        }
    }

    override fun onRecordingLocked() {
        "onRecordingLocked".logI(TAG)
    }

    override fun onRecordingCanceled() {
        "onRecordingCanceled".logI(TAG)
        amrAudioRecorder?.stop()
        amrAudioRecorder?.clear()
//        chatViewModel.sendStatus(PeerModel.UserModel.Status.TypeStop)
    }

    override fun onOpenEmoji(emojiSize: Int) {
        "onOpenEmoji".logI(TAG)
        binding.apply {
            val emojiSizeWithBottomBar = emojiSize + bottomBarSize
            rvChat.setMargins(0, 0, 0, emojiSizeWithBottomBar)
        }
    }

    override fun onCloseEmoji() {
        "onCloseEmoji".logI(TAG)
        setBottomBarMargin()
    }

    override fun onTyping() {
        "onTyping".logI(TAG)
    }

    override fun onHideKeyboard() {
        "onHideKeyboard".logI(TAG)
        sendStopTyping()
    }

    private fun sendStopTyping() {
        /*if (flagTyping) {
            "sendStopTyping".logD(TAG)
            chatViewModel.sendStatus(PeerModel.UserModel.Status.TypeStop)
            flagTyping = false
        }*/
    }

    override fun onSendMessage(data: InputMessageAttachment) {
        "onSendMessage".logI(TAG)
        chatModel.add(ChatModel(data.message))
        chatAdapter.chatList = chatModel
        binding.rvChat.adapter = chatAdapter
    }

    override fun onReplyMessage(data: InputMessageAttachment) {
        "onReplyMessage".logI(TAG)
        /*chatViewModel.replyMessage(
            text = data.message,
            replayMessageId = data.attachment!!.payload.id
        )*/
    }

    override fun onEditMessage(data: InputMessageAttachment) {
        "onEditMessage".logI(TAG)
        /*data.attachment?.payload?.let {
            chatViewModel.editMessage(it.id, data.message)
        }*/
    }

    override fun onForwardMessage(data: InputMessageAttachment) {
        "onForwardMessage".logI(TAG)
        closeForward = true
        if (data.attachment?.fileHash != null && data.attachment?.fileHash != -1L) {
            //TODO how to send forward file???
        } else {
            data.attachment?.payload?.let {
                /*chatViewModel.forwardMessage(
                    text = data.attachment?.description ?: "",
                    messageId = it.id
                )*/
            }
        }
    }

    override fun onCancelEdit() {
        "onCancelEdit".logI(TAG)
        closeForward = true
        setBottomBarMargin()
    }

    private fun setupEmojiView() {
        /*val emojiList = ""
        binding.bottomBar.layEmoji.apply {
            setup(
                emojiData = emojiList,
                fragment = this@ChatFragment
            )

            emojiViewListener = this@ChatFragment
        }*/
    }

    private fun requestPermissionMic(): Boolean =
        if (!PermissionUtils.isGranted(requireContext(), PERMISSION_MIC)) {
            if (!shouldShowRequestPermissionRationale(PERMISSION_MIC)) {
                PermissionUtils.requestPermission(
                    requireContext(),
                    registerResultMic,
                    PERMISSION_MIC
                )
                false
            } else {
                showPopUpMenu()
                false
            }
        } else {
            true
        }

    private fun showPopUpMenu() {
        val message: String = getString(R.string.permission_audio_record_message)
//        findNavController().navigate(
//            R.id.permissionDialogFragment,
//            PermissionDialogFragmentArgs(message).toBundle()
//        )
    }

    private fun initializeAmrAudio() {
        amrAudioRecorder = AMRAudioRecorder(getVoiceTempFile)
    }

    private fun setBottomBarMargin() = binding.apply {
        bottomBar.root.post {
            bottomBarSize = bottomBar.root.height
            rvChat.setMargins(0, 0, 0, bottomBarSize)
        }
    }

    private fun checkFileSize(file: File) {
        if (FileUtils.isSupportFile(file)) {
            if (FileUtils.isSupportSize(file).second) {
//                chatViewModel.sendMessage(file = file)
            } else {
                val maxSize = FileUtils.isSupportSize(file).first.supportSize / 1048576
                val notSupportSize = when (FileUtils.isSupportSize(file).first) {
                    FileSuffixType.IMAGE -> {
                        getString(R.string.not_support_file_size).applyValue(
                            getString(R.string.photo),
                            maxSize
                        )
                    }
                    FileSuffixType.VIDEO -> {
                        getString(R.string.not_support_file_size).applyValue(
                            getString(R.string.video),
                            maxSize
                        )
                    }
                    FileSuffixType.FILE -> {
                        getString(R.string.not_support_file_size).applyValue(
                            getString(R.string.file),
                            maxSize
                        )
                    }
                    FileSuffixType.AUDIO -> {
                        getString(R.string.not_support_file_size).applyValue(
                            getString(R.string.music),
                            maxSize
                        )
                    }
                    FileSuffixType.VOICE -> {
                        getString(R.string.not_support_file_size).applyValue(
                            getString(R.string.voice),
                            maxSize
                        )
                    }
                    FileSuffixType.COMPRESSED -> {
                        getString(R.string.not_support_file_size).applyValue(
                            getString(R.string.compress),
                            maxSize
                        )
                    }
                    FileSuffixType.DOCUMENT -> {
                        getString(R.string.not_support_file_size).applyValue(
                            getString(R.string.file),
                            maxSize
                        )
                    }
                }
                toastCustom(notSupportSize)
            }
        } else {
            toastCustom(getString(R.string.not_support_file))
        }
    }

    override fun onAttachmentClick(
        payload: InputMessageAttachment.Payload,
        type: InputMessageAttachment.Type
    ) {
        "onAttachmentClick".logI(TAG)
        payload.logI("$TAG onAttachmentClick-payload")
        type.logI("$TAG onAttachmentClick-type")

        when (type) {
            InputMessageAttachment.Type.NoAttachment -> {}
            InputMessageAttachment.Type.Reply -> {}
            InputMessageAttachment.Type.Edit -> {}
            InputMessageAttachment.Type.Forward -> {}
        }
    }

    override fun onClickAttachment() {
//        findNavController().navigate(R.id.attachmentDialogFragment)
    }

    override fun onInput(unicode: String) {
        binding.apply {
            val text = bottomBar.messageInputView.getEdittextView().textString()
            bottomBar.messageInputView.getEdittextView().setText(text.plus(unicode))
            bottomBar.messageInputView.getEdittextView().setSelection(
                bottomBar.messageInputView.getEdittextView().text.length
            )
        }
    }

    companion object {
        const val PERMISSION_MIC = Manifest.permission.RECORD_AUDIO
        const val TAG = "ChatFragment"
    }

}