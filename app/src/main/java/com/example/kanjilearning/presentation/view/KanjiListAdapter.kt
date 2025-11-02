package com.example.kanjilearning.presentation.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kanjilearning.data.model.KanjiEntity
import com.example.kanjilearning.databinding.ItemKanjiBinding

/**
 * VI: Adapter đơn giản cho RecyclerView, dùng ViewBinding để tránh findViewById.
 * EN: Minimal RecyclerView adapter using view binding.
 */
class KanjiListAdapter : RecyclerView.Adapter<KanjiListAdapter.KanjiViewHolder>() {

    private val items = mutableListOf<KanjiEntity>()

    fun submitList(newItems: List<KanjiEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KanjiViewHolder {
        val binding = ItemKanjiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KanjiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KanjiViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class KanjiViewHolder(private val binding: ItemKanjiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: KanjiEntity) {
            binding.wordTextView.text = item.word
            binding.meaningTextView.text = item.meaning
        }
    }
}
