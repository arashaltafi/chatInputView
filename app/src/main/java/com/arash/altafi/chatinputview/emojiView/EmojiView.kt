package com.arash.altafi.chatinputview.emojiView

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.arash.altafi.chatinputview.R
import com.arash.altafi.chatinputview.databinding.LayoutEmojiBinding
import com.arash.altafi.chatinputview.databinding.LayoutEmojiItemTabBinding
import com.arash.altafi.chatinputview.ext.loadCompat
import com.arash.altafi.chatinputview.model.EmojiCategoryModel
import com.google.android.material.tabs.TabLayoutMediator

class EmojiView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attributeSet, defStyleAttr) {

    //-------------------listeners
    var emojiViewListener: EmojiViewListener? = null

    //-------------------views
    private val binding by lazy { LayoutEmojiBinding.inflate(LayoutInflater.from(context)) }

    //-------------------flags


    //-------------------variables


    init {
        addView(binding.root)

        /*binding.root.setOnClickListener {
        }*/

    }

    var emojiData: List<EmojiCategoryModel>? = null
        private set

    fun setup(emojiData: List<EmojiCategoryModel>, fragment: Fragment) {
        this.emojiData = emojiData

        binding.vp2.adapter = EmojiPagerAdapter(
            emojiData,
            { emojiViewListener?.onInput(it) },
            fragment
        )

        TabLayoutMediator(
            binding.tabLay, binding.vp2
        ) { tab, position ->
            val customItem = LayoutEmojiItemTabBinding.inflate(LayoutInflater.from(context)).apply {
                ivTab.loadCompat(emojiData[position].iconLink, R.drawable.ic_emoji)

            }
//            customItem.ivTab.imageTintList = ColorStateList.valueOf(
//                context.getAttrColor(
//                    if (tab.isSelected)
//                        R.attr.colorPrimaryVariant
//                    else
//                        R.attr.colorControlNormal
//                )
//            )

            tab.customView = customItem.root
        }.attach()

    }

}

//------enums & interfaces & models-------------------------------------------------------------


interface EmojiViewListener {
    fun onInput(unicode: String)
//    fun onDelete()
}


class EmojiPagerAdapter(
    private val emojiCategoryModel: List<EmojiCategoryModel>,
    private val onClickListener: ((unicode: String) -> Unit),
    fragment: Fragment,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = emojiCategoryModel.size

    override fun createFragment(position: Int): Fragment {
        return EmojiPagerFragment().apply {
            arguments = Bundle().apply {
                putParcelable("EmojiCategoryModel", emojiCategoryModel[position])
            }

            onClickListener = this@EmojiPagerAdapter.onClickListener
        }
    }

}