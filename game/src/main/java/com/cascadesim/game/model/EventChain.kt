// PHASE 4: Added EventChain model for cascade visualization

package com.cascadesim.game.model

/**
 * Represents a complete chain of cascading events from a single decision.
 * PHASE 4: Added for event chain visualization feature
 */
data class EventChain(
    val rootDecisionId: String,
    val events: List<Event>,
    val severityScore: Float
) {
    /**
     * Calculates the total severity score for this chain.
     * Higher scores indicate more severe cascade effects.
     */
    fun calculateSeverityScore(): Float {
        if (events.isEmpty()) return 0f
        
        val severityWeights = mapOf(
            EventSeverity.LOW to 1f,
            EventSeverity.MEDIUM to 2f,
            EventSeverity.HIGH to 4f,
            EventSeverity.CRITICAL to 8f,
            EventSeverity.CATASTROPHIC to 16f
        )
        
        val totalWeight = events.sumOf { event ->
            severityWeights[event.severity] ?: 1f
        }
        
        // Normalize by number of events and cap at 1.0
        return (totalWeight / (events.size * 16f)).coerceIn(0f, 1f)
    }
    
    /**
     * Returns visualization data for UI rendering.
     * PHASE 4: Provides structured data for cascade visualization
     */
    fun getVisualizationData(): List<UiEventNode> {
        return events.mapIndexed { index, event ->
            UiEventNode(
                id = event.id,
                label = event.severity.name,
                description = event.description,
                level = index,
                severity = event.severity,
                isRoot = index == 0,
                timestamp = event.timestamp
            )
        }
    }
    
    /**
     * Returns the root (primary) event in the chain.
     */
    fun getRootEvent(): Event? = events.firstOrNull()
    
    /**
     * Returns all secondary (cascade) events.
     */
    fun getCascadeEvents(): List<Event> = events.drop(1)
    
    /**
     * Checks if this chain has cascade effects (more than one event).
     */
    fun hasCascadeEffects(): Boolean = events.size > 1
}

/**
 * UI-friendly representation of an event node in a cascade chain.
 * PHASE 4: Used for visualization components
 */
data class UiEventNode(
    val id: String,
    val label: String,
    val description: String,
    val level: Int,
    val severity: EventSeverity,
    val isRoot: Boolean,
    val timestamp: Long
) {
    /**
     * Returns the color weight for visualization (0.0 to 1.0).
     */
    fun getColorWeight(): Float {
        return when (severity) {
            EventSeverity.LOW -> 0.2f
            EventSeverity.MEDIUM -> 0.4f
            EventSeverity.HIGH -> 0.6f
            EventSeverity.CRITICAL -> 0.8f
            EventSeverity.CATASTROPHIC -> 1.0f
        }
    }
}

/**
 * Extension function to convert a list of events into an EventChain.
 * PHASE 4: Convenience method for building chains
 */
fun List<Event>.toEventChain(rootDecisionId: String): EventChain {
    return EventChain(
        rootDecisionId = rootDecisionId,
        events = this.sortedBy { it.timestamp },
        severityScore = 0f // Will be calculated
    ).copy(severityScore = EventChain(rootDecisionId, this.sortedBy { it.timestamp }, 0f).calculateSeverityScore())
}
