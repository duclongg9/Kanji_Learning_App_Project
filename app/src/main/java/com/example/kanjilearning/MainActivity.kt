package com.example.kanjilearning

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kanjilearning.databinding.ActivityMainBinding

/**
 * VI: Activity kiểm tra khởi động app.
 * EN: Simple bootstrap screen used to verify the project builds and runs on device/emulator.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textBootstrapMessage.text = getString(R.string.main_bootstrap_message)
    }
}
