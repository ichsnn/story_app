package com.app.storyapp.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.databinding.StoryItemBinding
import com.app.storyapp.utils.MessageFormat
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
            tvStoryCreated.text = MessageFormat.dateCreated(story.createdAt)
        }
    }

    inner class StoryListHolder(var binding: StoryItemBinding) : RecyclerView.ViewHolder(binding.root)
}