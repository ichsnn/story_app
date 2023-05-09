package com.app.storyapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.app.storyapp.data.StoryRepository
import com.app.storyapp.data.remote.response.ListStoryItem

class StoryListViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun getStory(token: String): LiveData<PagingData<ListStoryItem>> = storyRepository.getStories(token)
}