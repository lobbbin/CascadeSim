// PHASE 6: Updated to use types from :common module

package com.cascadesim.game.engine

import android.os.Trace
import com.cascadesim.common.model.Decision
import com.cascadesim.common.model.DecisionType
import com.cascadesim.common.model.Event
import com.cascadesim.common.model.EventSeverity
import com.cascadesim.common.model.WorldState
import com.cascadesim.common.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Core simulation engine for CascadeSim.
 * Handles world state updates, cascade calculations, and simulation logic.
 */
class CascadeEngine {

    private var currentState: WorldState = WorldState()
    private val eventChainHistory = mutableMapOf<String, MutableList<Event>>()
    private var eventSink: EventSink? = null

    fun setEventSink(sink: EventSink) {
        this.eventSink = sink
    }

    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.Default) {
        return@withContext try {
            currentState = WorldState()
            eventChainHistory.clear()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to initialize cascade engine")
        }
    }

    suspend fun processDecision(decision: Decision): List<Event> = withContext(Dispatchers.Default) {
        Trace.beginSection("CascadeEvaluate")
        
        try {
            val generatedEvents = mutableListOf<Event>()
            val chainId = UUID.randomUUID().toString()

            val baseSeverity = calculateSeverityFromImpact(decision.impactScore)

            val primaryEvent = Event(
                id = UUID.randomUUID().toString(),
                description = generateEventDescription(decision, baseSeverity),
                severity = baseSeverity,
                chainId = chainId,
                sourceDecisionId = decision.id,
                affectedEntityIds = decision.targetEntityId?.let { listOf(it) } ?: emptyList()
            )
            generatedEvents.add(primaryEvent)

            val cascadeEvents = calculateCascadeEffects(decision, primaryEvent, chainId)
            generatedEvents.addAll(cascadeEvents)

            currentState = currentState.copy(
                tickCount = currentState.tickCount + 1,
                cascades = currentState.cascades + com.cascadesim.common.model.Cascade(
                    id = chainId,
                    sourceEntityId = decision.targetEntityId ?: "system",
                    intensity = decision.impactScore,
                    affectedEntityIds = generatedEvents.flatMap { it.affectedEntityIds }
                )
            )

            eventChainHistory[chainId] = generatedEvents.toMutableList()
            eventSink?.onEventsGenerated(generatedEvents)

            generatedEvents
        } finally {
            Trace.endSection()
        }
    }

    private suspend fun calculateCascadeEffects(
        decision: Decision,
        primaryEvent: Event,
        chainId: String
    ): List<Event> = withContext(Dispatchers.Default) {
        val cascadeEvents = mutableListOf<Event>()

        val cascadeProbability = when (decision.type) {
            DecisionType.DIPLOMATIC -> 0.3f
            DecisionType.ECONOMIC -> 0.5f
            DecisionType.MILITARY -> 0.7f
            DecisionType.SOCIAL -> 0.4f
            DecisionType.ENVIRONMENTAL -> 0.6f
            DecisionType.EMERGENCY -> 0.8f
        }

        if (decision.impactScore > 0.5f && cascadeProbability > Math.random().toFloat()) {
            val secondarySeverity = calculateSecondarySeverity(primaryEvent.severity)
            if (secondarySeverity != null) {
                cascadeEvents.add(
                    Event(
                        id = UUID.randomUUID().toString(),
                        description = generateCascadeDescription(decision, secondarySeverity),
                        severity = secondarySeverity,
                        chainId = chainId,
                        sourceDecisionId = decision.id
                    )
                )
            }
        }

        cascadeEvents
    }

    private fun calculateSeverityFromImpact(impactScore: Float): EventSeverity {
        return when {
            impactScore >= 0.9f -> EventSeverity.CATASTROPHIC
            impactScore >= 0.7f -> EventSeverity.CRITICAL
            impactScore >= 0.5f -> EventSeverity.HIGH
            impactScore >= 0.3f -> EventSeverity.MEDIUM
            else -> EventSeverity.LOW
        }
    }

    private fun calculateSecondarySeverity(primary: EventSeverity): EventSeverity? {
        return when (primary) {
            EventSeverity.CATASTROPHIC -> EventSeverity.CRITICAL
            EventSeverity.CRITICAL -> EventSeverity.HIGH
            EventSeverity.HIGH -> EventSeverity.MEDIUM
            EventSeverity.MEDIUM -> EventSeverity.LOW
            EventSeverity.LOW -> null
        }
    }

    private fun generateEventDescription(decision: Decision, severity: EventSeverity): String {
        val targetInfo = decision.targetEntityId?.let { " targeting $it" } ?: ""
        return "${decision.type} decision${targetInfo} with ${severity.name} impact"
    }

    private fun generateCascadeDescription(decision: Decision, severity: EventSeverity): String {
        return "Cascade effect from ${decision.type} decision: ${severity.name} secondary impact"
    }

    suspend fun tick(): Result<WorldState> = withContext(Dispatchers.Default) {
        return@withContext try {
            currentState = currentState.copy(
                tickCount = currentState.tickCount + 1
            )
            Result.Success(currentState)
        } catch (e: Exception) {
            Result.Error(e, "Simulation tick failed")
        }
    }

    fun getState(): WorldState = currentState

    suspend fun reset(): Result<Unit> = initialize()

    fun getEventChain(chainId: String): List<Event> {
        return eventChainHistory[chainId]?.toList() ?: emptyList()
    }
}
