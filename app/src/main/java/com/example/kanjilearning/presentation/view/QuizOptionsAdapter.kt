package com.example.kanjilearning.presentation.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kanjilearning.R
import com.example.kanjilearning.databinding.ItemQuizOptionBinding
import com.example.kanjilearning.domain.model.QuizChoice

/**
 * VI: Adapter hiển thị các đáp án trắc nghiệm kèm trạng thái đúng/sai.
 * EN: Quiz options adapter with selection and reveal styling.
 */
class QuizOptionsAdapter(
    private val onChoiceClick: (QuizChoice) -> Unit
) : ListAdapter<QuizChoice, QuizOptionsAdapter.OptionViewHolder>(DiffCallback) {

    var selectedChoiceId: Long? = null
    var revealed: Boolean = false
    var correctChoiceId: Long? = null

    object DiffCallback : DiffUtil.ItemCallback<QuizChoice>() {
        override fun areItemsTheSame(oldItem: QuizChoice, newItem: QuizChoice): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: QuizChoice, newItem: QuizChoice): Boolean =
            oldItem == newItem
    }

    inner class OptionViewHolder(private val binding: ItemQuizOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: QuizChoice) {
            binding.textOption.text = item.content
            val context = binding.root.context
            val isSelected = selectedChoiceId == item.id
            val isCorrectChoice = correctChoiceId == item.id
            val backgroundColor = when {
                revealed && isCorrectChoice -> R.color.quiz_option_correct
                revealed && isSelected && !isCorrectChoice -> R.color.quiz_option_wrong
                !revealed && isSelected -> R.color.quiz_option_selected
                else -> R.color.bg_option
            }
            binding.root.setCardBackgroundColor(ContextCompat.getColor(context, backgroundColor))
            val textColor = if (revealed && isCorrectChoice) {
                ContextCompat.getColor(context, R.color.white)
            } else {
                ContextCompat.getColor(context, R.color.text_primary)
            }
            binding.textOption.setTextColor(textColor)
            binding.root.setOnClickListener { onChoiceClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemQuizOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
