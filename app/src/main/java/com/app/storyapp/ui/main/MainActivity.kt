package com.app.storyapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.storyapp.R
import com.app.storyapp.adapter.LoadingStateAdapter
import com.app.storyapp.adapter.StoriesAdapter
import com.app.storyapp.data.local.SharedPrefs
import com.app.storyapp.databinding.ActivityMainBinding
import com.app.storyapp.exception.UnauthorizedTokenException
import com.app.storyapp.ui.addstory.AddNewStoryActivity
import com.app.storyapp.ui.auth.AuthActivity
import com.app.storyapp.ui.mapstories.MapStoriesActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<StoryListViewModel>()

    private lateinit var sharedPrefs: SharedPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefs = SharedPrefs(this)
        val token = sharedPrefs.getUser().token

        if(token.isNullOrBlank()) {
            intentMainToLogin()
        }

        binding.btnAddStory.setOnClickListener { intentMainToAddStory() }
        binding.btnMaps.setOnClickListener { intentMainToMapStories() }

        getStories(token as String)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                handleLogout()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun intentMainToLogin() {
        startActivity(Intent(this@MainActivity, AuthActivity::class.java))
        finishAffinity()
    }

    private fun intentMainToAddStory() {
        startActivity(Intent(this@MainActivity, AddNewStoryActivity::class.java))
    }

    private fun intentMainToMapStories() {
        startActivity(Intent(this@MainActivity, MapStoriesActivity::class.java))
    }

    private fun handleLogout() {
        sharedPrefs.removeUser()
        intentMainToLogin()
    }

    private fun showLoadingIndicator(isLoading: Boolean) {
        binding.pagingLoadingItem.progressBarPaging.apply {
            visibility = createVisibility(isLoading)
        }
        showError()
        binding.pagingLoadingItem.btnRetry.visibility = createVisibility(false)
    }

    private fun createVisibility(state: Boolean): Int = if (state) View.VISIBLE else View.GONE

    private fun showError(message: String? = null) {
        binding.pagingLoadingItem.errorMsg.apply {
            text = message
            visibility = createVisibility(true)
        }
    }

    private fun showPagingLoadingItemVisibility(state: Boolean) {
        binding.pagingLoadingItemContainer.visibility = createVisibility(state)
    }

    private fun setRetry(retry: () -> Unit) {
        binding.pagingLoadingItem.btnRetry.apply {
            visibility = createVisibility(true)
            setOnClickListener { retry.invoke() }
        }
    }

    private fun getStories(token: String) {
        val adapter = StoriesAdapter()
        binding.rvStoryList.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
        adapter.addLoadStateListener { loadStates ->
            when (loadStates.refresh) {
                is LoadState.Loading -> {
                    showPagingLoadingItemVisibility(true)
                    showLoadingIndicator(true)
                }
                is LoadState.Error -> {
                    val errorState = loadStates.refresh as LoadState.Error
                    if (errorState.error is UnauthorizedTokenException) {
                        handleLogout()
                        Toast.makeText(this, errorState.error.localizedMessage, Toast.LENGTH_SHORT).show()
                    } else {
                        showPagingLoadingItemVisibility(true)
                        showLoadingIndicator(false)
                        showError(errorState.error.localizedMessage as String)
                        setRetry { adapter.retry() }
                    }
                }
                is LoadState.NotLoading -> {
                    showPagingLoadingItemVisibility(false)
                }
            }
        }
        binding.rvStoryList.layoutManager = LinearLayoutManager(this)
        viewModel.getStories(token).observe(this) {
            adapter.submitData(lifecycle, it)
        }
    }
}