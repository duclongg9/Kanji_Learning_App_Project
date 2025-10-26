package com.example.kanjilearning.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.FragmentLoginBinding
import com.example.kanjilearning.ui.login.LoginErrorType
import com.example.kanjilearning.ui.login.LoginEvent
import com.example.kanjilearning.ui.login.LoginUiState
import com.example.kanjilearning.ui.login.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    private var hasNavigated = false

    private val googleLoginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                viewModel.onLoginCancelled()
                return@registerForActivityResult
            }
            val intent = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    viewModel.onGoogleAccountReceived(account)
                } else {
                    viewModel.onLoginFailed(null)
                }
            } catch (error: ApiException) {
                viewModel.onLoginFailed(error)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonLogin.setOnClickListener {
            if (viewModel.uiState.value.isLoading) return@setOnClickListener
            val intent = googleSignInClient.signInIntent
            googleLoginLauncher.launch(intent)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { state -> renderState(state) } }
                launch { viewModel.events.collect { handleEvent(it) } }
            }
        }
    }

    private fun renderState(state: LoginUiState) {
        binding.buttonLogin.isEnabled = !state.isLoading
        binding.progressLogin.isVisible = state.isLoading
    }

    private fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.NavigateNext -> navigateNextIfNeeded()
            is LoginEvent.ShowError -> showError(event.type)
        }
    }

    private fun navigateNextIfNeeded() {
        if (hasNavigated) return
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.loginFragment) {
            hasNavigated = true
            navController.navigate(R.id.action_loginFragment_to_modeSelectionFragment)
        }
    }

    private fun showError(type: LoginErrorType) {
        val message = when (type) {
            LoginErrorType.Cancelled -> getString(R.string.login_error_cancelled)
            LoginErrorType.MissingId -> getString(R.string.login_error_missing_id)
            is LoginErrorType.SignInFailed -> getString(R.string.login_error_sign_in_failed)
            is LoginErrorType.SaveFailed -> getString(R.string.login_error_save_failed)
        }
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
