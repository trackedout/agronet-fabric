package org.trackedout

import org.trackedout.client.apis.EventsApi
import org.trackedout.client.models.Event

class EventsApiWithContext(
    private val api: EventsApi,
    private val server: String,
    private val runContext: RunContext,
) {
    fun eventsPost(event: Event): Event {
        var metadata = event.metadata ?: mapOf()
        metadata = metadata.plus("run-id" to runContext.runId)

        return api.eventsPost(event.copy(server = server, metadata = metadata))
    }
}
