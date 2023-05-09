package com.app.storyapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.storyapp.data.local.entity.StoryRemoteKey
import com.app.storyapp.data.local.room.ListStoryItemDao
import com.app.storyapp.data.local.room.StoryRemoteKeyDao
import com.app.storyapp.data.remote.response.ListStoryItem

@Database(
    entities = [ListStoryItem::class, StoryRemoteKey::class],
    version = 2,
    exportSchema = false
)
abstract class StoryDatabase: RoomDatabase() {
    abstract fun listStoryItemDao(): ListStoryItemDao

    abstract fun storyRemoteKeyDao(): StoryRemoteKeyDao

    companion object {
        @Volatile
        private var INSTANCE: StoryDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): StoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java, "story_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}