package com.cascadesim.core.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an NPC (Non-Player Character) in the simulation.
 * Stores trait mappings and hidden bias scores for procedural behavior.
 */
@Entity(tableName = "npcs")
data class NpcEntity(
    @PrimaryKey
    val id: String,
    
    val traitMapJson: String,
    
    val hiddenBiasScore: Float
)
