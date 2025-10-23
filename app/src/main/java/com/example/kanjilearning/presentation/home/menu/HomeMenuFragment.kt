package com.example.kanjilearning.presentation.home.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        binding.cardQuiz.setOnClickListener {
            findNavController().navigate(R.id.action_homeMenuFragment_to_kanjiQuizFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
