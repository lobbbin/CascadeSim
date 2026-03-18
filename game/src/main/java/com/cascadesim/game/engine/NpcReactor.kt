// PHASE 4: Added NPC reaction system for trait-based event responses

package com.cascadesim.game.engine

import com.cascadesim.common.entity.NpcEntity
import com.cascadesim.common.model.Event
import com.cascadesim.common.model.EventSeverity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Represents an NPC's reaction to an event.
 * PHASE 4: Added for NPC reaction system
 */
data class ReactionOutcome(
    val npcId: String,
    val eventId: String,
    val reactionType: ReactionType,
    val intensity: Float,
    val reasoning: String,
    val triggeredAction: String? = null
)

/**
 * Types of reactions an NPC can have.
 * PHASE 4: Four reaction types for diverse NPC behaviors
 */
enum class ReactionType {
    SUPPORT,    // NPC supports the decision/event
    OPPOSE,     // NPC opposes the decision/event
    NEUTRAL,    // NPC has no strong feeling
    RADICALIZE  // NPC becomes extreme in response
}

/**
 * NPC trait data class for reaction calculations.
 * PHASE 4: Used to evaluate NPC responses
 */
data class NpcTraits(
    val aggression: Float = 0.5f,
    val diplomacy: Float = 0.5f,
    val stability: Float = 0.5f,
    val adaptability: Float = 0.5f,
    val ideology: String = "moderate"
)

/**
 * NPC data class for reaction system.
 * PHASE 4: Simplified NPC representation for reaction calculations
 */
data class Npc(
    val id: String,
    val name: String,
    val traits: NpcTraits,
    val hiddenBiasScore: Float = 0.5f
)

/**
 * NPC Reaction Engine that evaluates NPC traits against events.
 * PHASE 4: Added for procedural NPC behavior system
 */
class NpcReactor {

    /**
     * Calculates an NPC's reaction to an event.
     * Runs on Dispatchers.Default for CPU-intensive calculations.
     * 
     * @param npc The NPC reacting to the event
     * @param event The event that triggered the reaction
     * @return ReactionOutcome describing the NPC's response
     */
    suspend fun calculateReaction(npc: Npc, event: Event): ReactionOutcome = withContext(Dispatchers.Default) {
        val reactionType = determineReactionType(npc, event)
        val intensity = calculateReactionIntensity(npc, event, reactionType)
        val reasoning = generateReasoning(npc, event, reactionType, intensity)
        val triggeredAction = determineTriggeredAction(npc, reactionType, intensity)
        
        ReactionOutcome(
            npcId = npc.id,
            eventId = event.id,
            reactionType = reactionType,
            intensity = intensity,
            reasoning = reasoning,
            triggeredAction = triggeredAction
        )
    }

    /**
     * Calculates reactions for multiple NPCs to an event.
     * PHASE 4: Batch processing for efficiency
     */
    suspend fun calculateReactions(npcs: List<Npc>, event: Event): List<ReactionOutcome> = withContext(Dispatchers.Default) {
        npcs.map { npc -> calculateReaction(npc, event) }
    }

    /**
     * Determines the type of reaction based on NPC traits and event properties.
     */
    private fun determineReactionType(npc: Npc, event: Event): ReactionType {
        // Calculate alignment score based on traits and event severity
        val alignmentScore = calculateAlignmentScore(npc, event)
        
        // Radicalization check: high aggression + high bias + severe event
        if (npc.traits.aggression > 0.7f && npc.hiddenBiasScore > 0.7f && 
            event.severity >= EventSeverity.HIGH) {
            return ReactionType.RADICALIZE
        }
        
        // Support check: high diplomacy + positive alignment
        if (npc.traits.diplomacy > 0.6f && alignmentScore > 0.3f) {
            return ReactionType.SUPPORT
        }
        
        // Oppose check: low alignment or high aggression with negative alignment
        if (alignmentScore < -0.3f || (npc.traits.aggression > 0.5f && alignmentScore < 0f)) {
            return ReactionType.OPPOSE
        }
        
        // Default to neutral
        return ReactionType.NEUTRAL
    }

