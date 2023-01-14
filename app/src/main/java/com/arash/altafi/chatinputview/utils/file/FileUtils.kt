package com.arash.altafi.chatinputview.utils.file

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import com.arash.altafi.chatinputview.ext.removeSpace
import java.io.BufferedOutputStream
import java.io.File
import kotlin.math.round

object FileUtils {

    private const val thumbnailSuffix = "webp"
    private var rootPath: String? = null

    fun setRootPath(rootPath: String) {
        FileUtils.rootPath = rootPath
    }

    private fun getFileDirectory(suffix: String): String {
        return "$rootPath/${getFileCategory(suffix)}"
    }

    fun getPath(suffix: String): String {
        val path = getFileDirectory(suffix)
        val filePath = File(path)
        if (!filePath.exists()) {
            filePath.mkdirs()
        }
        return path
    }

    fun getThumbnailPath(suffix: String): String {
        val path = ".${getFileDirectory(suffix)}/.thumbnail"
        val filePath = File(path)
        if (!filePath.exists()) {
            filePath.mkdirs()
        }
        return path
    }

    fun getTempPath(suffix: String): String {
        val path = "${getFileDirectory(suffix)}/.temp"
        val filePath = File(path)
        if (!filePath.exists()) {
            filePath.mkdirs()
        }
        return path
    }

    fun getFileSuffix(fileName: String): String {
        return MimeTypeMap.getFileExtensionFromUrl(
            fileName.removeSpace()
        )
    }

    fun getFileCategory(suffix: String): String? {
        return when {
            FileSuffixType.FILE.supportSuffixes.contains(suffix) ->
                FileSuffixType.FILE.category
            FileSuffixType.DOCUMENT.supportSuffixes.contains(suffix) ->
                FileSuffixType.DOCUMENT.category
            FileSuffixType.VOICE.supportSuffixes.contains(suffix) ->
                FileSuffixType.VOICE.category
            FileSuffixType.AUDIO.supportSuffixes.contains(suffix) ->
                FileSuffixType.AUDIO.category
            FileSuffixType.VIDEO.supportSuffixes.contains(suffix) ->
                FileSuffixType.VIDEO.category
            FileSuffixType.IMAGE.supportSuffixes.contains(suffix) ->
                FileSuffixType.IMAGE.category
            FileSuffixType.COMPRESSED.supportSuffixes.contains(suffix) ->
                FileSuffixType.COMPRESSED.category
            else -> null
        }
    }

    fun getFileType(suffix: String): FileSuffixType? {
        return when {
            FileSuffixType.FILE.supportSuffixes.contains(suffix) ->
                FileSuffixType.FILE
            FileSuffixType.DOCUMENT.supportSuffixes.contains(suffix) ->
                FileSuffixType.DOCUMENT
            FileSuffixType.VOICE.supportSuffixes.contains(suffix) ->
                FileSuffixType.VOICE
            FileSuffixType.AUDIO.supportSuffixes.contains(suffix) ->
                FileSuffixType.AUDIO
            FileSuffixType.VIDEO.supportSuffixes.contains(suffix) ->
                FileSuffixType.VIDEO
            FileSuffixType.IMAGE.supportSuffixes.contains(suffix) ->
                FileSuffixType.IMAGE
            FileSuffixType.COMPRESSED.supportSuffixes.contains(suffix) ->
                FileSuffixType.COMPRESSED
            else -> null
        }
    }

    fun isSupportFile(file: File): Boolean {
        return getFileCategory(getFileSuffix(file.name)) != null
    }

    fun isSupportSize(file: File): Pair<FileSuffixType, Boolean> {
        val length = file.length()
        return when (getFileCategory(getFileSuffix(file.name))) {
            FileSuffixType.FILE.category -> Pair(
                first = FileSuffixType.FILE,
                second = length <= FileSuffixType.FILE.supportSize
            )
            FileSuffixType.DOCUMENT.category -> Pair(
                first = FileSuffixType.DOCUMENT,
                second = length <= FileSuffixType.DOCUMENT.supportSize
            )
            FileSuffixType.VOICE.category -> Pair(
                first = FileSuffixType.VOICE,
                second = length <= FileSuffixType.VOICE.supportSize
            )
            FileSuffixType.AUDIO.category -> Pair(
                first = FileSuffixType.AUDIO,
                second = length <= FileSuffixType.AUDIO.supportSize
            )
            FileSuffixType.VIDEO.category -> Pair(
                first = FileSuffixType.VIDEO,
                second = length <= FileSuffixType.VIDEO.supportSize
            )
            FileSuffixType.IMAGE.category -> Pair(
                first = FileSuffixType.IMAGE,
                second = length <= FileSuffixType.IMAGE.supportSize
            )
            FileSuffixType.COMPRESSED.category -> Pair(
                first = FileSuffixType.COMPRESSED,
                second = length <= FileSuffixType.COMPRESSED.supportSize
            )
            else -> Pair(
                first = FileSuffixType.FILE,
                second = length <= FileSuffixType.FILE.supportSize
            )
        }
    }

