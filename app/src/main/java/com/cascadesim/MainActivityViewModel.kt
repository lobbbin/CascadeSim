package com.cascadesim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cascadesim.core.repository.WorldRepository
import com.cascadesim.core.util.Result
import com.cascadesim.game.model.Decision
import com.cascadesim.game.model.Event
import com.cascadesim.game.model.WorldState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State representing the current state of the application.
 */
data class UiState(
    val worldState: WorldState = WorldState(),
    val recentEvents: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for MainActivity.
 * Injects Repository and exposes uiState for UI observation.
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val worldRepository: WorldRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    
    init {
        initializeSimulation()
        observeWorldState()
    }
    
    /**
     * Initializes the simulation on first launch.
     */
    private fun initializeSimulation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = worldRepository.initialize()
            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to initialize simulation"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }
    
    /**
     * Observes world state changes from the repository.
     */
    private fun observeWorldState() {
        viewModelScope.launch {
            worldRepository.observeWorldState().collect { worldState ->
                _uiState.value = _uiState.value.copy(worldState = worldState)
            }
        }
    }
    
    /**
     * Handles a decision made by the user.
     * Processes through the cascade engine and updates UI state.
     */
    fun onDecisionMade(decision: Decision) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val events = worldRepository.processDecision(decision)
                _events.value = events
                _uiState.value = _uiState.value.copy(
                    recentEvents = events,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to process decision"
                )
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
                    _uiState.value = _uiState.value.copy(
                        worldState = result.data,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "Tick failed"
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = worldRepository.reset()
            when (result) {
                is Result.Success -> {
                    _uiState.value = UiState()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Reset failed"
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
        _uiState.value = _uiState.value.copy(error = null)
    }
}
