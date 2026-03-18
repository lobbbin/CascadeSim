package com.cascadesim.game.model

/**
 * Represents a decision made by the player or system.
 * Decisions trigger cascade events based on their type and impact.
 */
data class Decision(
    val id: String,
    val type: DecisionType,
    val impactScore: Float,
    val targetEntityId: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Types of decisions available in the simulation.
 */
enum class DecisionType {
    DIPLOMATIC,
    ECONOMIC,
    MILITARY,
    SOCIAL,
    ENVIRONMENTAL,
    EMERGENCY
}
