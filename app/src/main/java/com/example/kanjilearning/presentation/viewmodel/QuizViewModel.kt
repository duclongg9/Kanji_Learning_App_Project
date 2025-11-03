package com.example.kanjilearning.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.model.QuizSession
import com.example.kanjilearning.domain.usecase.LoadQuizUseCase
import com.example.kanjilearning.domain.usecase.RecordQuizResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * VI: ViewModel điều khiển luồng quiz (chọn đáp án, cộng điểm, lưu tiến độ).
 * EN: Manages quiz state, scoring and progress persistence.
 */
@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadQuizUseCase: LoadQuizUseCase,
    private val recordQuizResultUseCase: RecordQuizResultUseCase
) : ViewModel() {

    private val lessonId: Long = savedStateHandle.get<Long>("lessonId")
        ?: error("lessonId arg is required")

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    init {
        loadQuiz()
    }

    fun loadQuiz() {
        viewModelScope.launch {
            _state.value = QuizUiState(isLoading = true)
            runCatching { loadQuizUseCase(lessonId) }
                .onSuccess { session ->
                    _state.value = QuizUiState(
                        isLoading = false,
                        session = session,
                        currentIndex = 0,
                        selectedChoiceId = null,
                        score = 0,
                        isAnswerRevealed = false,
                        isFinished = false,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _state.value = QuizUiState(isLoading = false, errorMessage = error.message)
                }
        }
    }

    fun selectChoice(choiceId: Long) {
        val current = _state.value
        if (current.isLoading || current.isFinished || current.isAnswerRevealed) return
        _state.value = current.copy(selectedChoiceId = choiceId)
    }

    fun submitAnswer() {
        val current = _state.value
        val session = current.session ?: return
        if (current.selectedChoiceId == null || current.isAnswerRevealed) return
        val question = session.questions[current.currentIndex]
        val selectedChoice = question.choices.firstOrNull { it.id == current.selectedChoiceId } ?: return
        val increment = if (selectedChoice.isCorrect) 1 else 0
        _state.value = current.copy(
            score = current.score + increment,
            isAnswerRevealed = true
        )
    }

    fun goToNext() {
        val current = _state.value
        val session = current.session ?: return
        if (!current.isAnswerRevealed) return
        val nextIndex = current.currentIndex + 1
        if (nextIndex >= session.questions.size) {
            finishQuiz()
        } else {
            _state.value = current.copy(
                currentIndex = nextIndex,
                selectedChoiceId = null,
                isAnswerRevealed = false
            )
        }
    }

    private fun finishQuiz() {
        val current = _state.value
        val session = current.session ?: return
        if (current.isFinished) return
        _state.value = current.copy(isFinished = true)
        viewModelScope.launch {
            runCatching {
                recordQuizResultUseCase(lessonId, current.score, session.questions.size)
            }.onFailure { error ->
                _state.value = _state.value.copy(errorMessage = error.message)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}

data class QuizUiState(
    val isLoading: Boolean = true,
    val session: QuizSession? = null,
    val currentIndex: Int = 0,
    val selectedChoiceId: Long? = null,
    val score: Int = 0,
    val isAnswerRevealed: Boolean = false,
    val isFinished: Boolean = false,
    val errorMessage: String? = null
)
