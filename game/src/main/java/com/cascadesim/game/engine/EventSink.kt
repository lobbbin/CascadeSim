package com.cascadesim.game.engine

/**
 * Interface for receiving generated events from the CascadeEngine.
 * Allows the Repository to persist events after they are generated.
 */
interface EventSink {
    suspend fun onEventsGenerated(events: List<com.cascadesim.game.model.Event>)
}
