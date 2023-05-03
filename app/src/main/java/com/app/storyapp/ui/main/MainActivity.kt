package com.app.storyapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.storyapp.data.ResultState
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.databinding.ActivityMainBinding
import com.app.storyapp.ui.auth.AuthActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<StoryListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObserver()
    }

    private fun setupObserver() {
        viewModel.isLoggedIn.observe(this) {
            handleLogin(it)
        }
        viewModel.storyList.observe(this) {
            handlerStoryList(it)
        }
    }

    private fun handleLogin(isLoggedIn: Boolean) {
        if(!isLoggedIn) {
            startActivity(Intent(this@MainActivity, AuthActivity::class.java))
            finishAffinity()
        }
    }

    private fun handlerStoryList(result: ResultState<List<ListStoryItem>>) {
        when (result) {
            is ResultState.Loading -> {
                showLoadingIndicator(true)
            }
            is ResultState.Success -> {
                showLoadingIndicator(false)
                setStoryList(result.data)
            }
            is ResultState.Error -> {
                showLoadingIndicator(false)
                showError(result.error)
            }
        }
    }

    private fun showLoadingIndicator(isLoading: Boolean) {
        binding.loadingIndicator.apply {
            visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setStoryList(storyList: List<ListStoryItem>) {
        val rv = binding.rvStoryList
        val storyListAdapter = StoryListAdapter(storyList)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = storyListAdapter
    }
}