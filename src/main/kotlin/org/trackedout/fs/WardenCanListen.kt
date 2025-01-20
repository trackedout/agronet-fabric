package org.trackedout.fs

import kotlinx.serialization.Serializable

@Serializable
data class WardenCanListen (
    var replace: Boolean,
    var values: List<String>
)
