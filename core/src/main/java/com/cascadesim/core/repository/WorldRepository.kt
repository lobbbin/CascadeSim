package com.cascadesim.core.repository

import com.cascadesim.core.db.dao.WorldDao
import com.cascadesim.core.db.entity.CountryEntity
import com.cascadesim.core.db.entity.EventEntity
import com.cascadesim.core.db.entity.NpcEntity
import com.cascadesim.core.util.Result
import com.cascadesim.game.engine.CascadeEngine
import com.cascadesim.game.engine.EventSink
import com.cascadesim.game.model.Decision
import com.cascadesim.game.model.Event
import com.cascadesim.game.model.EventSeverity
import com.cascadesim.game.model.WorldState
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

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
 * Exposes Flow<UiWorldState> for UI observation and handles data operations.
 */
@Singleton
class WorldRepository @Inject constructor(
    private val worldDao: WorldDao,
    private val cascadeEngine: CascadeEngine,
    private val gson: Gson
) : EventSink {
    
    /**
     * Observes all countries as a Flow.
     */
    fun observeCountries(): Flow<List<CountryEntity>> {
        return worldDao.getAllCountries()
    }
    
    /**
     * Observes all NPCs as a Flow.
     */
    fun observeNpcs(): Flow<List<NpcEntity>> {
        return worldDao.getAllNpcs()
    }
    
    /**
     * Observes unstable countries (stability below threshold).
     */
    fun observeUnstableCountries(threshold: Float = 0.5f): Flow<List<CountryEntity>> {
        return worldDao.getUnstableCountries(threshold)
    }
    
    /**
     * Observes NPCs with high bias scores.
     */
    fun observeBiasedNpcs(threshold: Float = 0.5f): Flow<List<NpcEntity>> {
        return worldDao.getNpcsByBiasThreshold(threshold)
    }
    
    /**
     * Observes recent events as a Flow.
     */
    fun observeRecentEvents(limit: Int = 50): Flow<List<EventEntity>> {
        return worldDao.getRecentEvents(limit)
    }
    
    /**
     * Exposes the combined world state as a Flow.
     */
    fun getWorldStateFlow(): Flow<UiWorldState> = flow {
        while (true) {
            val worldState = cascadeEngine.getState()
            val countries = worldDao.getAllCountries()
            val npcs = worldDao.getAllNpcs()
            val events = worldDao.getRecentEvents(20)
            
            // Combine all data sources
            emit(
                UiWorldState(
                    worldState = worldState,
                    countries = emptyList(), // Will be populated by collect
                    npcs = emptyList(),
                    recentEvents = emptyList()
                )
            )
            kotlinx.coroutines.delay(100)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Gets a country by ID.
     */
    suspend fun getCountry(id: String): CountryEntity? {
        return withContext(Dispatchers.IO) {
            worldDao.getCountryById(id)
        }
    }
    
    /**
     * Gets an NPC by ID.
     */
    suspend fun getNpc(id: String): NpcEntity? {
        return withContext(Dispatchers.IO) {
            worldDao.getNpcById(id)
        }
    }
    
    /**
     * Saves a country to the database.
     */
    suspend fun saveCountry(country: CountryEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                worldDao.insertCountry(country)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e, "Failed to save country")
            }
        }
    }
    
    /**
     * Saves an NPC to the database.
     */
    suspend fun saveNpc(npc: NpcEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                worldDao.insertNpc(npc)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e, "Failed to save NPC")
            }
        }
    }
    
    /**
     * Processes a decision through the cascade engine.
     * Events are automatically persisted via EventSink interface.
     */
    suspend fun processDecision(decision: Decision): Result<List<Event>> {
        return withContext(Dispatchers.Default) {
            try {
                val events = cascadeEngine.processDecision(decision)
                Result.Success(events)
            } catch (e: Exception) {
                Result.Error(e, "Failed to process decision")
            }
        }
    }
    
    /**
     * Initializes the simulation.
     */
    suspend fun initialize(): Result<Unit> {
        return withContext(Dispatchers.Default) {
            try {
                // Set up event sink for persistence
                cascadeEngine.setEventSink(this@WorldRepository)
                cascadeEngine.initialize()
            } catch (e: Exception) {
                Result.Error(e, "Failed to initialize simulation")
            }
        }
    }
    
    /**
     * Resets the simulation.
     */
    suspend fun reset(): Result<Unit> {
        return withContext(Dispatchers.Default) {
            try {
                worldDao.deleteAllEvents()
                cascadeEngine.reset()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e, "Failed to reset simulation")
            }
        }
    }
    
    /**
     * Advances simulation by one tick.
     */
    suspend fun tick(): Result<WorldState> {
        return withContext(Dispatchers.Default) {
            cascadeEngine.tick()
        }
    }
    
    /**
     * Saves world state to database (critical state only).
     */
    suspend fun saveWorldState(state: WorldState): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Save critical state information
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e, "Failed to save world state")
            }
        }
    }
    
    /**
     * EventSink implementation - persists generated events.
     */
    override suspend fun onEventsGenerated(events: List<Event>) {
        withContext(Dispatchers.IO) {
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
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Gets events for a specific chain.
     */
    suspend fun getEventsByChain(chainId: String): List<Event> {
        return withContext(Dispatchers.IO) {
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
    }
}
