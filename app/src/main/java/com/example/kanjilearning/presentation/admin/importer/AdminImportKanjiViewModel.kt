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
                val rawLines = csvContent.lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                val hasHeader = rawLines.firstOrNull()?.contains("character", ignoreCase = true) == true
                val dataLines = if (hasHeader) rawLines.drop(1) else rawLines

                var totalImported = 0
                dataLines
                    .chunked(IMPORT_BATCH_SIZE)
                    .forEach { chunk ->
                        val chunkList = chunk.mapIndexed { indexInChunk, line ->
                            val columns = line.split(",")
                            val absoluteIndex = totalImported + indexInChunk
                            Kanji(
                                id = 0,
                                character = columns.getOrNull(0)?.trim().orEmpty(),
                                onyomi = columns.getOrNull(1)?.trim().orEmpty(),
                                kunyomi = columns.getOrNull(2)?.trim().orEmpty(),
                                meaning = columns.getOrNull(3)?.trim().orEmpty(),
                                jlptLevel = jlptLevel,
                                difficulty = columns.getOrNull(4)?.trim()?.toIntOrNull()
                                    ?: ((absoluteIndex % DEFAULT_DIFFICULTY_RANGE) + 1),
                                accessTier = accessTier
                            )
                        }
                        kanjiRepository.importKanji(chunkList)
                        totalImported += chunkList.size
                    }

                _state.update {
                    it.copy(
                        isProcessing = false,
                        message = "Import thành công $totalImported Kanji"
                    )
                }
                            } catch (e: Exception) {
                _state.update { it.copy(isProcessing = false, message = e.message ?: "Import thất bại") }
            }
        }
    }

    companion object {
        private const val IMPORT_BATCH_SIZE = 500
        private const val DEFAULT_DIFFICULTY_RANGE = 5
    }
}

/**
 * VI: State màn import Kanji.
 */
data class AdminImportUiState(
    val isProcessing: Boolean = false,
    val message: String? = null
)
