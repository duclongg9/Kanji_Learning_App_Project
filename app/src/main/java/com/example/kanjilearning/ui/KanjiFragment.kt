package com.example.kanjilearning.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kanjilearning.databinding.FragmentKanjiBinding
import com.example.kanjilearning.ui.kanji.KanjiSectionAdapter
import com.example.kanjilearning.ui.kanji.KanjiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class KanjiFragment : Fragment() {

    private var _binding: FragmentKanjiBinding? = null
    private val binding get() = _binding!!

    private val viewModel: KanjiViewModel by viewModels()

    private val kanjiAdapter = KanjiSectionAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressKanji.isVisible = state.isLoading
                    val hasContent = state.sections.any { it.entries.isNotEmpty() }
                    binding.recyclerKanji.isVisible = hasContent
                    binding.textKanjiEmpty.isVisible = !state.isLoading && !hasContent
                    kanjiAdapter.submitList(state.sections)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
