package com.example.kanjilearning.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.model.User
import com.example.kanjilearning.domain.usecase.user.SaveUserUseCase
import com.example.kanjilearning.domain.usecase.user.SyncUserRoleUseCase
import com.example.kanjilearning.domain.util.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * VI: ViewModel điều khiển màn đăng nhập Google.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val saveUserUseCase: SaveUserUseCase,
    private val syncUserRoleUseCase: SyncUserRoleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    /**
     * VI: Gọi khi Google Sign-In thành công. online = true nếu có mạng để sync role.
     */
    fun onGoogleSignInSuccess(
        googleId: String,
        displayName: String,
        email: String,
        online: Boolean
    ) {
        viewModelScope.launch {
            _state.value = LoginUiState(isLoading = true)
            try {
                val initialUser = User(
                    googleId = googleId,
                    displayName = displayName,
                    email = email,
                    role = Role.FREE,
                    lastSyncedAt = System.currentTimeMillis()
                )
                val syncedRole = syncUserRoleUseCase(initialUser, online)
                val finalUser = initialUser.copy(role = syncedRole)
                saveUserUseCase(finalUser)
                _state.value = LoginUiState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = LoginUiState(errorMessage = e.message ?: "Lỗi không xác định")
            }
        }
    }
}

/**
 * VI: UI state cho màn đăng nhập.
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
