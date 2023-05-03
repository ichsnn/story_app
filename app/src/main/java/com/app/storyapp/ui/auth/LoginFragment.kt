package com.app.storyapp.ui.auth

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.app.storyapp.R
import com.app.storyapp.data.ResultState
import com.app.storyapp.data.dataclass.LoginDao
import com.app.storyapp.data.dataclass.UserData
import com.app.storyapp.data.local.SharedPrefs
import com.app.storyapp.data.remote.response.LoginResult
import com.app.storyapp.databinding.FragmentLoginBinding
import com.app.storyapp.ui.main.MainActivity
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<AuthViewModel>()

    private lateinit var loadingDialog: AlertDialog
    private var isLoggingIn = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvRegister.setOnClickListener { fragmentToRegister() }
        binding.btnLogin.setOnClickListener { handleBtnLoginClicked() }

        setupObserver()
        setupLoadingDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun handleBtnLoginClicked() {
        clearFocus()
        val loginDao = LoginDao()
        binding.apply {
            loginDao.email = edLoginEmail.text.toString()
            loginDao.password = edLoginPassword.text.toString()
        }
        viewModel.login(loginDao)
    }

    private fun setupObserver() {
        viewModel.loginRes.observe(requireActivity()) { handleLoginRes(it) }
    }

    private fun setupLoadingDialog() {
        val adBuilder = AlertDialog.Builder(requireContext())
        adBuilder.setView(R.layout.loading)
        adBuilder.setCancelable(false)
        loadingDialog = adBuilder.create()
    }

    private fun handleLoginRes(result: ResultState<LoginResult>) {
        when (result) {
            is ResultState.Loading -> showLoading(true)
            is ResultState.Success -> {
                showLoading(false)
                handleSuccess(result.data)
                intentToMain()
            }
            is ResultState.Error -> {
                showError(result.error)
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        isLoggingIn = if (isLoading) {
            loadingDialog.show()
            true
        } else {
            loadingDialog.dismiss()
            false
        }
    }

    private fun showError(message: String) {
        if (isLoggingIn) Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
    }

    private fun handleSuccess(result: LoginResult) {
        val sharedPrefs = SharedPrefs(requireContext())
        val userData = UserData(result.userId, result.name, result.token)
        sharedPrefs.setUser(userData)
    }

    private fun intentToMain() {
        startActivity(Intent(requireActivity(), MainActivity::class.java))
        requireActivity().finishAffinity()
    }

    private fun clearFocus() {
        try {
            val imm: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun fragmentToRegister() {
        val registerFragment = RegisterFragment()
        val fragmentManager = parentFragmentManager
        fragmentManager.beginTransaction().apply {
            replace(R.id.frame_container, registerFragment, RegisterFragment::class.java.simpleName)
            addToBackStack(null)
            commit()
        }
    }

}