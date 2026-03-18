// PHASE 6: Updated to use types from :common module

package com.cascadesim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cascadesim.common.entity.EventEntity
import com.cascadesim.common.model.Decision
import com.cascadesim.common.model.EventSeverity
import com.cascadesim.common.model.WorldState
import com.cascadesim.common.util.Result
import com.cascadesim.core.repository.WorldRepository
import com.cascadesim.game.engine.CascadeEngine
import com.cascadesim.ui.model.CascadeLevel
import com.cascadesim.ui.model.EventUiModel
import com.cascadesim.ui.model.UiState
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val worldRepository: WorldRepository,
    private val cascadeEngine: CascadeEngine,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        initializeSimulation()
    }

    private fun initializeSimulation() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = runCatching {
                cascadeEngine.initialize()
                Result.Success(Unit)
            }.getOrElse { e ->
                Result.Error(e, "Failed to initialize simulation")
            }
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
            val result = runCatching {
                val events = cascadeEngine.processDecision(decision)
                
                // Save events to database
                val eventEntities = events.map { event ->
                    EventEntity(
                        id = event.id,
                        description = event.description,
                        severity = event.severity.name,
                        chainId = event.chainId,
                        sourceDecisionId = event.sourceDecisionId,
                        timestamp = event.timestamp,
                        affectedEntityIdsJson = gson.toJson(event.affectedEntityIds)
                    )
                }
                worldRepository.saveEvents(eventEntities)
                
                Result.Success(events)
            }.getOrElse { e ->
                Result.Error(e, "Failed to process decision")
            }
            
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
            val result = runCatching {
                cascadeEngine.tick()
            }.getOrElse { e ->
                Result.Error(e, "Tick failed")
            }
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
            val repoResult = worldRepository.reset()
            val engineResult = runCatching {
                cascadeEngine.reset()
                Result.Success(Unit)
            }.getOrElse { e ->
                Result.Error(e, "Failed to reset engine")
            }
            
            when {
                repoResult is Result.Error || engineResult is Result.Error -> {
                    _uiState.value = UiState.Error("Reset failed")
                }
                else -> {
                    _uiState.value = UiState.Success(cascadeLevel = CascadeLevel.STABLE)
                }
            }
        }
    }

    fun clearError() {
        val currentState = _uiState.value
        if (currentState is UiState.Error) {
            _uiState.value = UiState.Success(cascadeLevel = CascadeLevel.STABLE)
        }
    }

    // Checkpoint functionality temporarily disabled
    // fun createCheckpoint(label: String) { ... }
    // fun restoreCheckpoint(checkpointId: String) { ... }

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
