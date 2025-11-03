package com.example.kanjilearning.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.model.CourseItem
import com.example.kanjilearning.domain.usecase.ObserveCoursesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * VI: ViewModel cho màn course list, tính toán % hoàn thành tổng.
 * EN: Exposes course list state plus aggregated progress for the dashboard.
 */
@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val observeCoursesUseCase: ObserveCoursesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CourseListUiState())
    val state: StateFlow<CourseListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeCoursesUseCase()
                .onStart { _state.value = _state.value.copy(isLoading = true) }
                .catch { throwable ->
                    _state.value = _state.value.copy(isLoading = false, errorMessage = throwable.message)
                }
                .collect { courses ->
                    _state.value = CourseListUiState.fromCourses(courses)
                }
        }
    }
}

data class CourseListUiState(
    val isLoading: Boolean = false,
    val courses: List<CourseItem> = emptyList(),
    val overallPercent: Int = 0,
    val completedLessons: Int = 0,
    val totalLessons: Int = 0,
    val errorMessage: String? = null
) {
    companion object {
        fun fromCourses(courses: List<CourseItem>): CourseListUiState {
            val totalLessons = courses.sumOf { it.lessonCount }
            val completed = courses.sumOf { it.completedLessons }
            val percent = if (totalLessons == 0) 0 else ((completed.toDouble() / totalLessons) * 100).toInt()
            return CourseListUiState(
                isLoading = false,
                courses = courses,
                overallPercent = percent,
                completedLessons = completed,
                totalLessons = totalLessons,
                errorMessage = null
            )
        }
    }
}
