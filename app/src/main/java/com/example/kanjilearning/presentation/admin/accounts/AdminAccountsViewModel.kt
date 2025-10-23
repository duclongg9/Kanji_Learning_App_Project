package com.example.kanjilearning.presentation.admin.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.usecase.user.ObserveCurrentUserUseCase
import com.example.kanjilearning.domain.usecase.user.UpdateRoleUseCase
import com.example.kanjilearning.domain.util.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * VI: ViewModel giúp admin nâng cấp role người dùng.
 */
@HiltViewModel
class AdminAccountsViewModel @Inject constructor(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val updateRoleUseCase: UpdateRoleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminAccountsUiState())
    val state: StateFlow<AdminAccountsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                _state.update { it.copy(currentRole = user?.role ?: Role.FREE) }
            }
        }
    }

    /**
     * VI: Giả lập việc nâng cấp role (ví dụ chọn user hiện hành thành VIP).
     */
    fun upgradeToVip() {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }
            try {
                updateRoleUseCase(Role.VIP)
                _state.update { it.copy(isProcessing = false, message = "Đã nâng cấp lên VIP") }
            } catch (e: Exception) {
                _state.update { it.copy(isProcessing = false, message = e.message ?: "Lỗi") }
            }
        }
    }
}

/**
 * VI: State màn quản trị tài khoản.
 */
data class AdminAccountsUiState(
    val currentRole: Role = Role.FREE,
    val isProcessing: Boolean = false,
    val message: String? = null
)
