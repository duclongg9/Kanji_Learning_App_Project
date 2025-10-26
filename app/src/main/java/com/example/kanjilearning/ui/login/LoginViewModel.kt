package com.example.kanjilearning.ui.login

import com.example.kanjilearning.domain.model.User
import com.example.kanjilearning.domain.repository.UserRepository
import com.example.kanjilearning.domain.util.Role
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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

    fun onGoogleAccountReceived(account: GoogleSignInAccount) {
        val googleId = account.id ?: account.idToken ?: account.email
        if (googleId.isNullOrBlank()) {
            emitError(LoginEvent.ShowError(LoginErrorType.MissingId))
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = User(
                    googleId = googleId,
                    displayName = account.displayName.orEmpty(),
                    email = account.email.orEmpty(),
                    role = Role.FREE,
                    lastSyncedAt = System.currentTimeMillis()
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
