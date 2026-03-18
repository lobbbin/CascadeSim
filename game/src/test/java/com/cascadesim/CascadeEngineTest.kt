// PHASE 5: Unit tests for CascadeEngine

package com.cascadesim.game.engine

import com.cascadesim.common.model.Decision
import com.cascadesim.common.model.DecisionType
import com.cascadesim.common.model.EventSeverity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CascadeEngine.
 * PHASE 5: Basic test scaffolding
 */
class CascadeEngineTest {

    private lateinit var engine: CascadeEngine

    @Before
    fun setup() {
        engine = CascadeEngine()
    }

    @Test
    fun testEngineInitialization() = runTest {
        val result = engine.initialize()
        
        assertTrue(result is Result.Success)
        assertEquals(0L, engine.getState().tickCount)
    }

    @Test
    fun testProcessDecisionReturnsNonEmptyEvents() = runTest {
        engine.initialize()
        
        val decision = Decision(
            id = "test_decision",
            type = DecisionType.ECONOMIC,
            impactScore = 0.5f
        )
        
        val events = engine.processDecision(decision)
        
        assertTrue("Events list should not be empty", events.isNotEmpty())
        assertEquals("test_decision", events.first().sourceDecisionId)
    }

    @Test
    fun testProcessDecisionWithHighImpact() = runTest {
        engine.initialize()
        
        val decision = Decision(
            id = "high_impact_decision",
            type = DecisionType.MILITARY,
            impactScore = 0.9f
        )
        
        val events = engine.processDecision(decision)

        assertTrue(events.isNotEmpty())
        // High impact should have at least one event with HIGH or higher severity
        val hasHighSeverity = events.any {
            it.severity.ordinal >= EventSeverity.HIGH.ordinal
        }
        assertTrue("High impact decision should generate high severity event", hasHighSeverity)
    }

    @Test
    fun testProcessDecisionWithLowImpact() = runTest {
        engine.initialize()

        val decision = Decision(
            id = "low_impact_decision",
            type = DecisionType.DIPLOMATIC,
            impactScore = 0.1f
        )

        val events = engine.processDecision(decision)

        assertTrue(events.isNotEmpty())
        // Low impact should have LOW or MEDIUM severity
        val hasLowSeverity = events.any {
            it.severity.ordinal <= EventSeverity.MEDIUM.ordinal
        }
        assertTrue("Low impact decision should generate low severity event", hasLowSeverity)
    }

    @Test
    fun testTickIncrementsTickCount() = runTest {
        engine.initialize()
        
        val initialTick = engine.getState().tickCount
        val result = engine.tick()
        
        assertTrue(result is Result.Success)
        assertEquals(initialTick + 1, engine.getState().tickCount)
    }

    @Test
    fun testResetClearsState() = runTest {
        engine.initialize()
        
        // Make a decision to change state
        val decision = Decision(
            id = "test",
            type = DecisionType.ECONOMIC,
            impactScore = 0.5f
        )
        engine.processDecision(decision)
        
        val tickBeforeReset = engine.getState().tickCount
        assertTrue(tickBeforeReset > 0)
        
        // Reset
        val result = engine.reset()
        
        assertTrue(result is Result.Success)
        assertEquals(0L, engine.getState().tickCount)
    }

    @Test
    fun testDifferentDecisionTypesHaveDifferentCascadeProbabilities() = runTest {
        engine.initialize()
        
        // Military has highest cascade probability (0.7)
        val militaryDecision = Decision(
            id = "military",
            type = DecisionType.MILITARY,
            impactScore = 0.6f
        )
        
        // Diplomatic has lowest cascade probability (0.3)
        val diplomaticDecision = Decision(
            id = "diplomatic",
            type = DecisionType.DIPLOMATIC,
            impactScore = 0.6f
        )
        
        // Run multiple times to test probability
        val militaryCascadeCount = (1..10).count {
            engine.processDecision(militaryDecision).size > 1
        }
        
        engine.reset()
        
        val diplomaticCascadeCount = (1..10).count {
            engine.processDecision(diplomaticDecision).size > 1
        }
        
        // Military should have equal or more cascades than diplomatic (probabilistic)
        // This test may occasionally fail due to randomness, but should pass most of the time
        assertTrue(
            "Military decisions should have higher cascade probability than diplomatic",
            militaryCascadeCount >= diplomaticCascadeCount
        )
    }
}