    fun getFileMimeType(file: File): String? {
        return MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(getFileSuffix(file.name))
    }

    fun getFile(hashCode: Long, suffix: String): File {
        return File(getUri(hashCode, suffix)?.path!!)
    }

    fun isExist(hashCode: Long, suffix: String): Boolean {
        return getFile(hashCode, suffix).exists()
    }

    fun getUri(hashCode: Long, suffix: String): Uri? {
        return Uri.parse("${getPath(suffix)}/$hashCode.$suffix")
    }

    fun getUriByName(hashCode: Long, fileName: String): Uri? {
        val suffix = getFileSuffix(fileName)
        return Uri.parse("${getPath(suffix)}/$hashCode.$suffix")
    }

    fun getThumbnailFile(hashCode: Long): File {
        val file = File(getThumbnailUri(hashCode)?.path!!)
        return if (file.exists()) file else {
            file.createNewFile()
            file
        }
    }

    fun getThumbnailUri(hashCode: Long): Uri? {
        return Uri.parse("${getThumbnailPath(thumbnailSuffix)}/${hashCode}.$thumbnailSuffix")
    }

    fun getChecksum(file: File): String {
        val buffer = ByteArray(1024)
        val digest = java.security.MessageDigest.getInstance("SHA-512")
        file.inputStream().use {
            while (true) {
                val read = it.read(buffer)
                if (read == -1) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") {
            String.format("%02x", it)
        }
    }

    fun getFilePartList(file: File, partLength: Long): List<Pair<Long, Long>> {
        val fileLength = file.length()
        return mutableListOf<Pair<Long, Long>>().apply {
            // partLength < 1 single part
            // partLength > fileLength single part
            // partLength > 1 partLength < fileLength multiple parts

            when {
                partLength < 1 -> throw IllegalArgumentException("partLength must be greater than 0")
                fileLength < 1 -> throw IllegalArgumentException("fileLength must be greater than 0")
                partLength >= fileLength -> {
                    add(Pair(0, fileLength))
                    return@apply
                }
            }

            var start = 0L
            var end = if (fileLength <= round(partLength * 1.5))
                fileLength
            else start + partLength

            add(start to end)

            while (end < fileLength) {
                start += partLength
                end = if (fileLength - end <= round(partLength * 1.5))
                    fileLength
                else start + partLength

                add(start to end)
            }
        }
    }

    fun getPartBytes(file: File, start: Long, end: Long): ByteArray {
        val fileLength = file.length()
        if (start < 0 || end < 0 || start > fileLength || end > fileLength)
            throw IllegalArgumentException("start or end index is out of file length")

        val buffer = ByteArray((end - start).toInt())
        file.inputStream().use {
            it.skip(start)
            it.read(buffer)
        }
        return buffer
    }

    fun saveFile(
        hashCode: Long, fileName: String,
        byteArray: ByteArray,
    ): File {
        val file = getFile(hashCode, getFileSuffix(fileName))
        if (!file.exists()) {
            file.createNewFile()
        }

        BufferedOutputStream(file.outputStream()).use {
            it.write(byteArray)
        }

        return file
    }

    fun saveThumbnailFile(
        hashCode: Long, bitmap: Bitmap,
    ): File {
        val file = getThumbnailFile(hashCode)
        BufferedOutputStream(file.outputStream()).use {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                bitmap.compress(
                    Bitmap.CompressFormat.WEBP_LOSSLESS,
                    100, file.outputStream()
                )
            } else {
                bitmap.compress(
                    Bitmap.CompressFormat.WEBP,
                    100, file.outputStream()
                )
            }
        }

        return file
    }

    fun removeFile(hashCode: Long, suffix: String) {
        val file = getFile(
            hashCode = hashCode,
            suffix = suffix
        )

        if (file.exists()) {
            file.delete()
        }
    }

    fun removeThumbnailFile(hashCode: Long) {
        val file = getThumbnailFile(
            hashCode = hashCode
        )

        if (file.exists()) {
            file.delete()
        }
    }
}