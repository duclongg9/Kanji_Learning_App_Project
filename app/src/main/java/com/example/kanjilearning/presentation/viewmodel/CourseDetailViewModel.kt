package com.example.kanjilearning.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.model.CourseDetail
import com.example.kanjilearning.domain.model.PaymentReceipt
import com.example.kanjilearning.domain.usecase.ObserveCourseDetailUseCase
import com.example.kanjilearning.domain.usecase.UnlockCourseWithMomoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * VI: ViewModel cho màn chi tiết khoá học, xử lý mở khóa qua MoMo.
 * EN: Drives the course detail screen and MoMo unlock workflow.
 */
@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeCourseDetailUseCase: ObserveCourseDetailUseCase,
    private val unlockCourseWithMomoUseCase: UnlockCourseWithMomoUseCase
) : ViewModel() {

    private val courseId: Long = savedStateHandle.get<Long>("courseId")
        ?: error("courseId arg is required")

    private val _state = MutableStateFlow(CourseDetailUiState())
    val state: StateFlow<CourseDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeCourseDetailUseCase(courseId)
                .onStart { _state.value = _state.value.copy(isLoading = true) }
                .catch { throwable ->
                    _state.value = _state.value.copy(isLoading = false, errorMessage = throwable.message)
                }
                .collect { detail ->
                    _state.value = _state.value.copy(isLoading = false, course = detail, errorMessage = null)
                }
        }
    }

    fun unlockCourse(phoneNumber: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUnlocking = true)
            runCatching {
                unlockCourseWithMomoUseCase(courseId, phoneNumber)
            }.onSuccess { receipt ->
                _state.value = _state.value.copy(isUnlocking = false, lastReceipt = receipt)
            }.onFailure { error ->
                _state.value = _state.value.copy(isUnlocking = false, errorMessage = error.message)
            }
        }
    }

    fun consumeReceipt() {
        _state.value = _state.value.copy(lastReceipt = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}

data class CourseDetailUiState(
    val isLoading: Boolean = true,
    val isUnlocking: Boolean = false,
    val course: CourseDetail? = null,
    val errorMessage: String? = null,
    val lastReceipt: PaymentReceipt? = null
)
