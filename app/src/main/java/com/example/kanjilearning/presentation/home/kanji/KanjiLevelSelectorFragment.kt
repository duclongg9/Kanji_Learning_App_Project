package com.example.kanjilearning.presentation.home.kanji

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.FragmentKanjiLevelSelectorBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class KanjiLevelSelectorFragment : Fragment() {

    private var _binding: FragmentKanjiLevelSelectorBinding? = null
    private val binding get() = _binding!!

    private val adapter = KanjiLevelAdapter { level, sublevel ->
        if (sublevel.isAccessible) {
            findNavController().navigate(R.id.action_kanjiLevelSelectorFragment_to_kanjiTheoryFragment)
        } else {
            val message = if (level.name == "N5") {
                getString(R.string.kanji_level_selector_locked)
            } else {
                getString(R.string.kanji_level_selector_unavailable)
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var levels: List<KanjiLevel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKanjiLevelSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        levels = buildLevels()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.levelsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.levelsRecyclerView.adapter = adapter
        adapter.submitList(levels)
    }

    private fun buildLevels(): List<KanjiLevel> {
        val basic = getString(R.string.kanji_level_basic)
        val intermediate = getString(R.string.kanji_level_intermediate)
        val advanced = getString(R.string.kanji_level_advanced)
        return listOf(
            KanjiLevel(
                name = "N5",
                isFree = true,
                sublevels = listOf(
                    KanjiSublevel(name = basic, isAccessible = true),
                    KanjiSublevel(name = intermediate, isAccessible = false),
                    KanjiSublevel(name = advanced, isAccessible = false)
                )
            ),
            KanjiLevel(
                name = "N4",
                isFree = false,
                sublevels = listOf(
                    KanjiSublevel(name = basic, isAccessible = false),
                    KanjiSublevel(name = intermediate, isAccessible = false),
                    KanjiSublevel(name = advanced, isAccessible = false)
                )
            ),
            KanjiLevel(
                name = "N3",
                isFree = false,
                sublevels = listOf(
                    KanjiSublevel(name = basic, isAccessible = false),
                    KanjiSublevel(name = intermediate, isAccessible = false),
                    KanjiSublevel(name = advanced, isAccessible = false)
                )
            ),
            KanjiLevel(
                name = "N2",
                isFree = false,
                sublevels = listOf(
                    KanjiSublevel(name = basic, isAccessible = false),
                    KanjiSublevel(name = intermediate, isAccessible = false),
                    KanjiSublevel(name = advanced, isAccessible = false)
                )
            ),
            KanjiLevel(
                name = "N1",
                isFree = false,
                sublevels = listOf(
                    KanjiSublevel(name = basic, isAccessible = false),
                    KanjiSublevel(name = intermediate, isAccessible = false),
                    KanjiSublevel(name = advanced, isAccessible = false)
                )
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
