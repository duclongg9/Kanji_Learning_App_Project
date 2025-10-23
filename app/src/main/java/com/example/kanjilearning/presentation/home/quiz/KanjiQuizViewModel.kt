package com.example.kanjilearning.presentation.home.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.usecase.kanji.ObserveKanjiUseCase
import com.example.kanjilearning.domain.usecase.user.ObserveCurrentUserUseCase
import com.example.kanjilearning.domain.util.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * VI: ViewModel quản lý dữ liệu cho phần luyện trắc nghiệm SRS.
 */
@HiltViewModel
class KanjiQuizViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val observeKanjiUseCase: ObserveKanjiUseCase
) : ViewModel() {

    /**
     * VI: UI state gồm danh sách flashcard và role.
     */
    val uiState: StateFlow<KanjiQuizUiState> = observeCurrentUserUseCase()
        .map { user -> user?.role ?: Role.FREE }
        .flatMapLatest { role ->
            observeKanjiUseCase(role, null, 1, 5).map { kanji ->
                KanjiQuizUiState(
                    role = role,
                    cards = kanji
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = KanjiQuizUiState()
        )

    /**
     * VI: MutableStateFlow lưu index thẻ hiện hành.
     */
    private val currentIndex = MutableStateFlow(0)

    fun moveNext(total: Int) {
        if (total == 0) return
        currentIndex.value = (currentIndex.value + 1) % total
    }

    fun movePrevious(total: Int) {
        if (total == 0) return
        currentIndex.value = if (currentIndex.value - 1 < 0) total - 1 else currentIndex.value - 1
    }

    /**
     * VI: Lấy index hiện tại cho UI (ví dụ hiển thị số thứ tự).
     */
    val currentCardIndex: StateFlow<Int> = currentIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}

/**
 * VI: State hiển thị flashcard Kanji.
 */
data class KanjiQuizUiState(
    val role: Role = Role.FREE,
    val cards: List<Kanji> = emptyList()
)
