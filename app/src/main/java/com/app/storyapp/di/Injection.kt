package com.app.storyapp.di

import android.content.Context
import com.app.storyapp.data.StoryRepository
import com.app.storyapp.data.local.StoryDatabase
import com.app.storyapp.data.remote.retrofit.ApiConfig

object Injection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository(database, apiService)
    }
}