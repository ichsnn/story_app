package com.app.storyapp.ui.mapstories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.storyapp.data.ResultState
import com.app.storyapp.data.local.SharedPrefs
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.data.remote.retrofit.ApiConfig
import com.app.storyapp.utils.createErrorResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlin.Exception

class MapStoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = SharedPrefs(application)
    private var _mapStoryList = MutableLiveData<ResultState<List<ListStoryItem>>>()
    val mapStoryList: LiveData<ResultState<List<ListStoryItem>>> = _mapStoryList

    init {
        getMapStories()
    }

    private fun getMapStories() {
        _mapStoryList.value = ResultState.Loading
        viewModelScope.launch {
            try {
                val token = sharedPrefs.getUser().token
                val response = ApiConfig.getApiService().getMapStories("Bearer ${token.toString()}")
                _mapStoryList.postValue(ResultState.Success(response.listStory))
            } catch (e: HttpException) {
                val errorResponse = createErrorResponse(e)
                _mapStoryList.postValue(ResultState.Error(errorResponse.message))
            } catch (e: Exception) {
                _mapStoryList.postValue(ResultState.Error(e.message.toString()))
            }
        }
    }
}