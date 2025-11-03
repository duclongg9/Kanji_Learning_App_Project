package com.example.kanjilearning.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.FragmentWelcomeBinding
import com.example.kanjilearning.presentation.viewmodel.MainToolbarViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * VI: Màn hình chào hỏi mô tả hành trình học và kêu gọi bắt đầu.
 * EN: Welcome fragment introducing the learning journey.
 */
@AndroidEntryPoint
class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private val toolbarViewModel: MainToolbarViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonStart.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_courseListFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        toolbarViewModel.updateTitle(getString(R.string.app_name))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
