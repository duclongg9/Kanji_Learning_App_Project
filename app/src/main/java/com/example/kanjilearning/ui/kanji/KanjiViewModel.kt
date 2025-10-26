package com.example.kanjilearning.ui.kanji

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.repository.KanjiRepository
import com.example.kanjilearning.domain.repository.UserRepository
import com.example.kanjilearning.domain.util.AccessTier
import com.example.kanjilearning.domain.util.JlptLevel
import com.example.kanjilearning.domain.util.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * VI: ViewModel kết nối Room để hiển thị danh sách Kanji phù hợp với quyền truy cập.
 */
@HiltViewModel
class KanjiViewModel @Inject constructor(
    private val kanjiRepository: KanjiRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KanjiUiState())
    val uiState: StateFlow<KanjiUiState> = _uiState.asStateFlow()

    init {
        observeKanjiForCurrentRole()
    }

    private fun observeKanjiForCurrentRole() {
        viewModelScope.launch {
            userRepository.observeCurrentUser()
                .map { it?.role ?: Role.FREE }
                .distinctUntilChanged()
                .flatMapLatest { role ->
                    kanjiRepository.observeKanji(
                        jlptLevel = null,
                        minDifficulty = MIN_DIFFICULTY,
                        maxDifficulty = MAX_DIFFICULTY,
                        allowedTiers = allowedTiersFor(role)
                    ).map { kanji ->
                        KanjiUiState(
                            isLoading = false,
                            role = role,
                            sections = buildSections(kanji)
                        )
                    }.onStart { emit(KanjiUiState(isLoading = true, role = role)) }
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    private fun buildSections(items: List<Kanji>): List<KanjiSectionUi> {
        if (items.isEmpty()) {
            return JlptLevel.entries.map { level ->
                KanjiSectionUi(level = level, entries = emptyList())
            }
        }
        val grouped = items.groupBy { it.jlptLevel }
        return JlptLevel.entries.map { level ->
            KanjiSectionUi(
                level = level,
                entries = grouped[level].orEmpty().sortedBy { it.difficulty }.map { kanji ->
                    KanjiEntryUi(
                        character = kanji.character,
                        meaning = kanji.meaning,
                        onyomi = kanji.onyomi,
                        kunyomi = kanji.kunyomi,
                        difficulty = kanji.difficulty
                    )
                }
            )
        }
    }

    companion object {
        private const val MIN_DIFFICULTY = 0
        private const val MAX_DIFFICULTY = 10

        private fun allowedTiersFor(role: Role): List<AccessTier> = when (role) {
            Role.ADMIN -> AccessTier.entries
            Role.VIP -> listOf(AccessTier.FREE, AccessTier.VIP)
            Role.FREE -> listOf(AccessTier.FREE)
        }
    }
}

/**
 * VI: State hiển thị toàn màn hình Kanji.
 */
data class KanjiUiState(
    val isLoading: Boolean = true,
    val role: Role = Role.FREE,
    val sections: List<KanjiSectionUi> = emptyList()
)

/**
 * VI: Dữ liệu từng section Kanji theo cấp độ.
 */
data class KanjiSectionUi(
    val level: JlptLevel,
    val entries: List<KanjiEntryUi>
)

/**
 * VI: Mô tả một Kanji hiển thị cho user.
 */
data class KanjiEntryUi(
    val character: String,
    val meaning: String,
    val onyomi: String,
    val kunyomi: String,
    val difficulty: Int
)
