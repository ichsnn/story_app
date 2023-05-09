package com.app.storyapp.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.storyapp.data.local.entity.StoryRemoteKey

@Dao
interface StoryRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(storyRemoteKey: List<StoryRemoteKey>)

    @Query("SELECT * FROM remote_story_key WHERE id = :id")
    suspend fun getStoryRemoteKey(id: String): StoryRemoteKey?

    @Query("DELETE FROM remote_story_key")
    suspend fun deleteStoryRemoteKeys()
}