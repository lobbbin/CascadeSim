package com.cascadesim.core.repository

import com.cascadesim.core.db.dao.WorldDao
import com.cascadesim.core.db.entity.CountryEntity
import com.cascadesim.core.db.entity.NpcEntity
import com.cascadesim.core.util.Result
import com.cascadesim.game.engine.CascadeEngine
import com.cascadesim.game.model.Decision
import com.cascadesim.game.model.Event
import com.cascadesim.game.model.WorldState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository bridging database and game engine.
 * Exposes Flow<WorldState> for UI observation and handles data operations.
 */
@Singleton
class WorldRepository @Inject constructor(
    private val worldDao: WorldDao,
    private val cascadeEngine: CascadeEngine
) {
    
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
     * Exposes the current world state as a Flow.
     */
    fun observeWorldState(): Flow<WorldState> = flow {
        while (true) {
            emit(cascadeEngine.getState())
            kotlinx.coroutines.delay(100)
        }
    }
    
    /**
     * Gets a country by ID.
     */
    suspend fun getCountry(id: String): CountryEntity? {
        return worldDao.getCountryById(id)
    }
    
    /**
     * Gets an NPC by ID.
     */
    suspend fun getNpc(id: String): NpcEntity? {
        return worldDao.getNpcById(id)
    }
    
    /**
     * Saves a country to the database.
     */
    suspend fun saveCountry(country: CountryEntity): Result<Unit> {
        return try {
            worldDao.insertCountry(country)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save country")
        }
    }
    
    /**
     * Saves an NPC to the database.
     */
    suspend fun saveNpc(npc: NpcEntity): Result<Unit> {
        return try {
            worldDao.insertNpc(npc)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save NPC")
        }
    }
    
    /**
     * Processes a decision through the cascade engine.
     */
    suspend fun processDecision(decision: Decision): List<Event> {
        return cascadeEngine.processDecision(decision)
    }
    
    /**
     * Initializes the simulation.
     */
    suspend fun initialize(): Result<Unit> {
        return cascadeEngine.initialize()
    }
    
    /**
     * Resets the simulation.
     */
    suspend fun reset(): Result<Unit> {
        return cascadeEngine.reset()
    }
    
    /**
     * Advances simulation by one tick.
     */
    suspend fun tick(): Result<WorldState> {
        return cascadeEngine.tick()
    }
}
