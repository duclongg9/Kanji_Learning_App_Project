package com.example.kanjilearning.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.model.UserAccount
import com.example.kanjilearning.domain.model.UserRole
import com.example.kanjilearning.domain.usecase.LoadAvailableRolesUseCase
import com.example.kanjilearning.domain.usecase.LoginUserUseCase
import com.example.kanjilearning.domain.usecase.LogoutUserUseCase
import com.example.kanjilearning.domain.usecase.ObserveAuthSessionUseCase
import com.example.kanjilearning.domain.usecase.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * VI: ViewModel điều phối luồng đăng nhập/đăng ký và trạng thái session.
 * EN: ViewModel coordinating login/registration flows and session state.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val loadAvailableRolesUseCase: LoadAvailableRolesUseCase,
    private val observeAuthSessionUseCase: ObserveAuthSessionUseCase,
    private val logoutUserUseCase: LogoutUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthViewState())
    private val state: StateFlow<AuthViewState> = _state.asStateFlow()

    /**
     * VI: Cho phép tầng View (Java) quan sát LiveData trạng thái.
     * EN: Exposes LiveData so the Java view layer can observe updates.
     */
    val stateLiveData: LiveData<AuthViewState> = state.asLiveData()

    init {
        viewModelScope.launch {
            observeAuthSessionUseCase().collectLatest { account ->
                _state.update { current -> current.copy(session = account) }
            }
        }
        refreshRoles()
    }

    /**
     * VI: Tải lại danh sách role từ server.
     * EN: Refreshes role options from the backend.
     */
    fun refreshRoles() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { loadAvailableRolesUseCase() }
                .onSuccess { roles ->
                    _state.update { it.copy(isLoading = false, availableRoles = roles) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Không thể tải role")
                    }
                }
        }
    }

    /**
     * VI: Thực hiện đăng nhập.
     * EN: Triggers the login action.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { loginUserUseCase(email, password) }
                .onSuccess { account ->
                    _state.update { it.copy(isLoading = false, session = account) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Đăng nhập thất bại"
                        )
                    }
                }
        }
    }

    /**
     * VI: Đăng ký người dùng mới với role được chọn.
     * EN: Registers a new account using the selected role.
     */
    fun register(email: String, password: String, displayName: String, roleCode: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { registerUserUseCase(email, password, displayName, roleCode) }
                .onSuccess { account ->
                    _state.update { it.copy(isLoading = false, session = account) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Đăng ký thất bại"
                        )
                    }
                }
        }
    }

    /**
     * VI: Xoá lỗi hiển thị hiện tại.
     * EN: Clears any displayed error message.
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * VI: Đăng xuất khỏi hệ thống.
     * EN: Logs the current session out.
     */
    fun logout() {
        viewModelScope.launch {
            runCatching { logoutUserUseCase() }
            _state.update { it.copy(session = null) }
        }
    }
}

/**
 * VI: Trạng thái UI của luồng xác thực.
 * EN: UI state container for authentication screens.
 */
data class AuthViewState(
    val isLoading: Boolean = false,
    val availableRoles: List<UserRole> = emptyList(),
    val errorMessage: String? = null,
    val session: UserAccount? = null
)
