package com.cascadesim.core.db.entity

// Re-export entities from common module for backward compatibility
// This file can be removed once all references are updated
@Deprecated("Use com.cascadesim.common.entity.CountryEntity instead", ReplaceWith("CountryEntity", "com.cascadesim.common.entity.CountryEntity"))
typealias CountryEntity = com.cascadesim.common.entity.CountryEntity

@Deprecated("Use com.cascadesim.common.entity.NpcEntity instead", ReplaceWith("NpcEntity", "com.cascadesim.common.entity.NpcEntity"))
typealias NpcEntity = com.cascadesim.common.entity.NpcEntity

@Deprecated("Use com.cascadesim.common.entity.EventEntity instead", ReplaceWith("EventEntity", "com.cascadesim.common.entity.EventEntity"))
typealias EventEntity = com.cascadesim.common.entity.EventEntity
