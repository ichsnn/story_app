package com.app.storyapp.services

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.data.remote.retrofit.ApiService
import com.app.storyapp.utils.createErrorResponse
import retrofit2.HttpException

class StoryPagingSource(private val apiService: ApiService, private val token: String?) :
    PagingSource<Int, ListStoryItem>() {

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData =
                apiService.getAllStories("Bearer $token", page = position, size = params.loadSize)
            LoadResult.Page(
                data = responseData.listStory,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (responseData.listStory.isEmpty()) null else position + 1
            )
        } catch (e: HttpException) {
            val errorResponse = createErrorResponse(e)
            val error = Error(errorResponse.message)
            LoadResult.Error(error)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}