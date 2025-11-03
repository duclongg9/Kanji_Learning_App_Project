package com.example.kanjilearning.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.model.LessonDetailModel
import com.example.kanjilearning.domain.usecase.ObserveLessonDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * VI: ViewModel cung cấp dữ liệu chi tiết lesson và danh sách Kanji.
 * EN: Exposes lesson detail with kanji list to the UI.
 */
@HiltViewModel
class LessonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeLessonDetailUseCase: ObserveLessonDetailUseCase
) : ViewModel() {

    private val lessonId: Long = savedStateHandle.get<Long>("lessonId")
        ?: error("lessonId arg is required")

    private val _state = MutableStateFlow(LessonDetailUiState())
    val state: StateFlow<LessonDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeLessonDetailUseCase(lessonId)
                .onStart { _state.value = _state.value.copy(isLoading = true) }
                .catch { throwable ->
                    _state.value = _state.value.copy(isLoading = false, errorMessage = throwable.message)
                }
                .collect { detail ->
                    _state.value = LessonDetailUiState(isLoading = false, lesson = detail, errorMessage = null)
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}

data class LessonDetailUiState(
    val isLoading: Boolean = true,
    val lesson: LessonDetailModel? = null,
    val errorMessage: String? = null
)
