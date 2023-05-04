package com.app.storyapp.data.remote.retrofit

import com.app.storyapp.data.dataclass.LoginDao
import com.app.storyapp.data.dataclass.RegisterDao
import com.app.storyapp.data.remote.response.AddNewStoryResponse
import com.app.storyapp.data.remote.response.LoginResponse
import com.app.storyapp.data.remote.response.RegisterResponse
import com.app.storyapp.data.remote.response.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @POST("register")
    suspend fun register(
        @Body body: RegisterDao
    ): RegisterResponse

    @POST("login")
    suspend fun login(
        @Body body: LoginDao
    ): LoginResponse

    @Multipart
    @POST("stories")
    suspend fun addNewStory(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Header("Authorization") authorization: String
    ): AddNewStoryResponse

    @GET("stories")
    suspend fun getAllStories(@Header("Authorization") authorization: String): StoriesResponse
}