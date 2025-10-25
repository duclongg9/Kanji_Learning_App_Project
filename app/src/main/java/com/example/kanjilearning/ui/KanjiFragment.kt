package com.example.kanjilearning.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.kanjilearning.databinding.FragmentKanjiBinding

class KanjiFragment : Fragment() {

    private var _binding: FragmentKanjiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKanjiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sections = listOf(
            KanjiSection(binding.headerN5, binding.contentN5, binding.iconN5),
            KanjiSection(binding.headerN4, binding.contentN4, binding.iconN4),
            KanjiSection(binding.headerN3, binding.contentN3, binding.iconN3),
            KanjiSection(binding.headerN2, binding.contentN2, binding.iconN2),
            KanjiSection(binding.headerN1, binding.contentN1, binding.iconN1),
        )

        sections.forEach { section ->
            section.header.setOnClickListener { toggleSection(section) }
        }
    }

    private fun toggleSection(section: KanjiSection) {
        val shouldExpand = !section.content.isVisible
        section.content.visibility = if (shouldExpand) View.VISIBLE else View.GONE
        val rotation = if (shouldExpand) 180f else 0f
        section.icon.animate().rotation(rotation).setDuration(200).start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class KanjiSection(
        val header: View,
        val content: View,
        val icon: ImageView,
    )
}
