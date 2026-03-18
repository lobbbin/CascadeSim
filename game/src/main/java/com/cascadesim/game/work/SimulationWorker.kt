// PHASE 6: Background simulation worker using WorkManager

package com.cascadesim.game.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cascadesim.common.entity.EventEntity
import com.cascadesim.common.model.Decision
import com.cascadesim.common.model.DecisionType
import com.cascadesim.common.util.Result
import com.cascadesim.common.work.WorkConstants
import com.cascadesim.core.db.dao.WorldDao
import com.cascadesim.game.engine.CascadeEngine
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
        const val WORK_NAME = WorkConstants.WORK_NAME
        private const val MAX_BACKGROUND_TICKS = 5
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        try {
            cascadeEngine.initialize()

            val ticksCompleted = (0 until MAX_BACKGROUND_TICKS).count { tick ->
                val tickResult = cascadeEngine.tick()
                if (tickResult is Result.Success) {
                    if (Math.random() < 0.4f) {
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

    private suspend fun generateBackgroundEvent(tickIndex: Int) {
        val decisionTypes: List<DecisionType> = listOf(
            DecisionType.DIPLOMATIC,
            DecisionType.ECONOMIC,
            DecisionType.SOCIAL,
            DecisionType.ENVIRONMENTAL
        )

        val randomDecision = Decision(
            id = "background_event_${System.currentTimeMillis()}_$tickIndex",
            type = decisionTypes.random(),
            impactScore = (0.2f..0.6f).random(),
            targetEntityId = null,
            metadata = mapOf("source" to "background_simulation")
        )

        val events = cascadeEngine.processDecision(randomDecision)

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
