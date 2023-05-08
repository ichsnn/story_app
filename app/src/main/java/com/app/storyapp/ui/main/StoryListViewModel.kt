package com.app.storyapp.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.data.remote.retrofit.ApiConfig
import com.app.storyapp.services.StoryPagingSource

class StoryListViewModel(application: Application) : AndroidViewModel(application) {

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