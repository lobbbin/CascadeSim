package com.cascadesim.core.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a country in the simulation.
 * Stores stability metrics and resource data as JSON for flexibility.
 */
@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey
    val id: String,
    
    val name: String,
    
    val stability: Float,
    
    val resourcesJson: String
)
