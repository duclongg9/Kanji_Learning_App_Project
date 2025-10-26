package com.example.kanjilearning.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.FragmentProfileBinding
import com.example.kanjilearning.domain.util.Role
import com.example.kanjilearning.ui.profile.ProfileUiState
import com.example.kanjilearning.ui.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.buttonNext.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_kanjiFragment)
        }
        binding.drawerLayout.openDrawer(GravityCompat.START)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> renderState(state) }
            }
        }
    }

    private fun renderState(state: ProfileUiState) {
        val user = state.user
        if (user == null) {
            binding.textName.text = getString(R.string.profile_name)
            binding.textLevel.text = getString(R.string.profile_role_free)
            binding.textEmail.text = getString(R.string.profile_email_placeholder)
            binding.textSync.text = getString(R.string.profile_sync_placeholder)
            return
        }
        binding.textName.text = if (user.displayName.isNotBlank()) user.displayName else getString(R.string.profile_name)
        binding.textLevel.text = getString(roleLabelFor(user.role))
        binding.textEmail.text = if (user.email.isNotBlank()) {
            getString(R.string.profile_email_format, user.email)
        } else {
            getString(R.string.profile_email_placeholder)
        }
        binding.textSync.text = if (user.lastSyncedAt > 0) {
            val formatted = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date(user.lastSyncedAt))
            getString(R.string.profile_last_sync_format, formatted)
        } else {
            getString(R.string.profile_sync_never)
        }
    }

    private fun roleLabelFor(role: Role): Int = when (role) {
        Role.ADMIN -> R.string.profile_role_admin
        Role.VIP -> R.string.profile_role_vip
        Role.FREE -> R.string.profile_role_free
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
