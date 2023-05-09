package com.app.storyapp.services

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.app.storyapp.data.local.StoryDatabase
import com.app.storyapp.data.local.entity.StoryRemoteKey
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.data.remote.retrofit.ApiService

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    private val token: String,
) : RemoteMediator<Int, ListStoryItem>() {
    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>,
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKey = getStoryRemoteKeyClosestToCurrentPosition(state)
                remoteKey?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                val remoteKey = getStoryRemoteKeyForFirstItem(state)
                val prevKey = remoteKey?.prevKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKey != null
                )
                prevKey
            }

            LoadType.APPEND -> {
                val remoteKey = getStoryRemoteKeyForLastItem(state)
                val nextKey = remoteKey?.nextKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKey != null
                )
                nextKey
            }
        }
        try {
            val responseData =
                apiService.getAllStories("Bearer $token", page, state.config.pageSize)

            val endOfPaginationReached = responseData.listStory.isEmpty()

            storyDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    storyDatabase.storyRemoteKeyDao().deleteStoryRemoteKeys()
                    storyDatabase.listStoryItemDao().deleteStories()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = responseData.listStory.map {
                    StoryRemoteKey(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                storyDatabase.storyRemoteKeyDao().insertAll(keys)
                storyDatabase.listStoryItemDao().insertStories(responseData.listStory)
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getStoryRemoteKeyForLastItem(state: PagingState<Int, ListStoryItem>): StoryRemoteKey? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            storyDatabase.storyRemoteKeyDao().getStoryRemoteKey(data.id)
        }
    }

    private suspend fun getStoryRemoteKeyForFirstItem(state: PagingState<Int, ListStoryItem>): StoryRemoteKey? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            storyDatabase.storyRemoteKeyDao().getStoryRemoteKey(data.id)
        }
    }

    private suspend fun getStoryRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ListStoryItem>): StoryRemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id.let { id ->
                storyDatabase.storyRemoteKeyDao().getStoryRemoteKey(id.toString())
            }
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

}