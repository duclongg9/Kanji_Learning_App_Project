package com.example.kanjilearning.presentation.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.ItemLessonBinding
import com.example.kanjilearning.domain.model.LessonSummary
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

/**
 * VI: Adapter cho danh sách lesson ở màn course detail.
 * EN: Renders lesson cards showing order, summary and progress.
 */
class LessonAdapter(
    private val onLessonClick: (LessonSummary) -> Unit
) : ListAdapter<LessonSummary, LessonAdapter.LessonViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<LessonSummary>() {
        override fun areItemsTheSame(oldItem: LessonSummary, newItem: LessonSummary): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: LessonSummary, newItem: LessonSummary): Boolean =
            oldItem == newItem
    }

    inner class LessonViewHolder(private val binding: ItemLessonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LessonSummary) {
            binding.textOrder.text = binding.root.context.getString(R.string.lesson_order_label, item.orderIndex)
            binding.textTitle.text = item.title
            binding.textSummary.text = item.summary
            binding.textMeta.text = binding.root.context.getString(
                R.string.lesson_meta,
                item.questionCount,
                item.durationMinutes
            )
            val progressPercent = if (item.questionCount == 0) 0 else ((item.bestScore.toDouble() / item.questionCount) * 100).roundToInt()
            binding.progress.progress = progressPercent
            binding.textProgress.text = binding.root.context.getString(
                R.string.lesson_progress_label,
                item.bestScore,
                item.questionCount
            )
            if (item.completed) {
                binding.chipStatus.setChipBackgroundColorResource(R.color.pill_vip)
                binding.chipStatus.setText(R.string.lesson_status_completed)
                binding.chipStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.primary_vivid))
            } else {
                binding.chipStatus.setChipBackgroundColorResource(R.color.pill_chip_disabled)
                binding.chipStatus.setText(R.string.lesson_status_pending)
                binding.chipStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_secondary))
            }
            binding.root.setOnClickListener { onLessonClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
