package com.cascadesim.core.db.dao

import androidx.room.*
import com.cascadesim.core.db.entity.CountryEntity
import com.cascadesim.core.db.entity.NpcEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for world-related entities.
 * Provides suspend functions and Flow streams for reactive UI updates.
 */
@Dao
interface WorldDao {
    
    // Country operations
    @Query("SELECT * FROM countries WHERE id = :id")
    suspend fun getCountryById(id: String): CountryEntity?
    
    @Query("SELECT * FROM countries")
    fun getAllCountries(): Flow<List<CountryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountry(country: CountryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountries(countries: List<CountryEntity>)
    
    @Update
    suspend fun updateCountry(country: CountryEntity)
    
    @Delete
    suspend fun deleteCountry(country: CountryEntity)
    
    // NPC operations
    @Query("SELECT * FROM npcs WHERE id = :id")
    suspend fun getNpcById(id: String): NpcEntity?
    
    @Query("SELECT * FROM npcs")
    fun getAllNpcs(): Flow<List<NpcEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNpc(npc: NpcEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNpcs(npcs: List<NpcEntity>)
    
    @Update
    suspend fun updateNpc(npc: NpcEntity)
    
    @Delete
    suspend fun deleteNpc(npc: NpcEntity)
    
    // Utility queries
    @Query("SELECT * FROM npcs WHERE hiddenBiasScore > :threshold")
    fun getNpcsByBiasThreshold(threshold: Float): Flow<List<NpcEntity>>
    
    @Query("SELECT * FROM countries WHERE stability < :threshold")
    fun getUnstableCountries(threshold: Float): Flow<List<CountryEntity>>
}
