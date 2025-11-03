package com.example.kanjilearning.presentation.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kanjilearning.databinding.ItemKanjiDetailBinding
import com.example.kanjilearning.domain.model.KanjiModel

/**
 * VI: Adapter hiển thị chi tiết từng Kanji trong lesson.
 * EN: Adapter rendering kanji cards with readings and examples.
 */
class KanjiDetailAdapter : ListAdapter<KanjiModel, KanjiDetailAdapter.KanjiViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<KanjiModel>() {
        override fun areItemsTheSame(oldItem: KanjiModel, newItem: KanjiModel): Boolean =
            oldItem.characters == newItem.characters

        override fun areContentsTheSame(oldItem: KanjiModel, newItem: KanjiModel): Boolean =
            oldItem == newItem
    }

    inner class KanjiViewHolder(private val binding: ItemKanjiDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: KanjiModel) {
            binding.textCharacter.text = item.characters
            binding.textMeaning.text = binding.root.context.getString(
                com.example.kanjilearning.R.string.kanji_meaning_format,
                item.meaningVi,
                item.meaningEn
            )
            binding.textReadings.text = binding.root.context.getString(
                com.example.kanjilearning.R.string.kanji_reading_format,
                item.onyomi,
                item.kunyomi
            )
            binding.textExample.text = binding.root.context.getString(
                com.example.kanjilearning.R.string.kanji_example_format,
                item.example,
                item.exampleTranslation
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KanjiViewHolder {
        val binding = ItemKanjiDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KanjiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KanjiViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
