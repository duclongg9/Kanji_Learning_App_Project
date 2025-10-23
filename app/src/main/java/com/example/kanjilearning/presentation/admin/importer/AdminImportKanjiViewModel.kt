package com.example.kanjilearning.presentation.admin.importer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.repository.KanjiRepository
import com.example.kanjilearning.domain.util.AccessTier
import com.example.kanjilearning.domain.util.JlptLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * VI: ViewModel xử lý import CSV Kanji.
 */
@HiltViewModel
class AdminImportKanjiViewModel @Inject constructor(
    private val kanjiRepository: KanjiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminImportUiState())
    val state: StateFlow<AdminImportUiState> = _state.asStateFlow()

    /**
     * VI: Hàm nhận nội dung CSV dạng text và import.
     * Định dạng mong đợi: character,onyomi,kunyomi,meaning,difficulty
     */
    fun importFromCsv(
        csvContent: String,
        jlptLevel: JlptLevel,
        accessTier: AccessTier
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, message = null) }
            try {
                val lines = csvContent.lines().filter { it.isNotBlank() }
                val dataLines = if (lines.isNotEmpty() && lines.first().contains("character", true)) {
                    lines.drop(1)
                } else {
                    lines
                }
                val kanjiList = dataLines.mapIndexed { index, line ->
                    val columns = line.split(",")
                    Kanji(
                        id = 0,
                        character = columns.getOrNull(0)?.trim().orEmpty(),
                        onyomi = columns.getOrNull(1)?.trim().orEmpty(),
                        kunyomi = columns.getOrNull(2)?.trim().orEmpty(),
                        meaning = columns.getOrNull(3)?.trim().orEmpty(),
                        jlptLevel = jlptLevel,
                        difficulty = columns.getOrNull(4)?.trim()?.toIntOrNull() ?: (index % 5 + 1),
                        accessTier = accessTier
                    )
                }
                kanjiRepository.importKanji(kanjiList)
                _state.update { it.copy(isProcessing = false, message = "Import thành công ${kanjiList.size} Kanji") }
            } catch (e: Exception) {
                _state.update { it.copy(isProcessing = false, message = e.message ?: "Import thất bại") }
            }
        }
    }
}

/**
 * VI: State màn import Kanji.
 */
data class AdminImportUiState(
    val isProcessing: Boolean = false,
    val message: String? = null
)
