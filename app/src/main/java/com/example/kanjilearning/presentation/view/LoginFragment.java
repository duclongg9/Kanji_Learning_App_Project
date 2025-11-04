package com.example.kanjilearning.presentation.view;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.kanjilearning.databinding.FragmentLoginBinding;
import com.example.kanjilearning.presentation.viewmodel.AuthViewModel;
import com.example.kanjilearning.presentation.viewmodel.AuthViewState;
import com.example.kanjilearning.presentation.viewmodel.MainToolbarViewModel;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * VI: Màn hình đăng nhập phong cách holo xử lý xác thực từ DB MySQL.
 * EN: Neon-infused login screen authenticating against the MySQL backend.
 */
@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;
    private MainToolbarViewModel toolbarViewModel;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        authViewModel = provider.get(AuthViewModel.class);
        toolbarViewModel = provider.get(MainToolbarViewModel.class);
        toolbarViewModel.updateTitle(getString(R.string.login_title));
        setupUi();
        observeState();
    }

    private void setupUi() {
        binding.buttonLogin.setOnClickListener(v -> {
            String email = binding.inputEmail.getText() != null ? binding.inputEmail.getText().toString().trim() : "";
            String password = binding.inputPassword.getText() != null ? binding.inputPassword.getText().toString() : "";
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Snackbar.make(binding.getRoot(), R.string.auth_missing_fields, Snackbar.LENGTH_SHORT).show();
                return;
            }
            authViewModel.login(email, password);
        });
        binding.textRegister.setOnClickListener(v ->
            navController.navigate(R.id.action_loginFragment_to_registerFragment));
    }

    private void observeState() {
        authViewModel.getStateLiveData().observe(getViewLifecycleOwner(), this::renderState);
    }

    private void renderState(AuthViewState state) {
        binding.progressBar.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
        binding.buttonLogin.setEnabled(!state.isLoading());
        if (state.getErrorMessage() != null) {
            Snackbar.make(binding.getRoot(), state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
            authViewModel.clearError();
        }
        if (state.getSession() != null) {
            navigateToDashboard();
        }
    }

    private void navigateToDashboard() {
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() != R.id.courseListFragment) {
            navController.navigate(R.id.action_loginFragment_to_courseListFragment);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
