package com.arash.altafi.chatinputview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arash.altafi.chatinputview.databinding.ItemChatInMessageBinding
import com.arash.altafi.chatinputview.model.ChatModel

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    internal var chatList: ArrayList<ChatModel> = arrayListOf()

    inner class ViewHolder(private val binding: ItemChatInMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatModel: ChatModel) {
            binding.tvText.text = chatModel.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatInMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount(): Int = chatList.size

}