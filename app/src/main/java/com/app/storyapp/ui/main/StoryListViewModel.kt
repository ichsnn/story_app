package com.app.storyapp.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.app.storyapp.data.local.SharedPrefs
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.data.remote.retrofit.ApiConfig
import com.app.storyapp.services.StoryPagingSource

class StoryListViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = SharedPrefs(application)

    private val _isLoggedIn = MutableLiveData(true)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    fun getStories() : LiveData<PagingData<ListStoryItem>>? {
        val token = sharedPrefs.getUser().token
        if (token != null) {
            val pager = Pager(
                config = PagingConfig(pageSize = 10),
                pagingSourceFactory = {
                    StoryPagingSource(ApiConfig.getApiService(), token)
                }
            ).liveData
            return pager.cachedIn(viewModelScope)
        }
        _isLoggedIn.postValue(false)
        return null
    }
}