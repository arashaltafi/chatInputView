package com.arash.altafi.chatinputview.emojiView

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmojiCategoryModel(
    val iconLink: String,
    val alt: String,
    val title: String,
    val titlePersian: String,
    val emojis: List<EmojiModel>,
) : Parcelable {

    @Parcelize
    data class EmojiModel(
        val unicode: String,
        val alt: String,
        val title: String,
        val titlePersian: String,
    ) : Parcelable
}