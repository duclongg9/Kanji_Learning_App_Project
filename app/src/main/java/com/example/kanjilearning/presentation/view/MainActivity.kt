package com.example.kanjilearning.presentation.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.ActivityMainBinding
import com.example.kanjilearning.presentation.viewmodel.MainToolbarViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * VI: Activity gốc chứa NavHost và đồng bộ tiêu đề với các destination.
 * EN: Host activity owning the navigation graph and top app bar.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val toolbarViewModel: MainToolbarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
        observeToolbarTitle()
    }

    private fun setupNavigation() {
        val navHost = supportFragmentManager.findFragmentById(R.id.navHostContainer) as NavHostFragment
        navController = navHost.navController
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.welcomeFragment, R.id.courseListFragment))
        binding.topAppBar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun observeToolbarTitle() {
        toolbarViewModel.title.observe(this) { title ->
            binding.topAppBar.title = title
        }
    }
}
