// PHASE 6: Updated to use types from :common module

package com.cascadesim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cascadesim.common.model.Decision
import com.cascadesim.common.model.EventSeverity
import com.cascadesim.common.model.WorldState
import com.cascadesim.core.repository.WorldRepository
import com.cascadesim.common.util.Result
import com.cascadesim.ui.model.CascadeLevel
import com.cascadesim.ui.model.EventUiModel
import com.cascadesim.ui.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val worldRepository: WorldRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        initializeSimulation()
    }

    private fun initializeSimulation() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = worldRepository.initialize()
            when (result) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(cascadeLevel = CascadeLevel.STABLE)
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.message ?: "Failed to initialize simulation")
                }
                is Result.Loading -> {}
            }
        }
    }

    fun onDecisionMade(decision: Decision) {
        viewModelScope.launch {
            val result = worldRepository.processDecision(decision)
            when (result) {
                is Result.Success -> {
                    val events = result.data.map { event ->
                        EventUiModel(
                            id = event.id,
                            description = event.description,
                            severity = event.severity,
                            chainId = event.chainId,
                            timestamp = event.timestamp,
                            isHighSeverity = event.severity == EventSeverity.HIGH ||
                                event.severity == EventSeverity.CRITICAL ||
                                event.severity == EventSeverity.CATASTROPHIC
                        )
                    }

                    val currentSuccess = _uiState.value as? UiState.Success
                    _uiState.value = UiState.Success(
                        worldState = currentSuccess?.worldState ?: WorldState(),
                        recentEvents = events,
                        cascadeLevel = calculateCascadeLevel(events)
                    )
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.message ?: "Failed to process decision")
                }
                is Result.Loading -> {}
            }
        }
    }

    fun onTick() {
        viewModelScope.launch {
            val result = worldRepository.tick()
            when (result) {
                is Result.Success -> {
                    val currentSuccess = _uiState.value as? UiState.Success
                    _uiState.value = UiState.Success(
                        worldState = result.data,
                        recentEvents = currentSuccess?.recentEvents ?: emptyList(),
                        cascadeLevel = currentSuccess?.cascadeLevel ?: CascadeLevel.STABLE
                    )
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.message ?: "Tick failed")
                }
                is Result.Loading -> {}
            }
        }
    }

    fun onReset() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = worldRepository.reset()
            when (result) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(cascadeLevel = CascadeLevel.STABLE)
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.message ?: "Reset failed")
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        val currentState = _uiState.value
        if (currentState is UiState.Error) {
            _uiState.value = UiState.Success(cascadeLevel = CascadeLevel.STABLE)
        }
    }

    fun createCheckpoint(label: String) {
        viewModelScope.launch {
            worldRepository.createCheckpoint(label)
        }
    }

    fun restoreCheckpoint(checkpointId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = worldRepository.restoreCheckpoint(checkpointId)
            when (result) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(cascadeLevel = CascadeLevel.STABLE)
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.message ?: "Failed to restore checkpoint")
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun calculateCascadeLevel(events: List<EventUiModel>): CascadeLevel {
        if (events.isEmpty()) return CascadeLevel.STABLE

        val hasCatastrophic = events.any { it.severity == EventSeverity.CATASTROPHIC }
        val hasCritical = events.any { it.severity == EventSeverity.CRITICAL }
        val hasHigh = events.any { it.severity == EventSeverity.HIGH }

        return when {
            hasCatastrophic -> CascadeLevel.CASCADE
            hasCritical -> CascadeLevel.CRITICAL
            hasHigh -> CascadeLevel.UNSTABLE
            else -> CascadeLevel.STABLE
        }
    }
}
