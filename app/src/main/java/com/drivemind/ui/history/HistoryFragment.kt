package com.drivemind.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.drivemind.R
import com.drivemind.databinding.FragmentHistoryBinding
import com.drivemind.domain.model.NoteType
import com.drivemind.domain.model.VoiceNote
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: VoiceNoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupFilterChips()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = VoiceNoteAdapter(
            onItemClick = { note ->
                val action = HistoryFragmentDirections.actionHistoryToDetail(note)
                findNavController().navigate(action)
            },
            onDeleteClick = { note -> handleDelete(note) },
            onCheckChanged = { note -> viewModel.toggleCompletion(note) }
        )
        binding.recyclerNotes.adapter = adapter
        binding.recyclerNotes.itemAnimator = null // Disable animation for performance
    }

    private fun setupSearch() {
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.onSearchQueryChanged(text?.toString() ?: "")
        }
    }

    private fun setupFilterChips() {
        val filterTypes = listOf(null) + NoteType.values().toList()
        filterTypes.forEach { type ->
            val chip = Chip(requireContext()).apply {
                text = type?.let { getString(it.labelResId) } ?: getString(R.string.filter_all)
                isCheckable = true
                isChecked = type == null
                setOnClickListener { viewModel.setFilter(type) }
            }
            binding.chipGroupFilters.addView(chip)
        }
    }

    private fun handleDelete(note: VoiceNote) {
        viewModel.deleteNote(note)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.notes.collect { notes ->
                        adapter.submitList(notes)
                        binding.tvEmpty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
                        binding.recyclerNotes.visibility = if (notes.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
                launch {
                    viewModel.deletedNote.collect { deletedNote ->
                        Snackbar.make(binding.root, R.string.note_deleted, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_undo) { viewModel.undoDelete(deletedNote) }
                            .show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
