package com.example.kanjilearning.presentation.home.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kanjilearning.databinding.FragmentKanjiQuizBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * VI: Fragment luyện trắc nghiệm dạng flashcard đơn giản.
 */
@AndroidEntryPoint
class KanjiQuizFragment : Fragment() {

    private var _binding: FragmentKanjiQuizBinding? = null
    private val binding get() = _binding!!
    private val viewModel: KanjiQuizViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKanjiQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.textRole.text = "Quyền hạn: ${state.role.name}"
                    val index = viewModel.currentCardIndex.value
                    val current = state.cards.getOrNull(index)
                    if (current != null) {
                        binding.textCharacter.text = current.character
                        binding.textOn.text = "Onyomi: ${current.onyomi}"
                        binding.textKun.text = "Kunyomi: ${current.kunyomi}"
                        binding.textMeaning.text = "Ý nghĩa: ${current.meaning}"
                        binding.textMeta.text = "Thẻ ${index + 1}/${state.cards.size}"
                    } else {
                        binding.textCharacter.text = "Chưa có dữ liệu"
                        binding.textOn.text = ""
                        binding.textKun.text = ""
                        binding.textMeaning.text = ""
                        binding.textMeta.text = "Thẻ 0/0"
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentCardIndex.collect { index ->
                    val total = viewModel.uiState.value.cards.size
                    if (total > 0) {
                        binding.textMeta.text = "Thẻ ${index + 1}/$total"
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonNext.setOnClickListener {
            viewModel.moveNext(viewModel.uiState.value.cards.size)
        }
        binding.buttonPrevious.setOnClickListener {
            viewModel.movePrevious(viewModel.uiState.value.cards.size)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
