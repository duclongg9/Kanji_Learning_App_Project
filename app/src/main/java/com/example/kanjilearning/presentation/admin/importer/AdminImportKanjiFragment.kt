package com.example.kanjilearning.presentation.admin.importer

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kanjilearning.databinding.FragmentAdminImportKanjiBinding
import com.example.kanjilearning.domain.util.AccessTier
import com.example.kanjilearning.domain.util.JlptLevel
import dagger.hilt.android.AndroidEntryPoint
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.launch

/**
 * VI: Fragment cho phép admin import CSV Kanji.
 */
@AndroidEntryPoint
class AdminImportKanjiFragment : Fragment() {

    private var _binding: FragmentAdminImportKanjiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminImportKanjiViewModel by viewModels()

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            readCsvAndImport(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminImportKanjiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        setupObservers()
        binding.buttonPickFile.setOnClickListener {
            pickFileLauncher.launch("text/*")
        }
    }

    private fun setupSpinners() {
        val jlptAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, JlptLevel.entries.map { it.label })
        jlptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJlpt.adapter = jlptAdapter

        val accessAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, AccessTier.entries.map { it.name })
        accessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAccess.adapter = accessAdapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progress.visibility = if (state.isProcessing) View.VISIBLE else View.GONE
                    binding.textStatus.text = state.message.orEmpty()
                }
            }
        }
    }

    private fun readCsvAndImport(uri: Uri) {
        val jlpt = JlptLevel.entries[binding.spinnerJlpt.selectedItemPosition]
        val accessTier = AccessTier.entries[binding.spinnerAccess.selectedItemPosition]
        val contentResolver = requireContext().contentResolver
        val content = contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).readText()
        }
        if (content.isNullOrBlank()) {
            binding.textStatus.text = "Không đọc được nội dung file"
            return
        }
        viewModel.importFromCsv(content, jlpt, accessTier)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
