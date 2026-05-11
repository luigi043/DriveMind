package com.drivemind.ui.history

import com.drivemind.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.drivemind.databinding.ItemVoiceNoteBinding
import com.drivemind.domain.model.NoteType
import com.drivemind.domain.model.VoiceNote
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VoiceNoteAdapter(
    private val onItemClick: (VoiceNote) -> Unit,
    private val onDeleteClick: (VoiceNote) -> Unit,
    private val onCheckChanged: (VoiceNote) -> Unit
) : ListAdapter<VoiceNote, VoiceNoteAdapter.VoiceNoteViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoiceNoteViewHolder {
        val binding = ItemVoiceNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VoiceNoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VoiceNoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VoiceNoteViewHolder(
        private val binding: ItemVoiceNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: VoiceNote) {
            binding.apply {
                tvContent.text = note.processedContent
                tvTimestamp.text = formatTimestamp(note.timestamp)
                tvType.text = root.context.getString(note.type.labelResId)

                // Task checkbox
                if (note.type == NoteType.TASK) {
                    checkTask.visibility = View.VISIBLE
                    checkTask.isChecked = note.isCompleted
                    checkTask.setOnCheckedChangeListener { _, _ -> onCheckChanged(note) }
                    tvContent.alpha = if (note.isCompleted) 0.5f else 1.0f
                } else {
                    checkTask.visibility = View.GONE
                    tvContent.alpha = 1.0f
                }

                // Category emoji
                tvCategory.text = note.category.emoji

                root.setOnClickListener { onItemClick(note) }
                btnDelete.setOnClickListener { onDeleteClick(note) }
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val now = System.currentTimeMillis()
            val diffMs = now - timestamp
            val context = binding.root.context
            return when {
                diffMs < 60_000 -> context.getString(R.string.just_now)
                diffMs < 3_600_000 -> context.getString(R.string.minutes_ago, diffMs / 60_000)
                diffMs < 86_400_000 -> context.getString(R.string.hours_ago, diffMs / 3_600_000)
                diffMs < 604_800_000 -> SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
                else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<VoiceNote>() {
        override fun areItemsTheSame(oldItem: VoiceNote, newItem: VoiceNote) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: VoiceNote, newItem: VoiceNote) =
            oldItem == newItem
    }
}
