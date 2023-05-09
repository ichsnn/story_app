package com.app.storyapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.data.remote.retrofit.ApiConfig
import com.app.storyapp.services.StoryPagingSource

class StoryListViewModel : ViewModel() {

    fun getStories(token: String): LiveData<PagingData<ListStoryItem>> {
        val pager = Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = {
                StoryPagingSource(ApiConfig.getApiService(), token)
            }
        ).liveData
        return pager.cachedIn(viewModelScope)
    }
}