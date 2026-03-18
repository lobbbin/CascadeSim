package com.cascadesim.common.model

/**
 * Represents a complete chain of cascading events from a single decision.
 * Provides structured data for cascade visualization.
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

        return (totalWeight / (events.size * 16f)).coerceIn(0f, 1f)
    }

    /**
     * Returns visualization data for UI rendering.
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

    fun getRootEvent(): Event? = events.firstOrNull()
    fun getCascadeEvents(): List<Event> = events.drop(1)
    fun hasCascadeEffects(): Boolean = events.size > 1
}

/**
 * UI-friendly representation of an event node in a cascade chain.
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
 */
fun List<Event>.toEventChain(rootDecisionId: String): EventChain {
    return EventChain(
        rootDecisionId = rootDecisionId,
        events = this.sortedBy { it.timestamp },
        severityScore = 0f
    ).copy(severityScore = EventChain(rootDecisionId, this.sortedBy { it.timestamp }, 0f).calculateSeverityScore())
}
