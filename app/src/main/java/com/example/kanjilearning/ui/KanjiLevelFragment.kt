package com.example.kanjilearning.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.FragmentKanjiLevelBinding

class KanjiLevelFragment : Fragment() {

    private var _binding: FragmentKanjiLevelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKanjiLevelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val levelTitle = arguments?.getString(ARG_LEVEL_TITLE).orEmpty()
        val levelSubtitle = arguments?.getString(ARG_LEVEL_SUBTITLE).orEmpty()
        val fieldLabels = arguments?.getStringArray(ARG_FIELD_LABELS)?.toList().orEmpty()
        val nextActionId = arguments?.getInt(ARG_NEXT_ACTION_ID) ?: 0

        binding.textLevelTitle.text = levelTitle
        binding.textLevelSubtitle.text = levelSubtitle

        val fields = listOf(
            binding.fieldOne,
            binding.fieldTwo,
            binding.fieldThree,
            binding.fieldFour
        )

        val menus = listOf(
            binding.menuOne,
            binding.menuTwo,
            binding.menuThree,
            binding.menuFour
        )

        fields.forEachIndexed { index, layout ->
            if (index < fieldLabels.size) {
                val label = fieldLabels[index]
                layout.hint = label
                layout.visibility = View.VISIBLE
                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.list_item_dropdown,
                    resources.getStringArray(R.array.kanji_dropdown_options)
                )
                menus[index].setAdapter(adapter)
            } else {
                layout.visibility = View.GONE
            }
        }

        binding.buttonContinue.apply {
            text = if (nextActionId == 0) {
                getString(R.string.action_finish)
            } else {
                getString(R.string.action_continue)
            }
            setOnClickListener {
                if (nextActionId == 0) {
                    findNavController().navigate(R.id.action_kanjiLevelN2Fragment_to_welcomeFragment)
                } else {
                    findNavController().navigate(nextActionId)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_LEVEL_TITLE = "levelTitle"
        private const val ARG_LEVEL_SUBTITLE = "levelSubtitle"
        private const val ARG_FIELD_LABELS = "fieldLabels"
        private const val ARG_NEXT_ACTION_ID = "nextActionId"
    }
}
