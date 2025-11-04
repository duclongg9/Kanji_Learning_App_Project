package com.example.kanjilearning.presentation.view;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.kanjilearning.R;
import com.example.kanjilearning.databinding.FragmentRegisterBinding;
import com.example.kanjilearning.domain.model.UserRole;
import com.example.kanjilearning.presentation.viewmodel.AuthViewModel;
import com.example.kanjilearning.presentation.viewmodel.AuthViewState;
import com.example.kanjilearning.presentation.viewmodel.MainToolbarViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * VI: Màn hình đăng ký với lựa chọn role và hiệu ứng kính.
 * EN: Registration screen with holographic role selection chips.
 */
@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel authViewModel;
    private MainToolbarViewModel toolbarViewModel;
    private NavController navController;
    private String lastSelectedRoleCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        authViewModel = provider.get(AuthViewModel.class);
        toolbarViewModel = provider.get(MainToolbarViewModel.class);
        toolbarViewModel.updateTitle(getString(R.string.register_title));
        setupUi();
        observeState();
        authViewModel.refreshRoles();
    }

    private void setupUi() {
        binding.buttonRegister.setOnClickListener(v -> {
            String displayName = binding.inputDisplayName.getText() != null ? binding.inputDisplayName.getText().toString().trim() : "";
            String email = binding.inputRegisterEmail.getText() != null ? binding.inputRegisterEmail.getText().toString().trim() : "";
            String password = binding.inputRegisterPassword.getText() != null ? binding.inputRegisterPassword.getText().toString() : "";
            Chip checkedChip = binding.chipGroupRoles.findViewById(binding.chipGroupRoles.getCheckedChipId());
            if (TextUtils.isEmpty(displayName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || checkedChip == null) {
                Snackbar.make(binding.getRoot(), R.string.auth_missing_fields, Snackbar.LENGTH_SHORT).show();
                return;
            }
            String roleCode = checkedChip.getTag() != null ? checkedChip.getTag().toString() : "";
            lastSelectedRoleCode = roleCode;
            authViewModel.register(email, password, displayName, roleCode);
        });
        binding.textGoToLogin.setOnClickListener(v ->
            navController.navigate(R.id.action_registerFragment_to_loginFragment));
        binding.chipGroupRoles.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                View chip = group.findViewById(checkedIds.get(0));
                if (chip != null && chip.getTag() != null) {
                    lastSelectedRoleCode = chip.getTag().toString();
                }
            }
        });
    }

    private void observeState() {
        authViewModel.getStateLiveData().observe(getViewLifecycleOwner(), this::renderState);
    }

    private void renderState(AuthViewState state) {
        binding.registerProgress.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
        binding.buttonRegister.setEnabled(!state.isLoading());
        renderRoles(requireContext(), state.getAvailableRoles());
        if (state.getErrorMessage() != null) {
            Snackbar.make(binding.getRoot(), state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
            authViewModel.clearError();
        }
        if (state.getSession() != null) {
            navigateToDashboard();
        }
    }

    private void renderRoles(@NonNull Context context, @NonNull List<UserRole> roles) {
        binding.chipGroupRoles.removeAllViews();
        if (roles.isEmpty()) {
            return;
        }
        for (UserRole role : roles) {
            Chip chip = new Chip(new ContextThemeWrapper(context, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated));
            chip.setId(View.generateViewId());
            chip.setText(role.getDisplayName());
            chip.setChipBackgroundColorResource(R.color.glass_surface);
            chip.setTextColor(context.getColor(android.R.color.white));
            chip.setCheckable(true);
            chip.setTag(role.getCode());
            binding.chipGroupRoles.addView(chip);
            if (role.getCode().equals(lastSelectedRoleCode)) {
                binding.chipGroupRoles.check(chip.getId());
            }
        }
        if (binding.chipGroupRoles.getCheckedChipId() == View.NO_ID && binding.chipGroupRoles.getChildCount() > 0) {
            int firstId = binding.chipGroupRoles.getChildAt(0).getId();
            binding.chipGroupRoles.check(firstId);
            View firstChip = binding.chipGroupRoles.findViewById(firstId);
            if (firstChip != null && firstChip.getTag() != null) {
                lastSelectedRoleCode = firstChip.getTag().toString();
            }
        }
    }

    private void navigateToDashboard() {
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() != R.id.courseListFragment) {
            navController.navigate(R.id.action_registerFragment_to_courseListFragment);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
