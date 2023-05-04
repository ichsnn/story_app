package com.app.storyapp.ui.addstory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.storyapp.data.ResultState
import com.app.storyapp.data.remote.response.AddNewStoryResponse
import com.app.storyapp.data.remote.response.ErrorResponse
import com.app.storyapp.data.remote.retrofit.ApiConfig
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class AddNewStoryViewModel: ViewModel() {
    private var _addStoryRes = MutableLiveData<ResultState<AddNewStoryResponse>>()
    val addStoryRes : LiveData<ResultState<AddNewStoryResponse>> = _addStoryRes

    fun addNewStory(storyPhoto: File?, storyDescription: String, token: String) {
        if (storyPhoto == null) return
        val description = storyDescription.toRequestBody("text/plain".toMediaType())
        val file = storyPhoto.asRequestBody("image/jpeg".toMediaType())
        val fileMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            storyPhoto.name,
            file
        )
        _addStoryRes.value = ResultState.Loading
        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().addNewStory(fileMultipart, description, "Bearer $token")
                _addStoryRes.postValue(ResultState.Success(response))
            } catch (e: HttpException) {
                val errorJSONString = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorJSONString, ErrorResponse::class.java)
                _addStoryRes.postValue(ResultState.Error(errorResponse.message))
            } catch (e: Exception) {
                _addStoryRes.postValue(ResultState.Error(e.message.toString()))
            }
        }
    }
}