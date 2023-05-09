package com.app.storyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_story_key")
class StoryRemoteKey(
    @PrimaryKey
    val id: String,
    val prevKey: Int?,
    val nextKey: Int?,
)