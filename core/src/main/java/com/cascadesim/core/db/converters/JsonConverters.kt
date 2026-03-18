package com.cascadesim.core.db.converters

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

/**
 * Room TypeConverters for JSON serialization/deserialization.
 * Handles conversion between complex types and JSON strings.
 */
class JsonConverters @Inject constructor() {
    
    private val gson = Gson()
    
    /**
     * Converts a Map<String, Any> to JSON string.
     */
    @androidx.room.TypeConverter
    fun fromMapToString(value: Map<String, Any>): String {
        return gson.toJson(value)
    }
    
    /**
     * Converts JSON string to Map<String, Any>.
     */
    @androidx.room.TypeConverter
    fun fromStringToMap(json: String): Map<String, Any> {
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
    
    /**
     * Converts a List<String> to JSON string.
     */
    @androidx.room.TypeConverter
    fun fromStringListToString(value: List<String>): String {
        return gson.toJson(value)
    }
    
    /**
     * Converts JSON string to List<String>.
     */
    @androidx.room.TypeConverter
    fun fromStringToStringList(json: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    /**
     * Converts Any object to JSON string (for resources).
     */
    @androidx.room.TypeConverter
    fun fromAnyToString(value: Any?): String {
        return gson.toJson(value)
    }
    
    /**
     * Converts JSON string to Any object (for resources).
     */
    @androidx.room.TypeConverter
    fun fromStringToAny(json: String): Any? {
        return gson.fromJson(json, Any::class.java)
    }
}
