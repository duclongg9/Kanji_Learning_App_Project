package com.example.kanjilearning.presentation.home.kanji

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kanjilearning.databinding.FragmentKanjiTheoryBinding
import com.example.kanjilearning.domain.util.JlptLevel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * VI: Fragment hiển thị danh sách Kanji lý thuyết theo role và bộ lọc.
 */
@AndroidEntryPoint
class KanjiTheoryFragment : Fragment() {

    private var _binding: FragmentKanjiTheoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: KanjiTheoryViewModel by viewModels()
    private val adapter = KanjiAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKanjiTheoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSpinner()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.recyclerKanji.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerKanji.adapter = adapter
    }

    private fun setupSpinner() {
        val items = listOf("Tất cả") + JlptLevel.entries.map { it.label }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJlpt.adapter = spinnerAdapter
        binding.spinnerJlpt.setSelection(0)

        binding.spinnerJlpt.setOnItemSelectedListener { position ->
            val selected = if (position == 0) null else JlptLevel.entries[position - 1]
            viewModel.updateJlpt(selected)
        }

        binding.seekDifficulty.addOnChangeListener { _, value, _ ->
            viewModel.updateDifficulty(value.toInt())
            binding.textDifficulty.text = "Độ khó tối đa: ${value.toInt()}"
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.kanji)
                    binding.textRole.text = "Quyền hạn: ${state.role.name}"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * VI: Extension giúp rút gọn setOnItemSelectedListener cho Spinner bằng lambda.
 */
private fun androidx.appcompat.widget.AppCompatSpinner.setOnItemSelectedListener(onSelected: (Int) -> Unit) {
    onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: android.widget.AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            onSelected(position)
        }

        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
            // VI: Không làm gì
        }
    }
}
