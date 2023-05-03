package com.app.storyapp.ui.auth

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.app.storyapp.R
import com.app.storyapp.customviews.StoryAppInputText
import com.app.storyapp.data.ResultState
import com.app.storyapp.data.dataclass.RegisterDao
import com.app.storyapp.data.remote.response.RegisterResponse
import com.app.storyapp.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<AuthViewModel>()

    private lateinit var loadingDialog: AlertDialog
    private var isSubmitting = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnBack.setOnClickListener { handleBtnBackClicked() }
            tvLogin.setOnClickListener { handleLoginClicked() }
            btnRegister.setOnClickListener { handleBtnRegisterClicked() }
        }
        setupConfirmPassword()

        setupObserver()
        setupLoadingDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupConfirmPassword() {
        binding.edRegisterConfirmPassword.onTextChangeAddition(object :
            StoryAppInputText.OnTextChangeAddition {
            override fun onTextChange(s: Editable?) {
                isPasswordNotMatch(s.toString())
            }

        })
    }

    private fun isPasswordNotMatch(string: String): Boolean {
        binding.apply {
            if (edRegisterPassword.text.toString() != string) {
                edRegisterConfirmPassword.msgPasswordNotMatch()
                return true
            }
            edRegisterEmail.msgHide()
            return false
        }
    }

    private fun setupObserver() {
        viewModel.registerRes.observe(requireActivity()) { handleRegisterRes(it) }
    }

    private fun handleRegisterRes(result: ResultState<RegisterResponse>) {
        when (result) {
            is ResultState.Loading -> showLoading(true)
            is ResultState.Success -> {
                showLoading(false)
                showSuccess(result.data.message.toString())
            }
            is ResultState.Error -> {
                showLoading(false)
                showError(result.error)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        isSubmitting = if (isLoading) {
            loadingDialog.show()
            true
        } else {
            loadingDialog.dismiss()
            false
        }
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
    }

    private fun showSuccess(message: String) {
        val successSnack = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
        successSnack.setAction(R.string.login) { handleBtnBackClicked() }
        successSnack.show()
    }

    private fun setupLoadingDialog() {
        val adBuilder = AlertDialog.Builder(requireContext())
        adBuilder.setView(R.layout.loading)
        adBuilder.setCancelable(false)
        loadingDialog = adBuilder.create()
    }

    private fun handleBtnRegisterClicked() {
        clearFocus()
        val registerDao = RegisterDao()
        var isValid: Boolean
        binding.apply {
            isValid = !edRegisterName.isFieldEmpty() && !edRegisterEmail.isFieldEmpty() &&
                    !edRegisterPassword.isFieldEmpty() && !edRegisterPassword.isMinLengthNotValid() &&
                    !edRegisterConfirmPassword.isFieldEmpty() && !edRegisterConfirmPassword.isMinLengthNotValid() &&
                    !isPasswordNotMatch(edRegisterConfirmPassword.text.toString()) && edRegisterEmail.isEmailValid()
            registerDao.name = edRegisterName.text.toString()
            registerDao.email = edRegisterEmail.text.toString()
            registerDao.password = edRegisterPassword.text.toString()
        }
        if (isValid) {
            viewModel.register(registerDao)
        }
    }

    private fun handleBtnBackClicked() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun handleLoginClicked() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
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
}