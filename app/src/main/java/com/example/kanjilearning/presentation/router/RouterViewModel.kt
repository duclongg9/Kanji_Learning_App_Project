package com.example.kanjilearning.presentation.router

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.usecase.user.GetCurrentUserUseCase
import com.example.kanjilearning.domain.util.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * VI: ViewModel quyết định điều hướng dựa trên role đã lưu.
 */
@HiltViewModel
class RouterViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _destination = MutableStateFlow<Role?>(null)
    val destination: StateFlow<Role?> = _destination

    fun resolveDestination() {
        viewModelScope.launch {
            val role = getCurrentUserUseCase()?.role ?: Role.FREE
            _destination.value = role
        }
    }
}
