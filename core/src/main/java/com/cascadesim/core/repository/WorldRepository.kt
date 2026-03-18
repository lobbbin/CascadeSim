package com.cascadesim.core.repository

import com.cascadesim.common.entity.CountryEntity
import com.cascadesim.common.entity.EventEntity
import com.cascadesim.common.entity.NpcEntity
import com.cascadesim.common.util.Result
import com.cascadesim.core.db.dao.WorldDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for world data access.
 * Handles database operations only - no game engine logic.
 */
@Singleton
class WorldRepository @Inject constructor(
    private val worldDao: WorldDao
) {

    fun observeCountries(): Flow<List<CountryEntity>> = worldDao.getAllCountries()

    fun observeNpcs(): Flow<List<NpcEntity>> = worldDao.getAllNpcs()

    fun observeUnstableCountries(threshold: Float = 0.5f): Flow<List<CountryEntity>> =
        worldDao.getUnstableCountries(threshold)

    fun observeBiasedNpcs(threshold: Float = 0.5f): Flow<List<NpcEntity>> =
        worldDao.getNpcsByBiasThreshold(threshold)

    fun observeRecentEvents(limit: Int = 50): Flow<List<EventEntity>> =
        worldDao.getRecentEvents(limit)

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

    suspend fun reset(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            worldDao.deleteAllEvents()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to reset repository")
        }
    }

    suspend fun saveEvents(events: List<EventEntity>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            worldDao.insertEvents(events)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save events")
        }
    }
}
