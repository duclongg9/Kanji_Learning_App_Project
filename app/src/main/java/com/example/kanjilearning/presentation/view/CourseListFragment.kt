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
import com.example.kanjilearning.databinding.FragmentCourseListBinding
import com.example.kanjilearning.presentation.viewmodel.AuthViewModel
import com.example.kanjilearning.presentation.viewmodel.CourseListUiState
import com.example.kanjilearning.presentation.viewmodel.CourseListViewModel
import com.example.kanjilearning.presentation.viewmodel.MainToolbarViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * VI: Danh sách khoá học với progress tổng quan và các nút truy cập nhanh.
 * EN: Course dashboard fragment showing progress and entry points.
 */
@AndroidEntryPoint
class CourseListFragment : Fragment() {

    private var _binding: FragmentCourseListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CourseListViewModel by viewModels()
    private val toolbarViewModel: MainToolbarViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var adapter: CourseAdapter
    private var currentRoleName: String? = null
    private var latestState: CourseListUiState? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        collectState()
        observeAuthState()
    }

    override fun onResume() {
        super.onResume()
        toolbarViewModel.updateTitle(getString(R.string.courses_screen_title))
    }

    private fun setupRecyclerView() {
        adapter = CourseAdapter(
            onCourseClick = { course ->
                findNavController().navigate(
                    R.id.action_courseListFragment_to_courseDetailFragment,
                    bundleOf("courseId" to course.id)
                )
            },
            onUnlockClick = { course ->
                findNavController().navigate(
                    R.id.action_courseListFragment_to_courseDetailFragment,
                    bundleOf("courseId" to course.id)
                )
            }
        )
        binding.recyclerCourses.adapter = adapter
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

    private fun renderState(state: CourseListUiState) {
        latestState = state
        binding.loadingIndicator.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.textEmpty.visibility = if (!state.isLoading && state.courses.isEmpty()) View.VISIBLE else View.GONE
        adapter.submitList(state.courses)
        binding.progressOverview.progress = state.overallPercent
        binding.textProgress.text = getString(
            R.string.courses_overview_progress_dynamic,
            state.completedLessons,
            state.totalLessons,
            state.overallPercent
        )
        val overviewBase = if (state.totalLessons == 0) {
            getString(R.string.courses_overview_subtitle)
        } else {
            getString(R.string.courses_overview_subtitle_dynamic, state.courses.size)
        }
        val roleSuffix = currentRoleName?.let { getString(R.string.courses_role_context, it) }
        binding.textOverview.text = listOfNotNull(overviewBase, roleSuffix).joinToString(" · ")
    }

    private fun observeAuthState() {
        authViewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            val name = state.session?.displayName ?: getString(R.string.courses_overview_title)
            binding.textWelcome.text = getString(R.string.courses_overview_greeting, name)
            currentRoleName = state.session?.role?.displayName
            latestState?.let { renderState(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
