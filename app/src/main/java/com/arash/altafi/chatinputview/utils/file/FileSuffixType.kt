package com.arash.altafi.chatinputview.utils.file

enum class FileSuffixType(
    val category: String,
    val supportSize: Long,
    val supportSuffixes: List<String>
) {
    IMAGE(
        category = "Image",
        supportSize = 100 * (1024 * 1024),
        supportSuffixes = listOf(
            "jpg",
            "png",
            "gif",
            "jpeg",
            "svg",
            "webp",
            "ico"
        )
    ),
    VIDEO(
        category = "Video",
        supportSize = 100 * (1024 * 1024),
        supportSuffixes = listOf(
            "avi",
            "mkv",
            "mov",
            "mp4",
            "mpg",
            "mpeg",
            "wmv"
        )
    ),
    AUDIO(
        category = "Audio",
        supportSize = 100 * (1024 * 1024),
        supportSuffixes = listOf(
            "mp3",
            "wav",
            "m4a",
            "wma",
            "wav"
        )
    ),
    VOICE(
        category = "Voice",
        supportSize = 100 * (1024 * 1024),
        supportSuffixes = listOf(
            "ogg",
            "amr",
            "aac"
        )
    ),
    FILE(
        category = "File",
        supportSize = 100 * (1024 * 1024),
        supportSuffixes = listOf(
            "dmg",
            "iso",
            "apk",
            "srt"
        )
    ),
    DOCUMENT(
        category = "Document",
        supportSize = 100 * (1024 * 1024),
        supportSuffixes = listOf(
            "docx",
            "doc ",
            "xls",
            "odt",
            "pdf",
            "ppt",
            "pptx",
            "txt",
            "ods",
            "csv",
            "dat",
            "pps",
            "key",
            "xlsm",
            "xlsx",
            "rtf"
        )
    ),
    COMPRESSED(
        category = "Compressed",
        supportSize = 100 * (1024 * 1024),
        supportSuffixes = listOf(
            "zip",
            "rar",
            "7z",
            "gz",
            "tar.gz",
        )
    ),
}