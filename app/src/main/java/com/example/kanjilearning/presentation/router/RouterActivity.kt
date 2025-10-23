package com.example.kanjilearning.presentation.router

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kanjilearning.domain.util.Role
import com.example.kanjilearning.presentation.admin.AdminDashboardActivity
import com.example.kanjilearning.presentation.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * VI: Activity trung gian đọc role và điều hướng tới màn phù hợp.
 */
@AndroidEntryPoint
class RouterActivity : AppCompatActivity() {

    private val viewModel: RouterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeDestination()
        viewModel.resolveDestination()
    }

    private fun observeDestination() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.destination.collect { role ->
                    when (role) {
                        Role.ADMIN -> navigateToAdmin()
                        Role.FREE, Role.VIP -> navigateToHome()
                        null -> Unit
                    }
                }
            }
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun navigateToAdmin() {
        startActivity(Intent(this, AdminDashboardActivity::class.java))
        finish()
    }
}
