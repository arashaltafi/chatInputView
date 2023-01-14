package com.arash.altafi.chatinputview.utils

import android.media.MediaRecorder
import com.arash.altafi.chatinputview.ext.logE
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 *
 * Android does not support pause and resume when recording amr audio,
 * so we implement it to provide pause and resume funciton.
 *
 */
class AMRAudioRecorder(private var fileDirectory: String) {

    private var singleFile = true
    private var recorder: MediaRecorder? = null
    private val files = ArrayList<String>()

    var audioFilePath: String? = null
        private set
    var isRecording = false
        private set

    fun start(): Boolean {
        prepareRecorder()
        try {
            recorder?.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        if (recorder != null) {
            recorder?.start()
            isRecording = true
            return true
        }
        return false
    }

    fun pause(): Boolean {
        check(!(recorder == null || !isRecording)) { "[AMRAudioRecorder] recorder is not recording!" }
        recorder?.stop()
        recorder?.release()
        recorder = null
        isRecording = false
        return true
    }

    fun resume(): Boolean {
        check(!isRecording) { "[AMRAudioRecorder] recorder is recording!" }
        singleFile = false
        newRecorder()
        return start()
    }

    fun stop(): Boolean {
        if (!isRecording) {
            return merge()
        }
        if (recorder == null) {
            return false
        }
        try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            isRecording = false
            return merge()
        } catch (ex: Exception) {
            ex.logE("recorder")
        }
        return false
    }

    fun clear() {
        if (recorder != null && isRecording) {
            try {
                recorder?.stop()
                recorder?.release()
                recorder = null
                isRecording = false
            } catch (ex: Exception) {
                ex.logE("clear")
            }
        }
        var i = 0
        val len = files.size
        while (i < len) {
            val file = File(files[i])
            file.delete()
            i++
        }
    }

    private fun merge(): Boolean {

        // If never paused, just return the file
        if (singleFile) {
            if (files.size != 0) {
                audioFilePath = files[0]
            }
            return true
        }

        // Merge files
        val mergedFilePath = fileDirectory + Date().time + ".amr"
        try {
            val fos = FileOutputStream(mergedFilePath)
            var i = 0
            val len = files.size
            while (i < len) {
                val file = File(files[i])
                val fis = FileInputStream(file)

                // Skip file header bytes,
                // amr file header's length is 6 bytes
                if (i > 0) {
                    for (j in 0..5) {
                        fis.read()
                    }
                }
                val buffer = ByteArray(512)
                var count: Int
                while (fis.read(buffer).also { count = it } != -1) {
                    fos.write(buffer, 0, count)
                }
                fis.close()
                fos.flush()
                file.delete()
                i++
            }
            fos.flush()
            fos.close()
            audioFilePath = mergedFilePath
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    private fun newRecorder() {
        recorder = MediaRecorder()
    }

    private fun prepareRecorder() {
        val directory = File(fileDirectory)
        require(!(!directory.exists() || !directory.isDirectory)) { "[AMRAudioRecorder] audioFileDirectory is a not valid directory!" }
        val filePath = directory.absolutePath + "/" + Date().time + ".amr"
        files.add(filePath)
        recorder = MediaRecorder()
        recorder?.setOutputFile(filePath)
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
    }

    init {
        if (!fileDirectory.endsWith("/")) {
            fileDirectory += "/"
        }
        newRecorder()
    }
}
