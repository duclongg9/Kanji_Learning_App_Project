package com.example.kanjilearning.presentation.admin.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.FragmentAdminAccountsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * VI: Fragment quản lý tài khoản (nâng VIP).
 */
@AndroidEntryPoint
class AdminAccountsFragment : Fragment() {

    private var _binding: FragmentAdminAccountsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminAccountsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        binding.buttonUpgrade.setOnClickListener { viewModel.upgradeToVip() }
        binding.buttonToImport.setOnClickListener {
            findNavController().navigate(R.id.adminImportKanjiFragment)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.textCurrentRole.text = "Role hiện tại: ${state.currentRole.name}"
                    binding.progress.visibility = if (state.isProcessing) View.VISIBLE else View.GONE
                    binding.buttonUpgrade.isEnabled = !state.isProcessing
                    binding.textMessage.text = state.message.orEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
