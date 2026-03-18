package com.cascadesim.ui.model

import com.cascadesim.common.entity.CountryEntity
import com.cascadesim.common.entity.EventEntity
import com.cascadesim.common.model.EventSeverity

/**
 * UI State representing the current state of the application.
 * Sealed class for different UI states.
 */
sealed class UiState {
    object Loading : UiState()
    data class Success(
        val worldState: com.cascadesim.common.model.WorldState = com.cascadesim.common.model.WorldState(),
        val countries: List<CountryUiModel> = emptyList(),
        val recentEvents: List<EventUiModel> = emptyList(),
        val cascadeLevel: CascadeLevel = CascadeLevel.STABLE
    ) : UiState()
    data class Error(val message: String) : UiState()
}

/**
 * UI-friendly country model.
 */
data class CountryUiModel(
    val id: String,
    val name: String,
    val stability: Float,
    val stabilityPercent: Int,
    val lastUpdated: Long
) {
    companion object {
        fun fromEntity(entity: CountryEntity): CountryUiModel {
            return CountryUiModel(
                id = entity.id,
                name = entity.name,
                stability = entity.stability,
                stabilityPercent = (entity.stability * 100).toInt(),
                lastUpdated = entity.lastUpdated
            )
        }
    }
}

/**
 * UI-friendly event model.
 */
data class EventUiModel(
    val id: String,
    val description: String,
    val severity: EventSeverity,
    val chainId: String,
    val timestamp: Long,
    val isHighSeverity: Boolean
) {
    companion object {
        fun fromEntity(entity: EventEntity): EventUiModel {
            val severity = EventSeverity.valueOf(entity.severity)
            return EventUiModel(
                id = entity.id,
                description = entity.description,
                severity = severity,
                chainId = entity.chainId,
                timestamp = entity.timestamp,
                isHighSeverity = severity == EventSeverity.HIGH ||
                    severity == EventSeverity.CRITICAL ||
                    severity == EventSeverity.CATASTROPHIC
            )
        }
    }
}

/**
 * Cascade level indicating the current simulation state.
 */
enum class CascadeLevel {
    STABLE,
    UNSTABLE,
    CRITICAL,
    CASCADE
}
