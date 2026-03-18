package com.cascadesim.common.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing an NPC (Non-Player Character) in the simulation.
 * Stores trait mappings and hidden bias scores for procedural behavior.
 */
@Entity(
    tableName = "npcs",
    foreignKeys = [
        ForeignKey(
            entity = CountryEntity::class,
            parentColumns = ["id"],
            childColumns = ["countryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("countryId")]
)
data class NpcEntity(
    @PrimaryKey
    val id: String,

    val name: String,

    val traitsJson: String,

    val hiddenBiasScore: Float,

    val countryId: String?
)
