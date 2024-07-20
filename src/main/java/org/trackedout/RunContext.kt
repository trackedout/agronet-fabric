package org.trackedout

import com.google.common.collect.Maps
import java.util.UUID
import java.util.concurrent.ConcurrentMap

data object RunContext {
    var serverName = "unknown"
    // Map Player -> Game mode from claim (e.g. practice)
    val gameTags: ConcurrentMap<String, String> = Maps.newConcurrentMap()
    var runId = UUID.randomUUID().toString()
}
