// PHASE 6: Updated to use types from :common module

package com.cascadesim.core.repository

import com.cascadesim.common.entity.CountryEntity
import com.cascadesim.common.entity.EventEntity
import com.cascadesim.common.entity.NpcEntity
import com.cascadesim.common.model.Decision
import com.cascadesim.common.model.Event
import com.cascadesim.common.model.EventSeverity
import com.cascadesim.common.model.WorldState
import com.cascadesim.common.util.Result
import com.cascadesim.core.db.dao.WorldDao
import com.cascadesim.game.engine.CascadeEngine
import com.cascadesim.game.engine.EventSink
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Checkpoint for save/load functionality.
 */
data class Checkpoint(
    val id: String,
    val label: String,
    val timestamp: Long = System.currentTimeMillis(),
    val worldStateHash: String,
    val tickCount: Long,
    val decisionHistory: List<String>
)

/**
 * UI-specific world state combining database and engine data.
 */
data class UiWorldState(
    val worldState: WorldState = WorldState(),
    val countries: List<CountryEntity> = emptyList(),
    val npcs: List<NpcEntity> = emptyList(),
    val recentEvents: List<Event> = emptyList()
)

/**
 * Repository bridging database and game engine.
 */
@Singleton
class WorldRepository @Inject constructor(
    private val worldDao: WorldDao,
    private val cascadeEngine: CascadeEngine,
    private val gson: Gson
) : EventSink {

    private val decisionHistory = mutableListOf<String>()
    private val checkpoints = mutableMapOf<String, Checkpoint>()

    fun observeCountries(): Flow<List<CountryEntity>> = worldDao.getAllCountries()

    fun observeNpcs(): Flow<List<NpcEntity>> = worldDao.getAllNpcs()

    fun observeUnstableCountries(threshold: Float = 0.5f): Flow<List<CountryEntity>> =
        worldDao.getUnstableCountries(threshold)

    fun observeBiasedNpcs(threshold: Float = 0.5f): Flow<List<NpcEntity>> =
        worldDao.getNpcsByBiasThreshold(threshold)

    fun observeRecentEvents(limit: Int = 50): Flow<List<EventEntity>> =
        worldDao.getRecentEvents(limit)

    fun getWorldStateFlow(): Flow<UiWorldState> = combine(
        flow {
            while (true) {
                emit(cascadeEngine.getState())
                kotlinx.coroutines.delay(100)
            }
        }.flowOn(Dispatchers.Default),
        worldDao.getAllCountries(),
        worldDao.getAllNpcs(),
        worldDao.getRecentEvents(20)
    ) { worldState, countries, npcs, eventEntities ->
        val events = eventEntities.map { entity ->
            Event(
                id = entity.id,
                description = entity.description,
                severity = EventSeverity.valueOf(entity.severity),
                chainId = entity.chainId,
                sourceDecisionId = entity.sourceDecisionId,
                affectedEntityIds = gson.fromJson(entity.affectedEntityIdsJson, Array<String>::class.java).toList(),
                timestamp = entity.timestamp
            )
        }
        UiWorldState(
            worldState = worldState,
            countries = countries,
            npcs = npcs,
            recentEvents = events
        )
    }.flowOn(Dispatchers.IO)

    suspend fun getCountry(id: String): CountryEntity? = withContext(Dispatchers.IO) {
        worldDao.getCountryById(id)
    }

    suspend fun getNpc(id: String): NpcEntity? = withContext(Dispatchers.IO) {
        worldDao.getNpcById(id)
    }

    suspend fun saveCountry(country: CountryEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            worldDao.insertCountry(country)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save country")
        }
    }

    suspend fun saveNpc(npc: NpcEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            worldDao.insertNpc(npc)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save NPC")
        }
    }

    suspend fun processDecision(decision: Decision): Result<List<Event>> = withContext(Dispatchers.Default) {
        try {
            decisionHistory.add(decision.id)
            val events = cascadeEngine.processDecision(decision)
            Result.Success(events)
        } catch (e: Exception) {
            Result.Error(e, "Failed to process decision")
        }
    }

    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            cascadeEngine.setEventSink(this@WorldRepository)
            decisionHistory.clear()
            checkpoints.clear()
            cascadeEngine.initialize()
        } catch (e: Exception) {
            Result.Error(e, "Failed to initialize simulation")
        }
    }

    suspend fun reset(): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            worldDao.deleteAllEvents()
            decisionHistory.clear()
            checkpoints.clear()
            cascadeEngine.reset()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to reset simulation")
        }
    }

    suspend fun tick(): Result<WorldState> = withContext(Dispatchers.Default) {
        cascadeEngine.tick()
    }

    suspend fun saveWorldState(state: WorldState): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save world state")
        }
    }

    suspend fun createCheckpoint(label: String): String = withContext(Dispatchers.Default) {
        val currentState = cascadeEngine.getState()
        val checkpointId = UUID.randomUUID().toString()

        val checkpoint = Checkpoint(
            id = checkpointId,
            label = label,
            worldStateHash = currentState.hashCode().toString(),
            tickCount = currentState.tickCount,
            decisionHistory = decisionHistory.toList()
        )

        checkpoints[checkpointId] = checkpoint
        checkpointId
    }

    suspend fun restoreCheckpoint(id: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val checkpoint = checkpoints[id]
                ?: return@withContext Result.Error(Exception("Checkpoint not found"))

            cascadeEngine.reset()
            worldDao.deleteAllEvents()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to restore checkpoint")
        }
    }

    fun listCheckpoints(): List<Checkpoint> = checkpoints.values.toList()

    fun deleteCheckpoint(id: String): Boolean = checkpoints.remove(id) != null

    override suspend fun onEventsGenerated(events: List<Event>) = withContext(Dispatchers.IO) {
        try {
            val eventEntities = events.map { event ->
                EventEntity(
                    id = event.id,
                    description = event.description,
                    severity = event.severity.name,
                    chainId = event.chainId,
                    sourceDecisionId = event.sourceDecisionId,
                    timestamp = event.timestamp,
                    affectedEntityIdsJson = gson.toJson(event.affectedEntityIds)
                )
            }
            worldDao.insertEvents(eventEntities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getEventsByChain(chainId: String): List<Event> = withContext(Dispatchers.IO) {
        worldDao.getEventsByChain(chainId).map { entity ->
            Event(
                id = entity.id,
                description = entity.description,
                severity = EventSeverity.valueOf(entity.severity),
                chainId = entity.chainId,
                sourceDecisionId = entity.sourceDecisionId,
                affectedEntityIds = gson.fromJson(entity.affectedEntityIdsJson, Array<String>::class.java).toList(),
                timestamp = entity.timestamp
            )
        }
    }

    fun scheduleBackgroundSim(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<SimulationWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(WorkConstants.WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WorkConstants.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelBackgroundSim(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WorkConstants.WORK_NAME)
    }
}
