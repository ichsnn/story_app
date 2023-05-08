package com.app.storyapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.storyapp.databinding.PagingLoadingItemBinding

class LoadingStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<LoadingStateAdapter.LoadingStateHolder>() {
    override fun onBindViewHolder(
        holder: LoadingStateAdapter.LoadingStateHolder,
        loadState: LoadState,
    ) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): LoadingStateAdapter.LoadingStateHolder {
        val binding = PagingLoadingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadingStateHolder(binding, retry)
    }

    inner class LoadingStateHolder(
        private val binding: PagingLoadingItemBinding,
        retry: () -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnRetry.setOnClickListener { retry.invoke() }
        }
        fun bind(loadState: LoadState) {
            binding.apply {
                if (loadState is LoadState.Error) {
                    binding.errorMsg.text = loadState.error.localizedMessage
                }
                progressBarPaging.isVisible = loadState is LoadState.Loading
                binding.btnRetry.isVisible = loadState is LoadState.Error
                binding.errorMsg.isVisible = loadState is LoadState.Error
            }
        }
    }

}