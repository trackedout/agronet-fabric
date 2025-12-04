package org.trackedout

import org.trackedout.client.apis.EventsApi
import org.trackedout.client.models.Event

class EventsApiWithContext(
    private val api: EventsApi,
    private val server: String,
) {
    fun eventsPost(event: Event): Event {
        var metadata = mapOf(
            "run-id" to RunContext.runId,
        )

        event.player?.let { player ->
            metadata = metadata.plus("run-type" to RunContext.playerContext(player).runType())
        }

        // Merge in any existing metadata from the event, overwriting the defaults above if necessary
        metadata = metadata.plus(event.metadata ?: mapOf())

        return api.eventsPost(event.copy(server = server, metadata = metadata))
    }
}
