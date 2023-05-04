package com.app.storyapp.ui.storydetail

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.app.storyapp.R
import com.app.storyapp.data.ResultState
import com.app.storyapp.data.local.SharedPrefs
import com.app.storyapp.data.remote.response.StoryDetailResponse
import com.app.storyapp.databinding.ActivityStoryDetailBinding
import com.app.storyapp.utils.dateCreated
import com.bumptech.glide.Glide

class StoryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryDetailBinding
    private lateinit var viewModel: StoryDetailViewModel
    private lateinit var sharedPrefs: SharedPrefs

    private lateinit var storyId: String
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sharedPrefs = SharedPrefs(this)
        storyId = intent.getStringExtra(EXTRA_STORY_ID).toString()

        viewModel = ViewModelProvider(this)[StoryDetailViewModel::class.java]
        viewModel.getStoryDetail(storyId, sharedPrefs.getUser().token.toString())
        setupObserver()
        setupLoadingDialog()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupObserver() {
        viewModel.storyDetailRes.observe(this) {
            handleStoryDetailRes(it)
        }
    }

    private fun handleStoryDetailRes(result: ResultState<StoryDetailResponse>) {
        when (result) {
            is ResultState.Loading -> {
                showLoading(true)
            }
            is ResultState.Error -> {
                showLoading(false)
                showMessage(result.error)
            }
            is ResultState.Success -> {
                showLoading(false)
                handleSuccess(result.data)
            }
        }
    }

    private fun handleSuccess(data: StoryDetailResponse) {
        binding.apply {
            val story = data.story
            Glide.with(this@StoryDetailActivity).load(story?.photoUrl).into(storyPhoto)
            tvStoryUser.text = story?.name
            tvStoryDesc.text = story?.description
            tvStoryCreated.text = dateCreated(story?.createdAt)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this@StoryDetailActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupLoadingDialog() {
        val adBuilder = AlertDialog.Builder(this)
        adBuilder.setView(R.layout.loading)
        adBuilder.setCancelable(false)
        loadingDialog = adBuilder.create()
    }

    private fun showLoading(value: Boolean) {
        if (value) {
            binding.skeleton.root.visibility = View.VISIBLE
        } else {
            binding.skeleton.root.visibility = View.GONE
        }
    }

    companion object {
        const val EXTRA_STORY_ID = "extra_story_id"
    }
}