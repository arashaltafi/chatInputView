package com.arash.altafi.chatinputview.emojiView

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arash.altafi.chatinputview.R
import com.arash.altafi.chatinputview.databinding.LayoutEmojiItemRcItemBinding

class EmojiPagerFragment : Fragment(R.layout.layout_emoji_item_view) {

    private val emojiRcAdapter = EmojiRcAdapter()

    var onClickListener: ((unicode: String) -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.getParcelable<EmojiCategoryModel>("EmojiCategoryModel")?.apply {

            view.findViewById<RecyclerView>(R.id.rc).apply {
                adapter = emojiRcAdapter

                emojiRcAdapter.submitList(emojis)
                emojiRcAdapter.onClickListener = this@EmojiPagerFragment.onClickListener
            }
        }
    }

}

class EmojiRcAdapter : ListAdapter<EmojiCategoryModel.EmojiModel, EmojiRcAdapter.VH>(
    EmojiDiffCallback()
) {

    companion object {
        class EmojiDiffCallback : DiffUtil.ItemCallback<EmojiCategoryModel.EmojiModel>() {
            override fun areItemsTheSame(
                oldItem: EmojiCategoryModel.EmojiModel,
                newItem: EmojiCategoryModel.EmojiModel,
            ): Boolean {
                return oldItem.unicode == newItem.unicode
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: EmojiCategoryModel.EmojiModel,
                newItem: EmojiCategoryModel.EmojiModel,
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    var onClickListener: ((unicode: String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        return VH(LayoutEmojiItemRcItemBinding.inflate(inflater))
    }

    inner class VH(val binding: LayoutEmojiItemRcItemBinding) :
        RecyclerView.ViewHolder(requireNotNull(binding.root)) {

        fun bind(position: Int, item: EmojiCategoryModel.EmojiModel) {
//            val iconDrawable = AppCompatResources.getDrawable(itemView.context, item.icon)
//            binding.ivEmoji.setImageDrawable(iconDrawable)
            binding.tvEmoji.text = item.alt
            binding.root.setOnClickListener {
                onClickListener?.invoke(item.alt)
            }
        }

    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(position, getItem(position)!!)
    }

}