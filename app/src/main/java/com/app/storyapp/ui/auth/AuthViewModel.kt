package com.app.storyapp.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.storyapp.data.ResultState
import com.app.storyapp.data.dataclass.LoginDao
import com.app.storyapp.data.dataclass.RegisterDao
import com.app.storyapp.data.remote.response.LoginResult
import com.app.storyapp.data.remote.response.RegisterResponse
import com.app.storyapp.data.remote.retrofit.ApiConfig
import com.app.storyapp.utils.createErrorResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel : ViewModel() {
    private val _loginRes = MutableLiveData<ResultState<LoginResult>>()
    val loginRes: LiveData<ResultState<LoginResult>> = _loginRes

    private val _registerRes = MutableLiveData<ResultState<RegisterResponse>>()
    val registerRes: LiveData<ResultState<RegisterResponse>> = _registerRes

    fun login(loginDao: LoginDao) {
        _loginRes.value = ResultState.Loading
        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().login(loginDao)
                val loginResult = response.loginResult
                _loginRes.postValue(ResultState.Success(loginResult))
            } catch (e: HttpException) {
                val errorResponse = createErrorResponse(e)
                _loginRes.postValue(ResultState.Error(errorResponse.message))
            } catch (e: Exception) {
                _loginRes.postValue(ResultState.Error(e.message.toString()))
            }
        }
    }

    fun register(registerDao: RegisterDao) {
        _registerRes.value = ResultState.Loading
        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().register(registerDao)
                _registerRes.postValue(ResultState.Success(response))
            } catch (e: HttpException) {
                val errorResponse = createErrorResponse(e)
                _registerRes.postValue(ResultState.Error(errorResponse.message))
            } catch (e: Exception) {
                _registerRes.postValue(ResultState.Error(e.message.toString()))
            }
        }
    }
}