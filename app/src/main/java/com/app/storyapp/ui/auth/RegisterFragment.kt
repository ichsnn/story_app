package com.app.storyapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.storyapp.R
import com.app.storyapp.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { handleBtnBackClicked() }
        binding.tvLogin.setOnClickListener { handleLoginClicked() }
    }

    private fun handleBtnBackClicked() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun handleLoginClicked() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}