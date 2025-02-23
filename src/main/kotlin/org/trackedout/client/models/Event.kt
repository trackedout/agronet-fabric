/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package org.trackedout.client.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param id 
 * @param name 
 * @param player 
 * @param count 
 * @param x 
 * @param y 
 * @param z 
 * @param server 
 * @param sourceIP 
 * @param metadata 
 */


data class Event (

    @Json(name = "id")
    val id: kotlin.String? = null,

    @Json(name = "name")
    val name: kotlin.String? = null,

    @Json(name = "player")
    val player: kotlin.String? = null,

    @Json(name = "count")
    val count: kotlin.Int? = null,

    @Json(name = "x")
    val x: kotlin.Double? = null,

    @Json(name = "y")
    val y: kotlin.Double? = null,

    @Json(name = "z")
    val z: kotlin.Double? = null,

    @Json(name = "server")
    val server: kotlin.String? = null,

    @Json(name = "sourceIP")
    val sourceIP: kotlin.String? = null,

    @Json(name = "metadata")
    val metadata: kotlin.collections.Map<kotlin.String, kotlin.String>? = null

)

