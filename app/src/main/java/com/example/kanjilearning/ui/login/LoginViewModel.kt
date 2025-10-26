package com.example.kanjilearning.ui.login

import com.example.kanjilearning.domain.model.User
import com.example.kanjilearning.domain.repository.UserRepository
import com.example.kanjilearning.domain.util.Role
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.libraries.identity.googleid.GoogleIdCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

/**
 * VI: ViewModel xử lý đăng nhập Google và lưu user vào DB.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    init {
        observeSignedInUser()
    }

    private fun observeSignedInUser() {
        viewModelScope.launch {
            userRepository.observeCurrentUser().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (user != null) {
                    _events.send(LoginEvent.NavigateNext)
                }
            }
        }
    }

    fun onGoogleCredentialReceived(credential: GoogleIdCredential) {
        val googleId = credential.idToken ?: credential.id
        val email = credential.id
        val displayName = credential.displayName.orEmpty()
        persistUser(googleId, email, displayName)
    }

    fun onGoogleAccountReceived(account: GoogleSignInAccount) {
        val googleId = account.id ?: account.idToken ?: account.email
        val email = account.email
        val displayName = account.displayName
        persistUser(googleId, email, displayName)
    }

    fun onLoginCancelled() {
        emitError(LoginEvent.ShowError(LoginErrorType.Cancelled))
    }

    fun onLoginFailed(exception: Exception?) {
        emitError(LoginEvent.ShowError(LoginErrorType.SignInFailed(exception)))
    }

    private fun emitError(event: LoginEvent.ShowError) {
        viewModelScope.launch {
            _events.send(event)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun persistUser(
        googleId: String?,
        email: String?,
        displayName: String?
    ) {
        if (googleId.isNullOrBlank()) {
            emitError(LoginEvent.ShowError(LoginErrorType.MissingId))
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = User(
                    googleId = googleId,
                    displayName = displayName.orEmpty(),
                    email = email.orEmpty(),
                    role = Role.FREE,
                    lastSyncedAt = 0L
                )
                userRepository.saveUser(user)
                _events.send(LoginEvent.NavigateNext)
            } catch (error: Exception) {
                emitError(LoginEvent.ShowError(LoginErrorType.SaveFailed(error)))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

/**
 * VI: Trạng thái UI cho màn hình đăng nhập.
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null
)

/**
 * VI: Các sự kiện một lần cho màn hình đăng nhập.
 */
sealed class LoginEvent {
    object NavigateNext : LoginEvent()
    data class ShowError(val type: LoginErrorType) : LoginEvent()
}

/**
 * VI: Phân loại lỗi để hiển thị thông báo phù hợp.
 */
sealed class LoginErrorType {
    data object Cancelled : LoginErrorType()
    data object MissingId : LoginErrorType()
    data class SignInFailed(val cause: Exception?) : LoginErrorType()
    data class SaveFailed(val cause: Exception) : LoginErrorType()
}
