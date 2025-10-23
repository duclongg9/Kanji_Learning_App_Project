package com.example.kanjilearning.presentation.home.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.FragmentHomeMenuBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * VI: Fragment hiển thị hai ô Kanji lý thuyết và Luyện trắc nghiệm.
 */
@AndroidEntryPoint
class HomeMenuFragment : Fragment() {

    private var _binding: FragmentHomeMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cardKanjiTheory.setOnClickListener {
            findNavController().navigate(R.id.action_homeMenuFragment_to_kanjiTheoryFragment)
        }
        binding.practiceLevelDropdown.keyListener = null
        binding.practiceLevelDropdown.setText("", false)
        binding.practiceLevelDropdown.setOnClickListener {
            binding.practiceLevelDropdown.showDropDown()
        }
        binding.practiceLevelDropdown.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.practiceLevelDropdown.showDropDown()
            }
        }
        binding.practiceLevelDropdown.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent?.getItemAtPosition(position)?.toString().orEmpty()
            if (selected.isNotBlank()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.home_quiz_selection_message, selected),
                    Toast.LENGTH_SHORT
                ).show()
            }
            binding.practiceLevelDropdown.setText("", false)
        }
        binding.cardQuiz.setOnClickListener {
            binding.practiceLevelDropdown.showDropDown()
            Toast.makeText(requireContext(), R.string.home_quiz_placeholder, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
