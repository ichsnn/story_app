package com.app.storyapp.ui.storydetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.storyapp.data.ResultState
import com.app.storyapp.data.remote.response.StoryDetailResponse
import com.app.storyapp.data.remote.retrofit.ApiConfig
import com.app.storyapp.utils.createErrorResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StoryDetailViewModel : ViewModel() {
    private var _storyDetailRes = MutableLiveData<ResultState<StoryDetailResponse>>()
    val storyDetailRes: LiveData<ResultState<StoryDetailResponse>> = _storyDetailRes

    fun getStoryDetail(storyId: String, token: String) {
        _storyDetailRes.value = ResultState.Loading
        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().getStoryDetail(storyId, "Bearer $token")
                _storyDetailRes.postValue(ResultState.Success(response))
            } catch (e: HttpException) {
                val errorResponse = createErrorResponse(e)
                _storyDetailRes.postValue(ResultState.Error(errorResponse.message))
            } catch (e: Exception) {
                _storyDetailRes.postValue(ResultState.Error(e.message.toString()))
            }
        }
    }
}