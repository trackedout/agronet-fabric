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
 * @param email 
 * @param password 
 */


data class AuthLoginPostRequest (

    @Json(name = "email")
    val email: kotlin.String,

    @Json(name = "password")
    val password: kotlin.String

)

