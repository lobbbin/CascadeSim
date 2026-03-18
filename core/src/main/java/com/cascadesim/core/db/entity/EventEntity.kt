package com.cascadesim.core.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a simulation event.
 * Stores event data for persistence and historical tracking.
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: String,
    
    val description: String,
    
    val severity: String,
    
    val chainId: String,
    
    val sourceDecisionId: String?,
    
    val timestamp: Long,
    
    val affectedEntityIdsJson: String
)
