package com.app.storyapp.ui.main

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.databinding.StoryItemBinding
import com.app.storyapp.ui.storydetail.StoryDetailActivity
import com.app.storyapp.utils.dateCreated
import com.bumptech.glide.Glide

class StoryListAdapter(private val storyList: List<ListStoryItem>) : RecyclerView.Adapter<StoryListAdapter.StoryListHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryListHolder {
        val binding = StoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryListHolder(binding)
    }

    override fun getItemCount(): Int {
        return storyList.count()
    }

    override fun onBindViewHolder(holder: StoryListHolder, position: Int) {
        val story = storyList[position]
        holder.binding.apply {
            tvStoryUser.text = story.name
            Glide.with(holder.itemView.context).load(story.photoUrl).into(storyPhoto)
            tvStoryDesc.text = story.description
            tvStoryCreated.text = dateCreated(story.createdAt)
        }
        holder.apply {
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, StoryDetailActivity::class.java)
                val optionsCompat: ActivityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
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

    inner class StoryListHolder(var binding: StoryItemBinding) : RecyclerView.ViewHolder(binding.root)
}