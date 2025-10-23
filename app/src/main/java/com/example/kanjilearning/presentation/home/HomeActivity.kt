package com.example.kanjilearning.presentation.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.ActivityHomeBinding
import com.example.kanjilearning.work.SrsReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * VI: Activity chính cho user FREE/VIP.
 */
@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    @Inject
    lateinit var srsReminderScheduler: SrsReminderScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupNavigation()
        srsReminderScheduler.scheduleDailyReminder()
    }

    private fun setupNavigation() {
        val navHost = supportFragmentManager.findFragmentById(R.id.home_nav_host) as NavHostFragment
        setupActionBarWithNavController(navHost.navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHost = supportFragmentManager.findFragmentById(R.id.home_nav_host) as NavHostFragment
        return navHost.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
