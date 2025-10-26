package com.example.kanjilearning.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.model.User
import com.example.kanjilearning.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * VI: ViewModel hiển thị thông tin người dùng hiện tại trên màn hình profile.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.observeCurrentUser().collect { user ->
                _uiState.value = ProfileUiState(user)
            }
        }
    }
}

/**
 * VI: State UI chứa thông tin user.
 */
data class ProfileUiState(
    val user: User? = null
)
