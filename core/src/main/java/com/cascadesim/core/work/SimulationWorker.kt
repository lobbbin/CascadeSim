// PHASE 6: Background simulation worker using WorkManager

package com.cascadesim.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cascadesim.core.db.dao.WorldDao
import com.cascadesim.core.db.entity.EventEntity
import com.cascadesim.game.engine.CascadeEngine
import com.cascadesim.game.model.Decision
import com.cascadesim.game.model.DecisionType
import com.cascadesim.game.model.EventSeverity
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background worker that runs simulation ticks even when app is closed.
 * PHASE 6: Added for procedural event generation while user sleeps
 */
@HiltWorker
class SimulationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val worldDao: WorldDao,
    private val cascadeEngine: CascadeEngine,
    private val gson: Gson
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "cascade_simulation_worker"
        private const val MAX_BACKGROUND_TICKS = 5
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        try {
            // Initialize engine if needed
            cascadeEngine.initialize()

            // Run multiple ticks to simulate passage of time
            val ticksCompleted = (0 until MAX_BACKGROUND_TICKS).count { tick ->
                val tickResult = cascadeEngine.tick()
                if (tickResult is com.cascadesim.core.util.Result.Success) {
                    // Generate procedural background events occasionally
                    if (Math.random() < 0.4) { // 40% chance of event per tick
                        generateBackgroundEvent(tick)
                    }
                    true
                } else {
                    false
                }
            }

            if (ticksCompleted > 0) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    /**
     * Generates a procedural background event.
     * PHASE 6: Simulates events happening while user is away
     */
    private suspend fun generateBackgroundEvent(tickIndex: Int) {
        val decisionTypes = listOf(
            DecisionType.DIPLOMATIC,
            DecisionType.ECONOMIC,
            DecisionType.SOCIAL,
            DecisionType.ENVIRONMENTAL
        )

        val randomDecision = Decision(
            id = "background_event_${System.currentTimeMillis()}_$tickIndex",
            type = decisionTypes.random(),
            impactScore = (0.2f..0.6f).random(), // Lower impact for background events
            targetEntityId = null,
            metadata = mapOf("source" to "background_simulation")
        )

        val events = cascadeEngine.processDecision(randomDecision)

        // Persist events with background flag
        val eventEntities = events.map { event ->
            EventEntity(
                id = event.id,
                description = "[Background] ${event.description}",
                severity = event.severity.name,
                chainId = event.chainId,
                sourceDecisionId = event.sourceDecisionId,
                timestamp = event.timestamp,
                affectedEntityIdsJson = gson.toJson(event.affectedEntityIds)
            )
        }

        withContext(Dispatchers.IO) {
            worldDao.insertEvents(eventEntities)
        }
    }
}
