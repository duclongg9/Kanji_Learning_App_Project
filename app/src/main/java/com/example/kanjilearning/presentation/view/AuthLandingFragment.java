package com.example.kanjilearning.presentation.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.kanjilearning.R;
import com.example.kanjilearning.databinding.FragmentAuthLandingBinding;
import com.example.kanjilearning.presentation.viewmodel.AuthViewModel;
import com.example.kanjilearning.presentation.viewmodel.MainToolbarViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * VI: Màn hình landing độc đáo dẫn dắt người dùng tới đăng nhập hoặc đăng ký.
 * EN: Futuristic landing screen guiding users into login or registration.
 */
@AndroidEntryPoint
public class AuthLandingFragment extends Fragment {

    private FragmentAuthLandingBinding binding;
    private AuthViewModel authViewModel;
    private MainToolbarViewModel toolbarViewModel;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAuthLandingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        authViewModel = provider.get(AuthViewModel.class);
        toolbarViewModel = provider.get(MainToolbarViewModel.class);
        toolbarViewModel.updateTitle(getString(R.string.app_name));
        setupActions();
        observeSession();
    }

    private void setupActions() {
        binding.buttonLandingLogin.setOnClickListener(v ->
            navController.navigate(R.id.action_authLandingFragment_to_loginFragment));
        binding.buttonLandingRegister.setOnClickListener(v ->
            navController.navigate(R.id.action_authLandingFragment_to_registerFragment));
    }

    private void observeSession() {
        authViewModel.getStateLiveData().observe(getViewLifecycleOwner(), state -> {
            if (state.getSession() != null) {
                Toast.makeText(requireContext(),
                        getString(R.string.auth_welcome_back, state.getSession().getDisplayName()),
                        Toast.LENGTH_SHORT).show();
                navigateToDashboard();
            }
        });
    }

    private void navigateToDashboard() {
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() != R.id.courseListFragment) {
            navController.navigate(R.id.action_authLandingFragment_to_courseListFragment);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
