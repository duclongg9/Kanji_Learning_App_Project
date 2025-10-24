package com.example.kanjilearning.presentation.home.kanji

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.ItemKanjiLevelBinding
import com.google.android.material.textview.MaterialTextView

internal data class KanjiSublevel(
    val name: String,
    val isAccessible: Boolean
)

internal data class KanjiLevel(
    val name: String,
    val isFree: Boolean,
    val sublevels: List<KanjiSublevel>
)

class KanjiLevelAdapter(
    private val onSublevelClick: (KanjiLevel, KanjiSublevel) -> Unit
) : RecyclerView.Adapter<KanjiLevelAdapter.LevelViewHolder>() {

    private val items = mutableListOf<KanjiLevel>()
    private val expandedPositions = mutableSetOf<Int>()

    fun submitList(levels: List<KanjiLevel>) {
        items.clear()
        items.addAll(levels)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemKanjiLevelBinding.inflate(inflater, parent, false)
        return LevelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class LevelViewHolder(
        private val binding: ItemKanjiLevelBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(level: KanjiLevel, position: Int) {
            val expanded = expandedPositions.contains(position)
            binding.levelName.text = level.name
            binding.freeBadge.isVisible = level.isFree
            binding.expandIcon.rotation = if (expanded) 180f else 0f
            binding.sublevelContainer.isVisible = expanded

            if (expanded) {
                renderSublevels(level)
            } else {
                binding.sublevelContainer.removeAllViews()
            }

            binding.root.setOnClickListener {
                if (expanded) {
                    expandedPositions.remove(position)
                } else {
                    expandedPositions.add(position)
                }
                notifyItemChanged(position)
            }
        }

        private fun renderSublevels(level: KanjiLevel) {
            binding.sublevelContainer.removeAllViews()
            val context = binding.root.context
            val horizontalPadding = context.resources.getDimensionPixelSize(R.dimen.kanji_level_sublevel_padding_horizontal)
            val verticalPadding = context.resources.getDimensionPixelSize(R.dimen.kanji_level_sublevel_padding_vertical)
            val marginTop = context.resources.getDimensionPixelSize(R.dimen.kanji_level_sublevel_margin_top)
            level.sublevels.forEachIndexed { index, sublevel ->
                val textView = MaterialTextView(context).apply {
                    text = "${level.name} - ${sublevel.name}"
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body1)
                    setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = if (index == 0) 0 else marginTop
                    }
                    background = ContextCompat.getDrawable(context, R.drawable.bg_sublevel_item)
                    if (sublevel.isAccessible) {
                        setTextColor(ContextCompat.getColor(context, R.color.sakura_text))
                        setTypeface(typeface, Typeface.BOLD)
                        alpha = 1f
                    } else {
                        setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                        setTypeface(typeface, Typeface.NORMAL)
                        alpha = 0.35f
                    }
                    setOnClickListener { onSublevelClick(level, sublevel) }
                }
                binding.sublevelContainer.addView(textView)
            }
        }
    }
}
