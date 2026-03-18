package com.cascadesim.game.engine

import com.cascadesim.common.model.Event

/**
 * Interface for receiving generated events from the CascadeEngine.
 * Allows the Repository to persist events after they are generated.
 */
interface EventSink {
    suspend fun onEventsGenerated(events: List<Event>)
}
