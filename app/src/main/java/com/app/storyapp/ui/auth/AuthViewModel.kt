package com.app.storyapp.ui.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.app.storyapp.data.ResultState
import com.app.storyapp.data.dataclass.LoginDao
import com.app.storyapp.data.remote.response.LoginResponse
import com.app.storyapp.data.remote.response.LoginResult
import com.app.storyapp.data.remote.retrofit.ApiConfig
import com.app.storyapp.data.remote.retrofit.ApiService
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _loginRes = MutableLiveData<ResultState<LoginResult>>()
    val loginRes : LiveData<ResultState<LoginResult>> = _loginRes

    fun login(loginDao: LoginDao) {
        _loginRes.value = ResultState.Loading
        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().login(loginDao)
                val loginResult = response.loginResult
                _loginRes.postValue(ResultState.Success(loginResult))
            } catch (e: Exception) {
                _loginRes.postValue(ResultState.Error(e.message.toString()))
            }
        }
    }
}