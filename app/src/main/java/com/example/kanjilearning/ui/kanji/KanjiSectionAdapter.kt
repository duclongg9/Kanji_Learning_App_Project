package com.example.kanjilearning.ui.kanji

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.ItemKanjiEntryBinding
import com.example.kanjilearning.databinding.ItemKanjiLevelBinding
import com.example.kanjilearning.domain.util.AccessTier

/**
 * VI: Adapter hiển thị từng nhóm Kanji theo cấp độ JLPT.
 */
class KanjiSectionAdapter(
    private val onEdit: (KanjiEntryUi) -> Unit,
    private val onDelete: (KanjiEntryUi) -> Unit
) : ListAdapter<KanjiSectionUi, KanjiSectionViewHolder>(Diff) {

    private val expandedLevels = mutableSetOf<String>()
    private var canManage: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KanjiSectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemKanjiLevelBinding.inflate(inflater, parent, false)
        return KanjiSectionViewHolder(binding, ::toggleLevel, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: KanjiSectionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, expandedLevels.contains(item.level.label), canManage)
    }

    fun updateManageable(value: Boolean) {
        if (canManage != value) {
            canManage = value
            notifyDataSetChanged()
        }
    }

    private fun toggleLevel(level: String) {
        if (!expandedLevels.add(level)) {
            expandedLevels.remove(level)
        }
        notifyDataSetChanged()
    }

    private object Diff : DiffUtil.ItemCallback<KanjiSectionUi>() {
        override fun areItemsTheSame(oldItem: KanjiSectionUi, newItem: KanjiSectionUi): Boolean =
            oldItem.level == newItem.level

        override fun areContentsTheSame(oldItem: KanjiSectionUi, newItem: KanjiSectionUi): Boolean =
            oldItem == newItem
    }
}

class KanjiSectionViewHolder(
    private val binding: ItemKanjiLevelBinding,
    private val onToggle: (String) -> Unit,
    private val onEdit: (KanjiEntryUi) -> Unit,
    private val onDelete: (KanjiEntryUi) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(section: KanjiSectionUi, expanded: Boolean, canManage: Boolean) {
        val context = binding.root.context
        binding.tvTitle.text =
            context.getString(R.string.kanji_section_level_format, section.level.label)
        binding.icChevron.rotation = if (expanded) 180f else 0f
        binding.sublevels.isVisible = expanded && section.entries.isNotEmpty()
        binding.textEmpty.isVisible = expanded && section.entries.isEmpty()

        binding.sublevels.removeAllViews()
        if (expanded) {
            val inflater = LayoutInflater.from(context)
            section.entries.forEach { entry ->
                val itemBinding = ItemKanjiEntryBinding.inflate(inflater, binding.sublevels, false)
                itemBinding.textCharacter.text = entry.character
                itemBinding.textMeaning.text = entry.meaning
                itemBinding.textPronunciation.text =
                    context.getString(R.string.kanji_entry_pronunciation_format, entry.onyomi, entry.kunyomi)
                itemBinding.textDifficulty.text =
                    context.getString(R.string.kanji_entry_difficulty_format, entry.difficulty)
                val tierLabel = when (entry.accessTier) {
                    AccessTier.FREE -> context.getString(R.string.kanji_tier_free)
                    AccessTier.VIP -> context.getString(R.string.kanji_tier_vip)
                }
                itemBinding.textTier.text =
                    context.getString(R.string.kanji_entry_tier_format, tierLabel)
                itemBinding.buttonEdit.isVisible = canManage
                itemBinding.buttonDelete.isVisible = canManage
                itemBinding.buttonEdit.setOnClickListener { onEdit(entry) }
                itemBinding.buttonDelete.setOnClickListener { onDelete(entry) }
                binding.sublevels.addView(itemBinding.root)
            }
        }

        binding.header.setOnClickListener { onToggle(section.level.label) }
        binding.header.alpha = if (section.entries.isEmpty()) 0.7f else 1f
    }
}
