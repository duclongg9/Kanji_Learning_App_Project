package com.example.kanjilearning.presentation.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kanjilearning.databinding.ItemCourseBinding
import com.example.kanjilearning.domain.model.CourseItem
import com.example.kanjilearning.presentation.view.CourseAdapter.CourseViewHolder

/**
 * VI: Adapter hiển thị danh sách khoá học với trạng thái mở khóa và tiến độ.
 * EN: RecyclerView adapter rendering course cards including progress and premium badge.
 */
class CourseAdapter(
    private val onCourseClick: (CourseItem) -> Unit,
    private val onUnlockClick: (CourseItem) -> Unit
) : ListAdapter<CourseItem, CourseViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<CourseItem>() {
        override fun areItemsTheSame(oldItem: CourseItem, newItem: CourseItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CourseItem, newItem: CourseItem): Boolean =
            oldItem == newItem
    }

    inner class CourseViewHolder(private val binding: ItemCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CourseItem) {
            binding.chipLevel.text = item.levelTag
            binding.textTitle.text = item.title
            binding.textDescription.text = item.description
            binding.textLessons.text = binding.root.context.getString(
                com.example.kanjilearning.R.string.courses_lesson_count,
                item.lessonCount
            )
            binding.progress.progress = item.progressPercent
            binding.textProgress.text = binding.root.context.getString(
                com.example.kanjilearning.R.string.courses_progress_percent,
                item.progressPercent
            )
            val isUnlocked = item.isUnlocked
            binding.textPrice.visibility = if (!isUnlocked && item.isPremium) View.VISIBLE else View.GONE
            binding.chipLocked.visibility = if (!isUnlocked && item.isPremium) View.VISIBLE else View.GONE
            binding.accentBar.setBackgroundResource(
                if (isUnlocked || !item.isPremium) {
                    com.example.kanjilearning.R.drawable.bg_neon_pill
                } else {
                    com.example.kanjilearning.R.drawable.bg_neon_outline
                }
            )
            if (!isUnlocked && item.isPremium) {
                binding.textPrice.text = binding.root.context.getString(
                    com.example.kanjilearning.R.string.courses_price_format,
                    item.priceVnd
                )
                binding.buttonPrimary.text = binding.root.context.getString(
                    com.example.kanjilearning.R.string.courses_unlock_cta
                )
                binding.chipLocked.setChipBackgroundColorResource(com.example.kanjilearning.R.color.glass_surface)
            } else {
                binding.textPrice.visibility = View.GONE
                binding.chipLocked.visibility = if (item.isPremium) View.VISIBLE else View.GONE
                if (item.isPremium) {
                    binding.chipLocked.text = binding.root.context.getString(com.example.kanjilearning.R.string.courses_premium_label)
                    binding.chipLocked.setChipBackgroundColorResource(com.example.kanjilearning.R.color.glass_surface)
                } else {
                    binding.chipLocked.text = ""
                }
                binding.buttonPrimary.text = binding.root.context.getString(
                    com.example.kanjilearning.R.string.courses_open_cta
                )
            }
            binding.buttonPrimary.setOnClickListener {
                if (isUnlocked || !item.isPremium) {
                    onCourseClick(item)
                } else {
                    onUnlockClick(item)
                }
            }
            binding.root.setOnClickListener {
                onCourseClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
