package com.example.kanjilearning.presentation.home.kanji

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kanjilearning.databinding.ItemKanjiBinding
import com.example.kanjilearning.domain.model.Kanji

/**
 * VI: Adapter hiển thị từng Kanji trong RecyclerView.
 */
class KanjiAdapter : RecyclerView.Adapter<KanjiAdapter.ViewHolder>() {

    private val items = mutableListOf<Kanji>()

    fun submitList(newItems: List<Kanji>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKanjiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemKanjiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Kanji) {
            binding.textCharacter.text = item.character
            binding.textOn.text = "Onyomi: ${item.onyomi}"
            binding.textKun.text = "Kunyomi: ${item.kunyomi}"
            binding.textMeaning.text = "Ý nghĩa: ${item.meaning}"
            binding.textMeta.text = "JLPT ${item.jlptLevel.label} • Độ khó ${item.difficulty}"
        }
    }
}
