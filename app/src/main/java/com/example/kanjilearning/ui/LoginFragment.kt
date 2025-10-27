package com.example.kanjilearning.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.kanjilearning.BuildConfig
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.FragmentLoginBinding
import com.example.kanjilearning.ui.login.LoginErrorType
import com.example.kanjilearning.ui.login.LoginEvent
import com.example.kanjilearning.ui.login.LoginUiState
import com.example.kanjilearning.ui.login.LoginViewModel
import com.example.kanjilearning.oauth.GoogleOAuthConfig
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
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
    lateinit var identitySignInClient: SignInClient

    @Inject
    lateinit var signInIntentRequest: GetSignInIntentRequest

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    @Inject
    lateinit var googleOAuthConfig: GoogleOAuthConfig

    private var hasNavigated = false
    private var latestDeveloperErrorInfo: DeveloperErrorInfo? = null

    private val googleIdentityLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            val intent = result.data
            if (intent == null) {
                if (result.resultCode == Activity.RESULT_CANCELED) {
                    viewModel.onLoginCancelled()
                } else {
                    viewModel.onLoginFailed(null)
                }
                return@registerForActivityResult
            }
            try {
                val credential = identitySignInClient.getSignInCredentialFromIntent(intent)
                viewModel.onGoogleCredentialReceived(credential)
            } catch (error: ApiException) {
                when (error.statusCode) {
                    CommonStatusCodes.CANCELED -> viewModel.onLoginCancelled()
                    CommonStatusCodes.DEVELOPER_ERROR -> {
                        recordDeveloperError(error)
                        launchLegacySignIn()
                    }
                    else -> viewModel.onLoginFailed(error)
                }
            } catch (error: Exception) {
                viewModel.onLoginFailed(error)
            }
        }

    private val legacyGoogleLoginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val intent = result.data
            if (intent == null) {
                if (result.resultCode == Activity.RESULT_CANCELED) {
                    viewModel.onLoginCancelled()
                } else {
                    viewModel.onLoginFailed(null)
                }
                return@registerForActivityResult
            }
            try {
                val accountTask = GoogleSignIn.getSignedInAccountFromIntent(intent)
                val account = accountTask.getResult(ApiException::class.java)
                if (account != null) {
                    viewModel.onGoogleAccountReceived(account)
                } else {
                    viewModel.onLoginFailed(null)
                }
            } catch (error: ApiException) {
                when (error.statusCode) {
                    CommonStatusCodes.CANCELED -> viewModel.onLoginCancelled()
                    CommonStatusCodes.DEVELOPER_ERROR -> {
                        recordDeveloperError(error)
                        viewModel.onLoginFailed(error)
                    }
                    else -> viewModel.onLoginFailed(error)
                }
            } catch (error: Exception) {
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
            latestDeveloperErrorInfo = null
            launchIdentitySignIn()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { state -> renderState(state) } }
                launch { viewModel.events.collect { handleEvent(it) } }
            }
        }
    }

    private fun launchIdentitySignIn() {
        identitySignInClient.getSignInIntent(signInIntentRequest)
            .addOnSuccessListener { pendingIntent ->
                val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                googleIdentityLauncher.launch(request)
            }
            .addOnFailureListener { error ->
                val apiException = (error as? ApiException)
                if (apiException?.statusCode == CommonStatusCodes.DEVELOPER_ERROR) {
                    recordDeveloperError(apiException)
                    launchLegacySignIn()
                } else {
                    viewModel.onLoginFailed(error)
                }
            }
    }

    private fun launchLegacySignIn() {
        val intent = googleSignInClient.signInIntent
        legacyGoogleLoginLauncher.launch(intent)
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
            is LoginErrorType.SignInFailed -> {
                buildSignInFailedMessage(type)
            }
            is LoginErrorType.SaveFailed -> {
                val base = getString(R.string.login_error_save_failed)
                val detail = type.cause.localizedMessage
                if (detail.isNullOrBlank()) base else "$base\n$detail"
            }
        }
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun buildSignInFailedMessage(type: LoginErrorType.SignInFailed): String {
        val apiException = type.cause as? ApiException
        if (apiException?.statusCode == CommonStatusCodes.DEVELOPER_ERROR) {
            if (latestDeveloperErrorInfo == null && apiException != null) {
                recordDeveloperError(apiException, shouldLog = false)
            }
            val info = latestDeveloperErrorInfo
            val resolvedClientId = info?.resolvedClientId
                ?: getString(R.string.login_error_sign_in_developer_error_unknown_id)
            val packageName = info?.packageName ?: requireContext().packageName
            return getString(
                R.string.login_error_sign_in_developer_error,
                packageName,
                resolvedClientId
            )
        }

        latestDeveloperErrorInfo = null
        val base = getString(R.string.login_error_sign_in_failed)
        val detail = apiException?.let { exception ->
            val code = exception.statusCode
            val statusName = CommonStatusCodes.getStatusCodeString(code)
            "$statusName ($code)"
        } ?: type.cause?.localizedMessage
        return if (detail.isNullOrBlank()) base else "$base\n$detail"
    }

    private fun recordDeveloperError(error: ApiException, shouldLog: Boolean = true) {
        val context = requireContext()
        val packageName = context.packageName
        val candidateSources = listOf(
            "res/raw/google_oauth_client.json" to googleOAuthConfig.clientId,
            "res/values/strings.xml#default_web_client_id" to readStringResource("default_web_client_id"),
            "BuildConfig.GOOGLE_WEB_CLIENT_ID" to BuildConfig.GOOGLE_WEB_CLIENT_ID
        ).map { (source, value) -> source to value?.trim().orEmpty() }

        val resolvedClientId = candidateSources
            .mapNotNull { (_, value) ->
                value.takeIf { it.isNotEmpty() && !it.contains("placeholder", ignoreCase = true) }
            }
            .firstOrNull()

        latestDeveloperErrorInfo = DeveloperErrorInfo(
            packageName = packageName,
            resolvedClientId = resolvedClientId,
            candidateClientIds = candidateSources
        )

        if (shouldLog) {
            val logMessage = buildString {
                append("Google Sign-In developer error (status ")
                append(error.statusCode)
                append("). Package: ")
                append(packageName)
                append(". Candidate OAuth client IDs:")
                candidateSources.forEach { (source, value) ->
                    append("\n- ")
                    append(source)
                    append(": ")
                    append(if (value.isEmpty()) "<missing>" else value)
                }
                append("\nEnsure the SHA-1 fingerprints for this build variant are registered in the Google Cloud Console, then download and include an updated google-services.json file.")
            }
            Log.e("GoogleAuth", logMessage, error)
        }
    }

    private fun readStringResource(name: String): String? {
        val context = requireContext()
        val resId = context.resources.getIdentifier(name, "string", context.packageName)
        if (resId == 0) return null
        return runCatching { context.getString(resId).trim() }
            .getOrNull()
    }

    private data class DeveloperErrorInfo(
        val packageName: String,
        val resolvedClientId: String?,
        val candidateClientIds: List<Pair<String, String>>
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
