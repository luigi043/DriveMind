package com.drivemind.ui.driving

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.drivemind.databinding.FragmentDrivingBinding
import com.drivemind.domain.model.NoteType
import com.drivemind.utils.DrivingDetector
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DrivingFragment : Fragment() {

    private var _binding: FragmentDrivingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DrivingViewModel by viewModels()

    private val locationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.startDrivingDetection()
        }
    }

    private val microphoneLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startListening()
        } else {
            _binding?.let {
                Snackbar.make(it.root, com.drivemind.R.string.permission_audio_settings, Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrivingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDrivingDetection(savedInstanceState == null)
        setupUi()
        observeState()
    }

    private fun setupDrivingDetection(requestPermission: Boolean) {
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            viewModel.startDrivingDetection()
        } else if (requestPermission && !hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            locationLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun setupUi() {
        binding.btnMic.setOnClickListener { handleMicClick() }
    }

    private fun handleMicClick() {
        if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
            microphoneLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        when (viewModel.uiState.value) {
            is DrivingViewModel.UiState.Listening -> viewModel.stopListening()
            is DrivingViewModel.UiState.Idle,
            is DrivingViewModel.UiState.Error -> viewModel.startListening()
            else -> { /* Processing or saved — ignore tap */ }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { updateUi(it) } }
                launch { viewModel.drivingState.collect { updateDrivingBadge(it) } }
                launch { viewModel.speedKmh.collect { updateSpeed(it) } }
            }
        }
    }

    private fun updateUi(state: DrivingViewModel.UiState) {
        when (state) {
            is DrivingViewModel.UiState.Idle -> {
                binding.apply {
                    btnMic.setIconResource(com.drivemind.R.drawable.ic_mic)
                    btnMic.contentDescription = getString(com.drivemind.R.string.tap_to_speak)
                    tvStatus.text = getString(com.drivemind.R.string.tap_to_speak)
                    tvPartial.visibility = View.GONE
                    progressWave.visibility = View.GONE
                    cardSaved.visibility = View.GONE
                }
            }
            is DrivingViewModel.UiState.Listening -> {
                binding.apply {
                    btnMic.setIconResource(com.drivemind.R.drawable.ic_mic_active)
                    btnMic.contentDescription = getString(com.drivemind.R.string.finish_recording)
                    tvStatus.text = getString(com.drivemind.R.string.listening)
                    tvPartial.visibility = View.GONE
                    progressWave.visibility = View.VISIBLE
                    cardSaved.visibility = View.GONE
                }
            }
            is DrivingViewModel.UiState.Processing -> {
                binding.apply {
                    tvStatus.text = getString(com.drivemind.R.string.processing)
                    tvPartial.text = state.partialText
                    tvPartial.visibility = View.VISIBLE
                    progressWave.visibility = View.VISIBLE
                    cardSaved.visibility = View.GONE
                }
            }
            is DrivingViewModel.UiState.Saved -> {
                binding.apply {
                    btnMic.setIconResource(com.drivemind.R.drawable.ic_mic)
                    btnMic.contentDescription = getString(com.drivemind.R.string.tap_to_speak)
                    progressWave.visibility = View.GONE
                    tvPartial.visibility = View.GONE
                    cardSaved.visibility = View.VISIBLE
                    tvSavedContent.text = state.note.processedContent
                    tvSavedType.text = when (state.note.type) {
                        NoteType.TASK -> getString(com.drivemind.R.string.type_task)
                        NoteType.REMINDER -> getString(com.drivemind.R.string.type_reminder)
                        NoteType.IDEA -> getString(com.drivemind.R.string.type_idea)
                        NoteType.NOTE -> getString(com.drivemind.R.string.type_note)
                    }
                    tvStatus.text = getString(com.drivemind.R.string.saved)
                }
            }
            is DrivingViewModel.UiState.Error -> {
                binding.apply {
                    btnMic.setIconResource(com.drivemind.R.drawable.ic_mic)
                    btnMic.contentDescription = getString(com.drivemind.R.string.tap_to_speak)
                    progressWave.visibility = View.GONE
                    tvPartial.visibility = View.GONE
                    tvStatus.text = getString(com.drivemind.R.string.tap_to_retry)
                    cardSaved.visibility = View.GONE
                }
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDrivingBadge(state: DrivingDetector.DrivingState) {
        binding.chipDrivingStatus.apply {
            when (state) {
                DrivingDetector.DrivingState.DRIVING -> {
                    text = getString(com.drivemind.R.string.driving_status)
                    setChipBackgroundColorResource(com.drivemind.R.color.color_driving)
                    visibility = View.VISIBLE
                }
                DrivingDetector.DrivingState.PARKED -> {
                    text = getString(com.drivemind.R.string.parked_status)
                    setChipBackgroundColorResource(com.drivemind.R.color.color_parked)
                    visibility = View.VISIBLE
                }
                DrivingDetector.DrivingState.UNKNOWN -> {
                    visibility = View.GONE
                }
            }
        }
    }

    private fun updateSpeed(speedKmh: Float) {
        if (speedKmh > 0) {
            binding.tvSpeed.text = getString(com.drivemind.R.string.speed_unit, speedKmh.toInt())
            binding.tvSpeed.visibility = View.VISIBLE
        } else {
            binding.tvSpeed.visibility = View.GONE
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), permission) ==
                PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
