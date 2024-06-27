package org.trackedout

import org.trackedout.client.apis.EventsApi
import org.trackedout.client.models.Event

class EventsApiWithContext(
    private val api: EventsApi,
    private val server: String,
    private val runId: String,
) {
    fun eventsPost(event: Event): Event {
        return api.eventsPost(event.copy(server = server, runId = runId))
    }
}
