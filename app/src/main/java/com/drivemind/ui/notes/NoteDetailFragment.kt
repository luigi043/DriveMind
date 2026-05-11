package com.drivemind.ui.notes

import com.drivemind.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.drivemind.databinding.FragmentNoteDetailBinding
import com.drivemind.domain.model.NoteCategory
import com.drivemind.domain.model.NoteType
import com.drivemind.domain.model.VoiceNote
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class NoteDetailFragment : Fragment() {

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                com.drivemind.R.id.action_edit -> {
                    viewModel.startEditing()
                    true
                }
                com.drivemind.R.id.action_delete -> {
                    showDeleteConfirmation()
                    true
                }
                com.drivemind.R.id.action_read -> {
                    viewModel.readNoteAloud()
                    true
                }
                else -> false
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is NoteDetailViewModel.UiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is NoteDetailViewModel.UiState.Viewing -> {
                            binding.progressBar.visibility = View.GONE
                            renderNote(state.note)
                            setEditMode(false)
                        }
                        is NoteDetailViewModel.UiState.Editing -> {
                            binding.progressBar.visibility = View.GONE
                            renderEditMode(state.note)
                            setEditMode(true)
                        }
                        is NoteDetailViewModel.UiState.Deleted -> {
                            findNavController().navigateUp()
                        }
                        is NoteDetailViewModel.UiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun renderNote(note: VoiceNote) {
        binding.apply {
            tvContent.text = note.processedContent
            tvOriginal.text = if (note.rawContent != note.processedContent) {
                getString(R.string.original_prefix, note.rawContent)
            } else ""
            tvTimestamp.text = SimpleDateFormat(
                "dd MMM yyyy, HH:mm", Locale.getDefault()
            ).format(Date(note.timestamp))
            tvType.text = getString(note.type.labelResId)
            tvCategory.text = "${note.category.emoji} ${getString(note.category.labelResId)}"
            if (note.durationSeconds > 0) {
                tvDuration.text = getString(R.string.recording_format, note.durationSeconds)
                tvDuration.visibility = View.VISIBLE
            } else {
                tvDuration.visibility = View.GONE
            }
        }
    }

    private fun renderEditMode(note: VoiceNote) {
        binding.apply {
            etEditContent.setText(note.processedContent)
            btnSaveEdit.setOnClickListener {
                val newContent = etEditContent.text.toString()
                if (newContent.isNotBlank()) {
                    viewModel.saveEdits(newContent, note.type, note.category)
                }
            }
            btnCancelEdit.setOnClickListener {
                viewModel.cancelEditing()
            }
        }
    }

    private fun setEditMode(editing: Boolean) {
        binding.apply {
            groupView.visibility = if (editing) View.GONE else View.VISIBLE
            groupEdit.visibility = if (editing) View.VISIBLE else View.GONE
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_note_title)
            .setMessage(R.string.delete_note_message)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deleteNote() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
