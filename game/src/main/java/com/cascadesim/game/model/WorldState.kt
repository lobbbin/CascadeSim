package com.cascadesim.game.model

/**
 * Represents the complete state of the simulated world.
 * Immutable data class - create new instances for state changes.
 */
data class WorldState(
    val tickCount: Long = 0L,
    val entities: List<Entity> = emptyList(),
    val cascades: List<Cascade> = emptyList(),
    val configuration: SimulationConfig = SimulationConfig()
)

/**
 * Represents an entity within the simulation.
 */
data class Entity(
    val id: String,
    val name: String,
    val position: Position,
    val properties: Map<String, Any> = emptyMap()
)

/**
 * Represents a position in 2D space.
 */
data class Position(
    val x: Double,
    val y: Double
)

/**
 * Represents a cascade effect or chain reaction.
 */
data class Cascade(
    val id: String,
    val sourceEntityId: String,
    val intensity: Float,
    val affectedEntityIds: List<String>
)

/**
 * Configuration options for the simulation.
 */
data class SimulationConfig(
    val maxEntities: Int = 1000,
    val cascadePropagationRate: Float = 0.5f,
    val tickIntervalMs: Long = 100L
)
