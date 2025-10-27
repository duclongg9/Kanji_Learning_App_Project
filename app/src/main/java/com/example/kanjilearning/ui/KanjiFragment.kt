package com.example.kanjilearning.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.DialogKanjiFormBinding
import com.example.kanjilearning.databinding.FragmentKanjiBinding
import com.example.kanjilearning.domain.util.AccessTier
import com.example.kanjilearning.domain.util.JlptLevel
import com.example.kanjilearning.domain.util.Role
import com.example.kanjilearning.ui.kanji.KanjiEntryUi
import com.example.kanjilearning.ui.kanji.KanjiEvent
import com.example.kanjilearning.ui.kanji.KanjiFormError
import com.example.kanjilearning.ui.kanji.KanjiFormInput
import com.example.kanjilearning.ui.kanji.KanjiSectionAdapter
import com.example.kanjilearning.ui.kanji.KanjiUiState
import com.example.kanjilearning.ui.kanji.KanjiViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class KanjiFragment : Fragment() {

    private var _binding: FragmentKanjiBinding? = null
    private val binding get() = _binding!!

    private val viewModel: KanjiViewModel by viewModels()

    private val kanjiAdapter = KanjiSectionAdapter(::onEditKanji, ::onDeleteKanji)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentKanjiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerKanji.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = kanjiAdapter
            itemAnimator = null
        }
        binding.fabAddKanji.setOnClickListener { showKanjiForm(null) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::renderState) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }
    }

    private fun renderState(state: KanjiUiState) {
        val hasContent = state.sections.any { it.entries.isNotEmpty() }
        val showList = hasContent || state.role == Role.ADMIN
        binding.progressKanji.isVisible = state.isLoading || state.isMutating
        binding.recyclerKanji.isVisible = showList
        binding.recyclerKanji.alpha = if (state.isMutating) 0.4f else 1f
        binding.textKanjiEmpty.isVisible = !state.isLoading && !hasContent
        binding.fabAddKanji.isVisible = state.role == Role.ADMIN
        binding.fabAddKanji.isEnabled = !state.isMutating
        kanjiAdapter.updateManageable(state.role == Role.ADMIN)
        kanjiAdapter.submitList(state.sections)
    }

    private fun handleEvent(event: KanjiEvent) {
        when (event) {
            is KanjiEvent.ShowMessage -> showSnack(getString(event.type.messageRes))
            is KanjiEvent.ShowValidation -> {
                val message = when (event.error) {
                    KanjiFormError.CharacterBlank -> getString(R.string.kanji_error_character_required)
                    KanjiFormError.MeaningBlank -> getString(R.string.kanji_error_meaning_required)
                }
                showSnack(message)
            }
            is KanjiEvent.ShowError -> showSnack(event.message)
        }
    }

    private fun showKanjiForm(existing: KanjiEntryUi?) {
        val context = requireContext()
        val formBinding = DialogKanjiFormBinding.inflate(layoutInflater)
        val isEditing = existing != null
        formBinding.textFormTitle.text = getString(
            if (isEditing) R.string.kanji_action_edit else R.string.kanji_action_add
        )
        formBinding.inputCharacter.setText(existing?.character.orEmpty())
        formBinding.inputMeaning.setText(existing?.meaning.orEmpty())
        formBinding.inputOnyomi.setText(existing?.onyomi.orEmpty())
        formBinding.inputKunyomi.setText(existing?.kunyomi.orEmpty())

        val jlptLabels = JlptLevel.entries.map { it.label }
        formBinding.inputJlpt.setAdapter(
            ArrayAdapter(context, android.R.layout.simple_list_item_1, jlptLabels)
        )
        formBinding.inputJlpt.setText((existing?.jlptLevel ?: JlptLevel.N5).label, false)

        val tierMap = tierLabels()
        formBinding.inputTier.setAdapter(
            ArrayAdapter(context, android.R.layout.simple_list_item_1, tierMap.values.toList())
        )
        val tierDefault = tierMap[existing?.accessTier ?: AccessTier.FREE] ?: tierMap.getValue(AccessTier.FREE)
        formBinding.inputTier.setText(tierDefault, false)

        val difficultyValue = (existing?.difficulty ?: DEFAULT_DIFFICULTY).coerceIn(1, 9)
        formBinding.sliderDifficulty.value = difficultyValue.toFloat()
        updateDifficultyLabel(formBinding, difficultyValue)
        formBinding.sliderDifficulty.addOnChangeListener { _, value, _ ->
            updateDifficultyLabel(formBinding, value.toInt())
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(formBinding.root)
            .setPositiveButton(if (isEditing) R.string.kanji_action_update else R.string.kanji_action_add, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val character = formBinding.inputCharacter.text?.toString().orEmpty()
                val meaning = formBinding.inputMeaning.text?.toString().orEmpty()
                var hasError = false
                if (character.isBlank()) {
                    formBinding.layoutCharacter.error = getString(R.string.kanji_error_character_required)
                    hasError = true
                } else {
                    formBinding.layoutCharacter.error = null
                }
                if (meaning.isBlank()) {
                    formBinding.layoutMeaning.error = getString(R.string.kanji_error_meaning_required)
                    hasError = true
                } else {
                    formBinding.layoutMeaning.error = null
                }
                if (hasError) {
                    return@setOnClickListener
                }

                val selectedTierLabel = formBinding.inputTier.text?.toString().orEmpty()
                val tier = tierMap.entries.firstOrNull { it.value == selectedTierLabel }?.key ?: AccessTier.FREE
                val input = KanjiFormInput(
                    character = character,
                    meaning = meaning,
                    onyomi = formBinding.inputOnyomi.text?.toString().orEmpty(),
                    kunyomi = formBinding.inputKunyomi.text?.toString().orEmpty(),
                    jlptLevel = JlptLevel.fromLabel(formBinding.inputJlpt.text?.toString()),
                    accessTier = tier,
                    difficulty = formBinding.sliderDifficulty.value.toInt()
                )

                if (isEditing) {
                    viewModel.updateKanji(existing!!.id, input)
                } else {
                    viewModel.createKanji(input)
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun updateDifficultyLabel(formBinding: DialogKanjiFormBinding, value: Int) {
        formBinding.textDifficultyLabel.text = getString(R.string.kanji_entry_difficulty_format, value)
    }

    private fun tierLabels(): Map<AccessTier, String> = mapOf(
        AccessTier.FREE to getString(R.string.kanji_tier_free),
        AccessTier.VIP to getString(R.string.kanji_tier_vip)
    )

    private fun onEditKanji(entry: KanjiEntryUi) {
        if (viewModel.uiState.value.role == Role.ADMIN) {
            showKanjiForm(entry)
        }
    }

    private fun onDeleteKanji(entry: KanjiEntryUi) {
        if (viewModel.uiState.value.role != Role.ADMIN) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.kanji_action_delete))
            .setMessage(getString(R.string.kanji_delete_confirm, entry.character))
            .setPositiveButton(R.string.kanji_action_delete) { _, _ ->
                viewModel.deleteKanji(entry.id)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showSnack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val DEFAULT_DIFFICULTY = 1
    }
}
