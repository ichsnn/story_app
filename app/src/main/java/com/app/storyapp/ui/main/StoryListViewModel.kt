package com.app.storyapp.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.storyapp.data.ResultState
import com.app.storyapp.data.local.SharedPrefs
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StoryListViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = SharedPrefs(application)

    private val _isLoggedIn = MutableLiveData(true)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _storyList = MutableLiveData<ResultState<List<ListStoryItem>>>()
    val storyList: LiveData<ResultState<List<ListStoryItem>>> = _storyList

    init {
        getStoryList()
    }

    private fun getStoryList() {
        _storyList.value = ResultState.Loading
        val token = sharedPrefs.getUser().token
        if (token == null) {
            _isLoggedIn.postValue(false)
            return
        }
        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().getAllStories("Bearer $token")
                val storyListResponse = response.listStory
                _storyList.value = ResultState.Success(storyListResponse)
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    _isLoggedIn.postValue(false)
                }
            } catch (e: Exception) {
                _storyList.postValue(ResultState.Error(e.message.toString()))
            }
        }
    }
}