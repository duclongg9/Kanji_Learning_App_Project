package com.example.kanjilearning.presentation.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kanjilearning.databinding.ActivityMainBinding
import com.example.kanjilearning.presentation.viewmodel.KanjiViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * VI: View (Activity) chỉ lo hiển thị và quan sát LiveData từ ViewModel.
 * EN: The Activity wires the RecyclerView and observes the ViewModel.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: KanjiViewModel by viewModels()
    private val adapter = KanjiListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeKanjis()
    }

    private fun setupRecyclerView() {
        binding.kanjiRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.kanjiRecyclerView.adapter = adapter
    }

    private fun observeKanjis() {
        viewModel.allKanjis.observe(this) { kanjis ->
            adapter.submitList(kanjis)
        }
    }
}
