package com.example.kanjilearning.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.FragmentModeSelectionBinding

class ModeSelectionFragment : Fragment() {

    private var _binding: FragmentModeSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModeSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toProfile = View.OnClickListener {
            findNavController().navigate(R.id.action_modeSelectionFragment_to_profileFragment)
        }
        binding.cardKanji.setOnClickListener(toProfile)
        binding.cardPractice.setOnClickListener(toProfile)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
