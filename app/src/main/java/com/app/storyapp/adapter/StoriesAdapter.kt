package com.app.storyapp.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.databinding.StoryItemBinding
import com.app.storyapp.ui.storydetail.StoryDetailActivity
import com.app.storyapp.utils.dateCreated
import com.bumptech.glide.Glide

class StoriesAdapter :
    PagingDataAdapter<ListStoryItem, StoriesAdapter.StoriesHolder>(DIFF_CALLBACK) {
    override fun onBindViewHolder(holder: StoriesHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StoriesHolder {
        val binding = StoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoriesHolder(binding)
    }

    inner class StoriesHolder(private val binding: StoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem) {
            binding.apply {
                tvStoryUser.text = story.name
                Glide.with(itemView.context).load(story.photoUrl).into(storyPhoto)
                tvStoryDesc.text = story.description
                tvStoryCreated.text = dateCreated(story.createdAt)
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, StoryDetailActivity::class.java)
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.storyPhoto, "storyPhoto"),
                        Pair(binding.tvStoryUser, "userName"),
                        Pair(binding.tvStoryDesc, "description"),
                        Pair(binding.tvStoryCreated, "createdOn")
                    )
                intent.putExtra(StoryDetailActivity.EXTRA_STORY_ID, story.id)
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }

        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }
}