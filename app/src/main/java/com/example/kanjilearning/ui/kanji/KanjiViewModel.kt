package com.example.kanjilearning.ui.kanji

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.repository.KanjiRepository
import com.example.kanjilearning.domain.repository.UserRepository
import com.example.kanjilearning.domain.usecase.kanji.CreateKanjiUseCase
import com.example.kanjilearning.domain.usecase.kanji.DeleteKanjiUseCase
import com.example.kanjilearning.domain.usecase.kanji.UpdateKanjiUseCase
import com.example.kanjilearning.domain.util.AccessTier
import com.example.kanjilearning.domain.util.JlptLevel
import com.example.kanjilearning.domain.util.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * VI: ViewModel kết nối Room để hiển thị danh sách Kanji phù hợp với quyền truy cập.
 */
@HiltViewModel
class KanjiViewModel @Inject constructor(
    private val kanjiRepository: KanjiRepository,
    private val userRepository: UserRepository,
    private val createKanjiUseCase: CreateKanjiUseCase,
    private val updateKanjiUseCase: UpdateKanjiUseCase,
    private val deleteKanjiUseCase: DeleteKanjiUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(KanjiUiState())
    val uiState: StateFlow<KanjiUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<KanjiEvent>()
    val events: SharedFlow<KanjiEvent> = _events.asSharedFlow()

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
                            sections = buildSections(kanji),
                            isMutating = _uiState.value.isMutating
                        )
                    }.onStart {
                        emit(
                            KanjiUiState(
                                isLoading = true,
                                role = role,
                                sections = emptyList(),
                                isMutating = _uiState.value.isMutating
                            )
                        )
                    }
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    /**
     * VI: Thêm mới Kanji sau khi người dùng nhập biểu mẫu.
     */
    fun createKanji(input: KanjiFormInput) {
        val error = validate(input)
        if (error != null) {
            emitValidation(error)
            return
        }
        val kanji = input.toDomain()
        viewModelScope.launch {
            setMutating(true)
            try {
                createKanjiUseCase(kanji)
                _events.emit(KanjiEvent.ShowMessage(KanjiMessage.Created))
            } catch (error: Exception) {
                _events.emit(KanjiEvent.ShowError(error.localizedMessage ?: "Không thể tạo Kanji mới"))
            } finally {
                setMutating(false)
            }
        }
    }

    /**
     * VI: Cập nhật Kanji hiện có với dữ liệu mới.
     */
    fun updateKanji(id: Long, input: KanjiFormInput) {
        val error = validate(input)
        if (error != null) {
            emitValidation(error)
            return
        }
        val kanji = input.toDomain(id)
        viewModelScope.launch {
            setMutating(true)
            try {
                updateKanjiUseCase(kanji)
                _events.emit(KanjiEvent.ShowMessage(KanjiMessage.Updated))
            } catch (error: Exception) {
                _events.emit(KanjiEvent.ShowError(error.localizedMessage ?: "Không thể cập nhật Kanji"))
            } finally {
                setMutating(false)
            }
        }
    }

    /**
     * VI: Xoá Kanji theo ID.
     */
    fun deleteKanji(id: Long) {
        viewModelScope.launch {
            setMutating(true)
            try {
                deleteKanjiUseCase(id)
                _events.emit(KanjiEvent.ShowMessage(KanjiMessage.Deleted))
            } catch (error: Exception) {
                _events.emit(KanjiEvent.ShowError(error.localizedMessage ?: "Không thể xoá Kanji"))
            } finally {
                setMutating(false)
            }
        }
    }

    private fun setMutating(value: Boolean) {
        _uiState.update { current -> current.copy(isMutating = value) }
    }

    private fun validate(input: KanjiFormInput): KanjiFormError? = when {
        input.character.isBlank() -> KanjiFormError.CharacterBlank
        input.meaning.isBlank() -> KanjiFormError.MeaningBlank
        else -> null
    }

    private fun emitValidation(error: KanjiFormError) {
        viewModelScope.launch {
            _events.emit(KanjiEvent.ShowValidation(error))
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
                entries = grouped[level].orEmpty()
                    .sortedBy { it.difficulty }
                    .map { kanji ->
                        KanjiEntryUi(
                            id = kanji.id,
                            character = kanji.character,
                            meaning = kanji.meaning,
                            onyomi = kanji.onyomi,
                            kunyomi = kanji.kunyomi,
                            difficulty = kanji.difficulty,
                            accessTier = kanji.accessTier,
                            jlptLevel = kanji.jlptLevel
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
    val sections: List<KanjiSectionUi> = emptyList(),
    val isMutating: Boolean = false
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
    val id: Long,
    val character: String,
    val meaning: String,
    val onyomi: String,
    val kunyomi: String,
    val difficulty: Int,
    val accessTier: AccessTier,
    val jlptLevel: JlptLevel
)

/**
 * VI: Dữ liệu từ biểu mẫu tạo/sửa Kanji.
 */
data class KanjiFormInput(
    val character: String,
    val meaning: String,
    val onyomi: String,
    val kunyomi: String,
    val jlptLevel: JlptLevel,
    val accessTier: AccessTier,
    val difficulty: Int
) {
    fun toDomain(id: Long = 0L): Kanji = Kanji(
        id = id,
        character = character.trim(),
        meaning = meaning.trim(),
        onyomi = onyomi.trim(),
        kunyomi = kunyomi.trim(),
        jlptLevel = jlptLevel,
        accessTier = accessTier,
        difficulty = difficulty
    )
}

/**
 * VI: Phân loại thông điệp hiển thị sau CRUD.
 */
enum class KanjiMessage(@StringRes val messageRes: Int) {
    Created(com.example.kanjilearning.R.string.kanji_message_created),
    Updated(com.example.kanjilearning.R.string.kanji_message_updated),
    Deleted(com.example.kanjilearning.R.string.kanji_message_deleted)
}

/**
 * VI: Thông báo một lần để Fragment hiện Snackbar.
 */
sealed class KanjiEvent {
    data class ShowMessage(val type: KanjiMessage) : KanjiEvent()
    data class ShowValidation(val error: KanjiFormError) : KanjiEvent()
    data class ShowError(val message: String) : KanjiEvent()
}

/**
 * VI: Các lỗi xác thực biểu mẫu Kanji.
 */
sealed class KanjiFormError {
    data object CharacterBlank : KanjiFormError()
    data object MeaningBlank : KanjiFormError()
}
