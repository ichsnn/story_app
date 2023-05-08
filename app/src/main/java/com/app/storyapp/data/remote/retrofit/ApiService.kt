package com.app.storyapp.data.remote.retrofit

import com.app.storyapp.data.dataclass.LoginDao
import com.app.storyapp.data.dataclass.RegisterDao
import com.app.storyapp.data.remote.response.*
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
    suspend fun getAllStories(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") location: Int = 0
    ): StoriesResponse

    @GET("stories/{id}")
    suspend fun getStoryDetail(
        @Path("id") id: String,
        @Header("Authorization") authorization: String
    ): StoryDetailResponse

    @GET("stories?location=1")
    suspend fun getMapStories(@Header("Authorization") authorization: String): StoriesResponse
}