    /**
     * Calculates the intensity of the reaction (0.0 to 1.0).
     */
    private fun calculateReactionIntensity(
        npc: Npc,
        event: Event,
        reactionType: ReactionType
    ): Float {
        val baseIntensity = when (event.severity) {
            EventSeverity.LOW -> 0.2f
            EventSeverity.MEDIUM -> 0.4f
            EventSeverity.HIGH -> 0.6f
            EventSeverity.CRITICAL -> 0.8f
            EventSeverity.CATASTROPHIC -> 1.0f
        }
        
        // Modify intensity based on traits
        val traitModifier = when (reactionType) {
            ReactionType.RADICALIZE -> npc.traits.aggression * 0.5f
            ReactionType.SUPPORT -> npc.traits.diplomacy * 0.3f
            ReactionType.OPPOSE -> npc.traits.aggression * 0.4f
            ReactionType.NEUTRAL -> 0f
        }
        
        // Modify intensity based on hidden bias
        val biasModifier = npc.hiddenBiasScore * 0.2f
        
        return (baseIntensity + traitModifier + biasModifier).coerceIn(0f, 1f)
    }

    /**
     * Generates a reasoning string explaining the reaction.
     */
    private fun generateReasoning(
        npc: Npc,
        event: Event,
        reactionType: ReactionType,
        intensity: Float
    ): String {
        val severityDesc = when (event.severity) {
            EventSeverity.LOW -> "minor"
            EventSeverity.MEDIUM -> "moderate"
            EventSeverity.HIGH -> "significant"
            EventSeverity.CRITICAL -> "severe"
            EventSeverity.CATASTROPHIC -> "catastrophic"
        }
        
        return when (reactionType) {
            ReactionType.SUPPORT -> 
                "${npc.name} supports the ${severityDesc} event due to diplomatic nature (intensity: ${intensity.toPercent()})"
            ReactionType.OPPOSE -> 
                "${npc.name} opposes the ${severityDesc} event due to conflicting interests (intensity: ${intensity.toPercent()})"
            ReactionType.NEUTRAL -> 
                "${npc.name} remains neutral about the ${severityDesc} event (intensity: ${intensity.toPercent()})"
            ReactionType.RADICALIZE -> 
                "${npc.name} becomes radicalized by the ${severityDesc} event (intensity: ${intensity.toPercent()})"
        }
    }

    /**
     * Determines any triggered action based on reaction type and intensity.
     */
    private fun determineTriggeredAction(
        npc: Npc,
        reactionType: ReactionType,
        intensity: Float
    ): String? {
        // Only trigger actions for high intensity reactions
        if (intensity < 0.6f) return null
        
        return when (reactionType) {
            ReactionType.SUPPORT -> "NPC_${npc.id}_OFFERS_ALLIANCE"
            ReactionType.OPPOSE -> "NPC_${npc.id}_INITIATES_SANCTIONS"
            ReactionType.RADICALIZE -> "NPC_${npc.id}_MOBILIZES_FORCES"
            ReactionType.NEUTRAL -> null
        }
    }

    /**
     * Calculates alignment score between NPC traits and event.
     * Returns value between -1.0 (opposed) and 1.0 (aligned).
     */
    private fun calculateAlignmentScore(npc: Npc, event: Event): Float {
        // Base alignment from hidden bias
        var alignment = (npc.hiddenBiasScore - 0.5f) * 2f // Normalize to -1 to 1
        
        // Adjust based on ideology
        alignment += when (npc.traits.ideology) {
            "aggressive" -> npc.traits.aggression * 0.3f
            "diplomatic" -> npc.traits.diplomacy * 0.3f
            "stable" -> npc.traits.stability * 0.2f
            else -> 0f
        }
        
        // Adjust based on event severity (severe events polarize more)
        val severityFactor = when (event.severity) {
            EventSeverity.LOW -> 0.1f
            EventSeverity.MEDIUM -> 0.2f
            EventSeverity.HIGH -> 0.4f
            EventSeverity.CRITICAL -> 0.6f
            EventSeverity.CATASTROPHIC -> 0.8f
        }
        
        alignment += (npc.traits.adaptability - 0.5f) * severityFactor
        
        return alignment.coerceIn(-1f, 1f)
    }

    /**
     * Extension function to format float as percentage.
     */
    private fun Float.toPercent(): String = "${(this * 100).toInt()}%"
}

/**
 * Extension function to create Npc from entity data.
 * PHASE 4: Convenience method for converting from database entity
 */
fun NpcEntity.toNpc(traits: NpcTraits = NpcTraits()): Npc {
    return Npc(
        id = this.id,
        name = this.name,
        traits = traits,
        hiddenBiasScore = this.hiddenBiasScore
    )
}
