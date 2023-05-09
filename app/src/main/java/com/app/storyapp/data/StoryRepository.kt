package com.app.storyapp.data

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.app.storyapp.data.local.StoryDatabase
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.data.remote.retrofit.ApiService
import com.app.storyapp.services.StoryRemoteMediator

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
) {
    fun getStories(token: String): LiveData<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(pageSize = 10),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = {
                storyDatabase.listStoryItemDao().getStories()
            }
        ).liveData
    }
}