package com.example.kanjilearning.presentation.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kanjilearning.databinding.ActivitySplashBinding
import com.example.kanjilearning.presentation.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * VI: SplashActivity hiển thị logo + nền Nhật Bản trong 1.5s rồi chuyển sang màn đăng nhập.
 */
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // VI: Delay 1.5s để user kịp thưởng thức splash.
        lifecycleScope.launch {
            delay(1500)
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }
    }
}
