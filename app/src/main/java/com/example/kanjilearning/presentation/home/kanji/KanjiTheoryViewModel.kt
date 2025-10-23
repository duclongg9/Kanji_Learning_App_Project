package com.example.kanjilearning.presentation.home.kanji

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.usecase.kanji.ObserveKanjiUseCase
import com.example.kanjilearning.domain.usecase.user.ObserveCurrentUserUseCase
import com.example.kanjilearning.domain.util.JlptLevel
import com.example.kanjilearning.domain.util.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * VI: ViewModel quản lý dữ liệu cho màn Kanji lý thuyết.
 */
@HiltViewModel
class KanjiTheoryViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val observeKanjiUseCase: ObserveKanjiUseCase
) : ViewModel() {

    /**
     * VI: Bộ lọc để người dùng chọn JLPT/độ khó.
     */
    data class Filter(
        val jlptLevel: JlptLevel? = null,
        val minDifficulty: Int = 1,
        val maxDifficulty: Int = 5
    )

    private val filterState = MutableStateFlow(Filter())

    /**
     * VI: StateFlow hiển thị lên UI, gồm role và danh sách Kanji theo bộ lọc hiện tại.
     */
    val uiState: StateFlow<KanjiTheoryUiState> = combine(
        observeCurrentUserUseCase(),
        filterState
    ) { user, filter ->
        (user?.role ?: Role.FREE) to filter
    }.flatMapLatest { (role, filter) ->
        observeKanjiUseCase(role, filter.jlptLevel, filter.minDifficulty, filter.maxDifficulty)
            .map { kanji ->
                KanjiTheoryUiState(
                    role = role,
                    kanji = kanji,
                    filter = filter
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KanjiTheoryUiState()
    )

    /**
     * VI: Hàm public cho UI thay đổi JLPT filter.
     */
    fun updateJlpt(level: JlptLevel?) {
        filterState.value = filterState.value.copy(jlptLevel = level)
    }

    /**
     * VI: Hàm public để chỉnh độ khó tối đa (ví dụ slider).
     */
    fun updateDifficulty(maxDifficulty: Int) {
        filterState.value = filterState.value.copy(maxDifficulty = maxDifficulty)
    }
}

/**
 * VI: UI state cho màn Kanji Theory.
 */
data class KanjiTheoryUiState(
    val role: Role = Role.FREE,
    val kanji: List<Kanji> = emptyList(),
    val filter: KanjiTheoryViewModel.Filter = KanjiTheoryViewModel.Filter()
)
