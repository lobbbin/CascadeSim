// FIX: PHASE 4 - Removed redundant EventEntity conversion, use Event directly
// FIX: PHASE 4 - Added checkpoint methods

package com.cascadesim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cascadesim.core.repository.WorldRepository
import com.cascadesim.core.util.Result
import com.cascadesim.game.model.Decision
import com.cascadesim.game.model.Event
import com.cascadesim.ui.model.CascadeLevel
import com.cascadesim.ui.model.EventUiModel
import com.cascadesim.ui.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for MainActivity.
 * Injects Repository and exposes uiState for UI observation.
 * 
 * PHASE 4: Fixed Event conversion and added checkpoint support
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val worldRepository: WorldRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        initializeSimulation()
    }

    /**
     * Initializes the simulation on first launch.
     */
    private fun initializeSimulation() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = worldRepository.initialize()
            when (result) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(
                        cascadeLevel = CascadeLevel.STABLE
                    )
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(
                        result.message ?: "Failed to initialize simulation"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Handles a decision made by the user.
     * Processes through the cascade engine and updates UI state.
     */
    fun onDecisionMade(decision: Decision) {
        viewModelScope.launch {
            val result = worldRepository.processDecision(decision)
            when (result) {
                is Result.Success -> {
                    // PHASE 4 FIX: Use Event directly instead of converting through EventEntity
                    val events = result.data.map { event ->
                        EventUiModel(
                            id = event.id,
                            description = event.description,
                            severity = event.severity,
                            chainId = event.chainId,
                            timestamp = event.timestamp,
                            isHighSeverity = event.severity == com.cascadesim.game.model.EventSeverity.HIGH ||
                                event.severity == com.cascadesim.game.model.EventSeverity.CRITICAL ||
                                event.severity == com.cascadesim.game.model.EventSeverity.CATASTROPHIC
                        )
                    }

                    val currentSuccess = _uiState.value as? UiState.Success
                    _uiState.value = UiState.Success(
                        worldState = currentSuccess?.worldState ?: com.cascadesim.game.model.WorldState(),
                        recentEvents = events,
                        cascadeLevel = calculateCascadeLevel(events)
                    )
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(
                        result.message ?: "Failed to process decision"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Advances the simulation by one tick.
     */
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
                    _uiState.value = UiState.Error(
                        result.message ?: "Tick failed"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Resets the simulation.
     */
    fun onReset() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = worldRepository.reset()
            when (result) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(
                        cascadeLevel = CascadeLevel.STABLE
                    )
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(
                        result.message ?: "Reset failed"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Clears the current error state.
     */
    fun clearError() {
        val currentState = _uiState.value
        if (currentState is UiState.Error) {
            _uiState.value = UiState.Success(
                cascadeLevel = CascadeLevel.STABLE
            )
        }
    }

    /**
     * PHASE 4: Creates a checkpoint.
     */
    fun createCheckpoint(label: String) {
        viewModelScope.launch {
            val checkpointId = worldRepository.createCheckpoint(label)
            // Could emit a snackbar event here
        }
    }

    /**
     * PHASE 4: Restores a checkpoint.
     */
    fun restoreCheckpoint(checkpointId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = worldRepository.restoreCheckpoint(checkpointId)
            when (result) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(
                        cascadeLevel = CascadeLevel.STABLE
                    )
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(
                        result.message ?: "Failed to restore checkpoint"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Calculates cascade level based on recent events.
     */
    private fun calculateCascadeLevel(events: List<EventUiModel>): CascadeLevel {
        if (events.isEmpty()) return CascadeLevel.STABLE

        val hasCatastrophic = events.any {
            it.severity == com.cascadesim.game.model.EventSeverity.CATASTROPHIC
        }
        val hasCritical = events.any {
            it.severity == com.cascadesim.game.model.EventSeverity.CRITICAL
        }
        val hasHigh = events.any {
            it.severity == com.cascadesim.game.model.EventSeverity.HIGH
        }

        return when {
            hasCatastrophic -> CascadeLevel.CASCADE
            hasCritical -> CascadeLevel.CRITICAL
            hasHigh -> CascadeLevel.UNSTABLE
            else -> CascadeLevel.STABLE
        }
    }
}
