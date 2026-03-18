// FIX: PHASE 4 - TypeConverter methods must be static for Room to work properly
// Changed from instance methods to static methods in companion object

package com.cascadesim.core.db.converters

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room TypeConverters for JSON serialization/deserialization.
 * Handles conversion between complex types and JSON strings.
 * 
 * PHASE 4: Fixed - methods are now static for Room compatibility
 */
class JsonConverters {

    companion object {
        private val gson = Gson()

        /**
         * Converts a Map<String, Any> to JSON string.
         */
        @JvmStatic
        @androidx.room.TypeConverter
        fun fromMapToString(value: Map<String, Any>): String {
            return gson.toJson(value)
        }

        /**
         * Converts JSON string to Map<String, Any>.
         */
        @JvmStatic
        @androidx.room.TypeConverter
        fun fromStringToMap(json: String): Map<String, Any> {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            return gson.fromJson(json, type) ?: emptyMap()
        }

        /**
         * Converts a List<String> to JSON string.
         */
        @JvmStatic
        @androidx.room.TypeConverter
        fun fromStringListToString(value: List<String>): String {
            return gson.toJson(value)
        }

        /**
         * Converts JSON string to List<String>.
         */
        @JvmStatic
        @androidx.room.TypeConverter
        fun fromStringToStringList(json: String): List<String> {
            val type = object : TypeToken<List<String>>() {}.type
            return gson.fromJson(json, type) ?: emptyList()
        }

        /**
         * Converts Any object to JSON string (for resources).
         */
        @JvmStatic
        @androidx.room.TypeConverter
        fun fromAnyToString(value: Any?): String {
            return gson.toJson(value)
        }

        /**
         * Converts JSON string to Any object (for resources).
         */
        @JvmStatic
        @androidx.room.TypeConverter
        fun fromStringToAny(json: String): Any? {
            return gson.fromJson(json, Any::class.java)
        }
    }
}
