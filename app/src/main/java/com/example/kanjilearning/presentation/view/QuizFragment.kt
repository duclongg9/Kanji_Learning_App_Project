package com.example.kanjilearning.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.FragmentQuizBinding
import com.example.kanjilearning.presentation.viewmodel.MainToolbarViewModel
import com.example.kanjilearning.presentation.viewmodel.QuizUiState
import com.example.kanjilearning.presentation.viewmodel.QuizViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * VI: Màn luyện thi trắc nghiệm dạng Quizlet.
 * EN: Quiz fragment with reveal/next controls.
 */
@AndroidEntryPoint
class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by viewModels()
    private val toolbarViewModel: MainToolbarViewModel by activityViewModels()
    private val adapter = QuizOptionsAdapter { choice -> viewModel.selectChoice(choice.id) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerOptions.adapter = adapter
        binding.buttonPrimary.setOnClickListener { onPrimaryClicked() }
        binding.buttonRetry.setOnClickListener { viewModel.loadQuiz() }
        binding.buttonClose.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        collectState()
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> renderState(state) }
            }
        }
    }

    private fun onPrimaryClicked() {
        val state = viewModel.state.value
        when {
            state.isFinished -> {
                binding.resultContainer.visibility = View.VISIBLE
            }
            state.isAnswerRevealed -> viewModel.goToNext()
            else -> viewModel.submitAnswer()
        }
    }

    private fun renderState(state: QuizUiState) {
        binding.loadingIndicator.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        if (state.isLoading) {
            binding.cardQuestion.visibility = View.INVISIBLE
            binding.recyclerOptions.visibility = View.INVISIBLE
            binding.buttonPrimary.isEnabled = false
            return
        } else {
            binding.cardQuestion.visibility = if (state.isFinished) View.GONE else View.VISIBLE
            binding.recyclerOptions.visibility = if (state.isFinished) View.GONE else View.VISIBLE
        }
        val session = state.session
        if (session != null) {
            toolbarViewModel.updateTitle(session.lessonTitle)
            val total = session.questions.size
            if (total == 0) {
                binding.textQuestionProgress.text = getString(R.string.quiz_progress_label, 0, 0)
                binding.progressQuiz.progress = 0
                binding.cardQuestion.visibility = View.GONE
                binding.recyclerOptions.visibility = View.GONE
                binding.textExplanation.visibility = View.GONE
                binding.buttonPrimary.visibility = View.GONE
                binding.resultContainer.visibility = View.VISIBLE
                binding.textResultTitle.text = getString(R.string.quiz_result_keep_trying)
                binding.textResultMessage.text = getString(R.string.quiz_result_summary, 0, 0)
                return
            }
            val currentIndex = state.currentIndex.coerceAtMost(total - 1)
            val question = session.questions.getOrNull(currentIndex)
            binding.textQuestionProgress.text = getString(
                R.string.quiz_progress_label,
                currentIndex + 1,
                total
            )
            val progressValue = if (total == 0) 0 else ((currentIndex.toDouble() / total) * 100).toInt()
            binding.progressQuiz.progress = progressValue
            if (question != null) {
                binding.textQuestion.text = question.prompt
                adapter.submitList(question.choices.map { it.copy() })
                adapter.selectedChoiceId = state.selectedChoiceId
                adapter.revealed = state.isAnswerRevealed
                adapter.correctChoiceId = question.choices.firstOrNull { it.isCorrect }?.id
                adapter.notifyDataSetChanged()
                if (state.isAnswerRevealed && question.explanation.isNotBlank()) {
                    binding.textExplanation.visibility = View.VISIBLE
                    binding.textExplanation.text = getString(R.string.quiz_explanation_format, question.explanation)
                } else {
                    binding.textExplanation.visibility = View.GONE
                }
                binding.buttonPrimary.text = when {
                    state.isAnswerRevealed && currentIndex == total - 1 -> getString(R.string.quiz_finish_cta)
                    state.isAnswerRevealed -> getString(R.string.quiz_next_cta)
                    else -> getString(R.string.quiz_check_cta)
                }
            }
            if (state.isFinished) {
                val score = state.score
                binding.resultContainer.visibility = View.VISIBLE
                binding.textResultTitle.text = if (score * 100 / total >= 80) {
                    getString(R.string.quiz_result_great)
                } else {
                    getString(R.string.quiz_result_keep_trying)
                }
                binding.textResultMessage.text = getString(R.string.quiz_result_summary, score, total)
                binding.buttonPrimary.visibility = View.GONE
            } else {
                binding.resultContainer.visibility = View.GONE
                binding.buttonPrimary.visibility = View.VISIBLE
                binding.buttonPrimary.isEnabled = if (state.isAnswerRevealed) {
                    true
                } else {
                    state.selectedChoiceId != null
                }
            }
        }
        state.errorMessage?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
