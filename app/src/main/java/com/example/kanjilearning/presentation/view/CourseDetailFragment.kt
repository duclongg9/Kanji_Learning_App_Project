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
import com.example.kanjilearning.databinding.FragmentCourseDetailBinding
import com.example.kanjilearning.domain.model.LessonSummary
import com.example.kanjilearning.presentation.viewmodel.CourseDetailUiState
import com.example.kanjilearning.presentation.viewmodel.CourseDetailViewModel
import com.example.kanjilearning.presentation.viewmodel.MainToolbarViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * VI: Màn chi tiết khoá học hiển thị lộ trình và cho phép mở khóa qua MoMo.
 * EN: Course detail fragment listing lessons and enabling simulated MoMo unlock.
 */
@AndroidEntryPoint
class CourseDetailFragment : Fragment() {

    private var _binding: FragmentCourseDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CourseDetailViewModel by viewModels()
    private val toolbarViewModel: MainToolbarViewModel by activityViewModels()
    private lateinit var lessonAdapter: LessonAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButton()
        collectState()
    }

    private fun setupRecyclerView() {
        lessonAdapter = LessonAdapter { lesson ->
            navigateToLesson(lesson)
        }
        binding.recyclerLessons.adapter = lessonAdapter
    }

    private fun setupButton() {
        binding.buttonAction.setOnClickListener {
            val courseDetail = viewModel.state.value.course ?: return@setOnClickListener
            if (courseDetail.course.isUnlocked || !courseDetail.course.isPremium) {
                val firstLesson = courseDetail.lessons.minByOrNull { it.orderIndex }
                if (firstLesson != null) {
                    navigateToLesson(firstLesson)
                } else {
                    Snackbar.make(binding.root, R.string.course_detail_no_lessons, Snackbar.LENGTH_SHORT).show()
                }
            } else {
                showMomoDialog()
            }
        }
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: CourseDetailUiState) {
        binding.buttonAction.isEnabled = !state.isUnlocking
        binding.buttonAction.text = if (state.isUnlocking) {
            getString(R.string.course_detail_unlocking)
        } else {
            val course = state.course?.course
            when {
                course == null -> getString(R.string.course_detail_start_cta)
                !course.isUnlocked && course.isPremium -> getString(R.string.course_detail_unlock_cta)
                else -> getString(R.string.course_detail_start_cta)
            }
        }
        state.course?.let { detail ->
            toolbarViewModel.updateTitle(detail.course.title)
            binding.chipLevel.text = detail.course.levelTag
            binding.textTitle.text = detail.course.title
            binding.textDescription.text = detail.course.description
            binding.textMeta.text = getString(
                R.string.course_detail_meta,
                detail.lessons.size,
                detail.course.durationMinutes
            )
            binding.progress.progress = detail.course.progressPercent
            binding.textProgress.text = getString(
                R.string.course_detail_progress,
                detail.course.completedLessons,
                detail.course.lessonCount,
                detail.course.progressPercent
            )
            val showPrice = detail.course.isPremium && !detail.course.isUnlocked
            binding.textPrice.visibility = if (showPrice) View.VISIBLE else View.GONE
            binding.textLockedMessage.visibility = if (showPrice) View.VISIBLE else View.GONE
            if (showPrice) {
                binding.textPrice.text = getString(R.string.courses_price_format, detail.course.priceVnd)
            }
            lessonAdapter.submitList(detail.lessons)
        }
        state.errorMessage?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            viewModel.clearError()
        }
        state.lastReceipt?.let { receipt ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.course_detail_unlock_success_title)
                .setMessage(getString(R.string.course_detail_unlock_success_body, receipt.reference))
                .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                .show()
            viewModel.consumeReceipt()
        }
    }

    private fun showMomoDialog() {
        val inputView = layoutInflater.inflate(R.layout.dialog_momo_input, null)
        val editText = inputView.findViewById<TextInputEditText>(R.id.inputMomo)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.course_detail_momo_title)
            .setMessage(R.string.course_detail_momo_message)
            .setView(inputView)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.course_detail_unlock_cta) { dialog, _ ->
                val phone = editText.text?.toString().orEmpty()
                if (phone.length < 8) {
                    Snackbar.make(binding.root, R.string.course_detail_momo_invalid, Snackbar.LENGTH_SHORT).show()
                } else {
                    viewModel.unlockCourse(phone)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToLesson(lesson: LessonSummary) {
        findNavController().navigate(
            R.id.action_courseDetailFragment_to_lessonDetailFragment,
            bundleOf("lessonId" to lesson.id)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
