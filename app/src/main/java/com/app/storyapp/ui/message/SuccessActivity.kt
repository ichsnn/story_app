package com.app.storyapp.ui.message

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.storyapp.R
import com.app.storyapp.databinding.ActivitySuccessBinding
import com.app.storyapp.ui.main.MainActivity

class SuccessActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val extras: Bundle? = intent.extras
        val message: String = extras?.getString(EXTRA_MESSAGE_SUCCESS, getString(R.string.success)) as String

        binding.tvMessage.text = message
        binding.btnHome.setOnClickListener { intentToHome() }
    }

    private fun intentToHome() {
        startActivity(Intent(this@SuccessActivity, MainActivity::class.java))
        finishAffinity()
    }

    companion object {
        const val EXTRA_MESSAGE_SUCCESS = "extra_message_success"
    }
}