package com.example.kanjilearning.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.FragmentLessonDetailBinding
import com.example.kanjilearning.presentation.viewmodel.LessonDetailViewModel
import com.example.kanjilearning.presentation.viewmodel.MainToolbarViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * VI: Màn hiển thị thông tin chi tiết của một lesson và danh sách Kanji.
 * EN: Lesson detail fragment listing kanji and providing a quiz entry point.
 */
@AndroidEntryPoint
class LessonDetailFragment : Fragment() {

    private var _binding: FragmentLessonDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LessonDetailViewModel by viewModels()
    private val toolbarViewModel: MainToolbarViewModel by activityViewModels()
    private val adapter = KanjiDetailAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerKanjis.adapter = adapter
        binding.buttonStartQuiz.setOnClickListener {
            val lesson = viewModel.state.value.lesson ?: return@setOnClickListener
            findNavController().navigate(
                R.id.action_lessonDetailFragment_to_quizFragment,
                bundleOf("lessonId" to lesson.summary.id)
            )
        }
        collectState()
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    val lesson = state.lesson
                    if (lesson != null) {
                        toolbarViewModel.updateTitle(lesson.summary.title)
                        binding.textTitle.text = lesson.summary.title
                        binding.textSummary.text = lesson.summary.summary
                        binding.textMeta.text = getString(
                            R.string.lesson_meta,
                            lesson.summary.questionCount,
                            lesson.summary.durationMinutes
                        )
                        binding.textProgress.text = getString(
                            R.string.lesson_progress_label,
                            lesson.summary.bestScore,
                            lesson.summary.questionCount
                        )
                        adapter.submitList(lesson.kanjis)
                    }
                    binding.buttonStartQuiz.isEnabled = !state.isLoading
                    state.errorMessage?.let { message ->
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
