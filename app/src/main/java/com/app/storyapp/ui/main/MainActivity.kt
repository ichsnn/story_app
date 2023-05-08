package com.app.storyapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.storyapp.R
import com.app.storyapp.adapter.LoadingStateAdapter
import com.app.storyapp.adapter.StoriesAdapter
import com.app.storyapp.data.local.SharedPrefs
import com.app.storyapp.databinding.ActivityMainBinding
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

        binding.btnAddStory.setOnClickListener { intentMainToAddStory() }
        binding.btnMaps.setOnClickListener { intentMainToMapStories() }

        setupObserver()
        getStories()
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

    private fun setupObserver() {
        viewModel.isLoggedIn.observe(this) {
            handleLogin(it)
        }
    }

    private fun handleLogin(isLoggedIn: Boolean) {
        if (!isLoggedIn) intentMainToLogin()
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

    private fun getStories() {
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
                    showPagingLoadingItemVisibility(true)
                    showLoadingIndicator(false)
                    val errorState = loadStates.refresh as LoadState.Error
                    showError(errorState.error.localizedMessage as String)
                    setRetry { adapter.retry() }
                }

                is LoadState.NotLoading -> {
                    showPagingLoadingItemVisibility(false)
                }
            }
        }
        binding.rvStoryList.layoutManager = LinearLayoutManager(this)
        viewModel.getStories()?.observe(this) {
            adapter.submitData(lifecycle, it)
        }
    }
}