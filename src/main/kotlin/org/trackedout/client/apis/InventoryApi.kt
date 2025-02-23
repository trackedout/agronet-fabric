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

package org.trackedout.client.apis

import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.HttpUrl

import org.trackedout.client.models.Card
import org.trackedout.client.models.Error
import org.trackedout.client.models.InventoryCardsGet200Response
import org.trackedout.client.models.Item
import org.trackedout.client.models.StorageItemsGet200Response

import com.squareup.moshi.Json

import org.trackedout.client.infrastructure.ApiClient
import org.trackedout.client.infrastructure.ApiResponse
import org.trackedout.client.infrastructure.ClientException
import org.trackedout.client.infrastructure.ClientError
import org.trackedout.client.infrastructure.ServerException
import org.trackedout.client.infrastructure.ServerError
import org.trackedout.client.infrastructure.MultiValueMap
import org.trackedout.client.infrastructure.PartConfig
import org.trackedout.client.infrastructure.RequestConfig
import org.trackedout.client.infrastructure.RequestMethod
import org.trackedout.client.infrastructure.ResponseType
import org.trackedout.client.infrastructure.Success
import org.trackedout.client.infrastructure.toMultiValue

class InventoryApi(basePath: kotlin.String = defaultBasePath, client: OkHttpClient = ApiClient.defaultClient) : ApiClient(basePath, client) {
    companion object {
        @JvmStatic
        val defaultBasePath: String by lazy {
            System.getProperties().getProperty(ApiClient.baseUrlKey, "http://localhost:3000/v1")
        }
    }

    /**
     * Add a card to a player&#39;s deck
     * Add a card to a player&#39;s deck from one of the Decked Out 2 instances or the lobby server.
     * @param card 
     * @return Card
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun inventoryAddCardPost(card: Card) : Card {
        val localVarResponse = inventoryAddCardPostWithHttpInfo(card = card)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as Card
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Add a card to a player&#39;s deck
     * Add a card to a player&#39;s deck from one of the Decked Out 2 instances or the lobby server.
     * @param card 
     * @return ApiResponse<Card?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun inventoryAddCardPostWithHttpInfo(card: Card) : ApiResponse<Card?> {
        val localVariableConfig = inventoryAddCardPostRequestConfig(card = card)

        return request<Card, Card>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation inventoryAddCardPost
     *
     * @param card 
     * @return RequestConfig
     */
    fun inventoryAddCardPostRequestConfig(card: Card) : RequestConfig<Card> {
        val localVariableBody = card
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/inventory/add-card",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Get all cards
     * Only admins can retrieve all cards.
     * @param name Card name (optional)
     * @param player Player (optional)
     * @param deckType Deck Type (optional)
     * @param deckId Deck ID (optional)
     * @param sortBy sort by query in the form of field:desc/asc (ex. name:asc) (optional)
     * @param projectBy project by query in the form of field:hide/include (ex. name:hide) (optional)
     * @param limit Maximum number of cards (optional)
     * @param page Page number (optional, default to 1)
     * @return InventoryCardsGet200Response
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun inventoryCardsGet(name: kotlin.String? = null, player: kotlin.String? = null, deckType: kotlin.String? = null, deckId: kotlin.String? = null, sortBy: kotlin.String? = null, projectBy: kotlin.String? = null, limit: kotlin.Int? = null, page: kotlin.Int? = 1) : InventoryCardsGet200Response {
        val localVarResponse = inventoryCardsGetWithHttpInfo(name = name, player = player, deckType = deckType, deckId = deckId, sortBy = sortBy, projectBy = projectBy, limit = limit, page = page)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as InventoryCardsGet200Response
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Get all cards
     * Only admins can retrieve all cards.
     * @param name Card name (optional)
     * @param player Player (optional)
     * @param deckType Deck Type (optional)
     * @param deckId Deck ID (optional)
     * @param sortBy sort by query in the form of field:desc/asc (ex. name:asc) (optional)
     * @param projectBy project by query in the form of field:hide/include (ex. name:hide) (optional)
     * @param limit Maximum number of cards (optional)
     * @param page Page number (optional, default to 1)
     * @return ApiResponse<InventoryCardsGet200Response?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun inventoryCardsGetWithHttpInfo(name: kotlin.String?, player: kotlin.String?, deckType: kotlin.String?, deckId: kotlin.String?, sortBy: kotlin.String?, projectBy: kotlin.String?, limit: kotlin.Int?, page: kotlin.Int?) : ApiResponse<InventoryCardsGet200Response?> {
        val localVariableConfig = inventoryCardsGetRequestConfig(name = name, player = player, deckType = deckType, deckId = deckId, sortBy = sortBy, projectBy = projectBy, limit = limit, page = page)

        return request<Unit, InventoryCardsGet200Response>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation inventoryCardsGet
     *
     * @param name Card name (optional)
     * @param player Player (optional)
     * @param deckType Deck Type (optional)
     * @param deckId Deck ID (optional)
     * @param sortBy sort by query in the form of field:desc/asc (ex. name:asc) (optional)
     * @param projectBy project by query in the form of field:hide/include (ex. name:hide) (optional)
     * @param limit Maximum number of cards (optional)
     * @param page Page number (optional, default to 1)
     * @return RequestConfig
     */
    fun inventoryCardsGetRequestConfig(name: kotlin.String?, player: kotlin.String?, deckType: kotlin.String?, deckId: kotlin.String?, sortBy: kotlin.String?, projectBy: kotlin.String?, limit: kotlin.Int?, page: kotlin.Int?) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf<kotlin.String, kotlin.collections.List<kotlin.String>>()
            .apply {
                if (name != null) {
                    put("name", listOf(name.toString()))
                }
                if (player != null) {
                    put("player", listOf(player.toString()))
                }
                if (deckType != null) {
                    put("deckType", listOf(deckType.toString()))
                }
                if (deckId != null) {
                    put("deckId", listOf(deckId.toString()))
                }
                if (sortBy != null) {
                    put("sortBy", listOf(sortBy.toString()))
                }
                if (projectBy != null) {
                    put("projectBy", listOf(projectBy.toString()))
                }
                if (limit != null) {
                    put("limit", listOf(limit.toString()))
                }
                if (page != null) {
                    put("page", listOf(page.toString()))
                }
            }
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/inventory/cards",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Delete a card
     * Remove a card from a player&#39;s deck. If multiple copies of this card exist, only one will be removed.
     * @param card 
     * @return void
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun inventoryDeleteCardPost(card: Card) : Unit {
        val localVarResponse = inventoryDeleteCardPostWithHttpInfo(card = card)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> Unit
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Delete a card
     * Remove a card from a player&#39;s deck. If multiple copies of this card exist, only one will be removed.
     * @param card 
     * @return ApiResponse<Unit?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Throws(IllegalStateException::class, IOException::class)
    fun inventoryDeleteCardPostWithHttpInfo(card: Card) : ApiResponse<Unit?> {
        val localVariableConfig = inventoryDeleteCardPostRequestConfig(card = card)

        return request<Card, Unit>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation inventoryDeleteCardPost
     *
     * @param card 
     * @return RequestConfig
     */
    fun inventoryDeleteCardPostRequestConfig(card: Card) : RequestConfig<Card> {
        val localVariableBody = card
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/inventory/delete-card",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Overwrites the player&#39;s deck with the supplied list of cards
     * Remove all existing cards and create a new deck with the list of cards provided.
     * @param requestBody 
     * @param player Player (optional)
     * @param server Server (optional)
     * @param deckId Deck ID (optional)
     * @return void
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun inventoryOverwritePlayerDeckPut(requestBody: kotlin.collections.List<kotlin.String>, player: kotlin.String? = null, server: kotlin.String? = null, deckId: kotlin.String? = null) : Unit {
        val localVarResponse = inventoryOverwritePlayerDeckPutWithHttpInfo(requestBody = requestBody, player = player, server = server, deckId = deckId)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> Unit
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Overwrites the player&#39;s deck with the supplied list of cards
     * Remove all existing cards and create a new deck with the list of cards provided.
     * @param requestBody 
     * @param player Player (optional)
     * @param server Server (optional)
     * @param deckId Deck ID (optional)
     * @return ApiResponse<Unit?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Throws(IllegalStateException::class, IOException::class)
    fun inventoryOverwritePlayerDeckPutWithHttpInfo(requestBody: kotlin.collections.List<kotlin.String>, player: kotlin.String?, server: kotlin.String?, deckId: kotlin.String?) : ApiResponse<Unit?> {
        val localVariableConfig = inventoryOverwritePlayerDeckPutRequestConfig(requestBody = requestBody, player = player, server = server, deckId = deckId)

        return request<kotlin.collections.List<kotlin.String>, Unit>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation inventoryOverwritePlayerDeckPut
     *
     * @param requestBody 
     * @param player Player (optional)
     * @param server Server (optional)
     * @param deckId Deck ID (optional)
     * @return RequestConfig
     */
    fun inventoryOverwritePlayerDeckPutRequestConfig(requestBody: kotlin.collections.List<kotlin.String>, player: kotlin.String?, server: kotlin.String?, deckId: kotlin.String?) : RequestConfig<kotlin.collections.List<kotlin.String>> {
        val localVariableBody = requestBody
        val localVariableQuery: MultiValueMap = mutableMapOf<kotlin.String, kotlin.collections.List<kotlin.String>>()
            .apply {
                if (player != null) {
                    put("player", listOf(player.toString()))
                }
                if (server != null) {
                    put("server", listOf(server.toString()))
                }
                if (deckId != null) {
                    put("deckId", listOf(deckId.toString()))
                }
            }
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.PUT,
            path = "/inventory/overwrite-player-deck",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Add an item to a player&#39;s deck
     * Add an item to a player&#39;s deck from one of the Decked Out 2 instances or the lobby server.
     * @param item 
     * @return Item
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun storageAddItemPost(item: Item) : Item {
        val localVarResponse = storageAddItemPostWithHttpInfo(item = item)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as Item
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Add an item to a player&#39;s deck
     * Add an item to a player&#39;s deck from one of the Decked Out 2 instances or the lobby server.
     * @param item 
     * @return ApiResponse<Item?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun storageAddItemPostWithHttpInfo(item: Item) : ApiResponse<Item?> {
        val localVariableConfig = storageAddItemPostRequestConfig(item = item)

        return request<Item, Item>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation storageAddItemPost
     *
     * @param item 
     * @return RequestConfig
     */
    fun storageAddItemPostRequestConfig(item: Item) : RequestConfig<Item> {
        val localVariableBody = item
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/storage/add-item",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Delete an item
     * Remove an item from a player&#39;s deck. If multiple copies of this item exist, only one will be removed.
     * @param item 
     * @return void
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun storageDeleteItemPost(item: Item) : Unit {
        val localVarResponse = storageDeleteItemPostWithHttpInfo(item = item)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> Unit
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Delete an item
     * Remove an item from a player&#39;s deck. If multiple copies of this item exist, only one will be removed.
     * @param item 
     * @return ApiResponse<Unit?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Throws(IllegalStateException::class, IOException::class)
    fun storageDeleteItemPostWithHttpInfo(item: Item) : ApiResponse<Unit?> {
        val localVariableConfig = storageDeleteItemPostRequestConfig(item = item)

        return request<Item, Unit>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation storageDeleteItemPost
     *
     * @param item 
     * @return RequestConfig
     */
    fun storageDeleteItemPostRequestConfig(item: Item) : RequestConfig<Item> {
        val localVariableBody = item
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/storage/delete-item",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Get all items
     * Only admins can retrieve all items.
     * @param name Item name (optional)
     * @param player Player (optional)
     * @param deckType Deck Type (optional)
     * @param deckId Deck ID (optional)
     * @param sortBy sort by query in the form of field:desc/asc (ex. name:asc) (optional)
     * @param projectBy project by query in the form of field:hide/include (ex. name:hide) (optional)
     * @param limit Maximum number of items (optional)
     * @param page Page number (optional, default to 1)
     * @return StorageItemsGet200Response
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun storageItemsGet(name: kotlin.String? = null, player: kotlin.String? = null, deckType: kotlin.String? = null, deckId: kotlin.String? = null, sortBy: kotlin.String? = null, projectBy: kotlin.String? = null, limit: kotlin.Int? = null, page: kotlin.Int? = 1) : StorageItemsGet200Response {
        val localVarResponse = storageItemsGetWithHttpInfo(name = name, player = player, deckType = deckType, deckId = deckId, sortBy = sortBy, projectBy = projectBy, limit = limit, page = page)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as StorageItemsGet200Response
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Get all items
     * Only admins can retrieve all items.
     * @param name Item name (optional)
     * @param player Player (optional)
     * @param deckType Deck Type (optional)
     * @param deckId Deck ID (optional)
     * @param sortBy sort by query in the form of field:desc/asc (ex. name:asc) (optional)
     * @param projectBy project by query in the form of field:hide/include (ex. name:hide) (optional)
     * @param limit Maximum number of items (optional)
     * @param page Page number (optional, default to 1)
     * @return ApiResponse<StorageItemsGet200Response?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun storageItemsGetWithHttpInfo(name: kotlin.String?, player: kotlin.String?, deckType: kotlin.String?, deckId: kotlin.String?, sortBy: kotlin.String?, projectBy: kotlin.String?, limit: kotlin.Int?, page: kotlin.Int?) : ApiResponse<StorageItemsGet200Response?> {
        val localVariableConfig = storageItemsGetRequestConfig(name = name, player = player, deckType = deckType, deckId = deckId, sortBy = sortBy, projectBy = projectBy, limit = limit, page = page)

        return request<Unit, StorageItemsGet200Response>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation storageItemsGet
     *
     * @param name Item name (optional)
     * @param player Player (optional)
     * @param deckType Deck Type (optional)
     * @param deckId Deck ID (optional)
     * @param sortBy sort by query in the form of field:desc/asc (ex. name:asc) (optional)
     * @param projectBy project by query in the form of field:hide/include (ex. name:hide) (optional)
     * @param limit Maximum number of items (optional)
     * @param page Page number (optional, default to 1)
     * @return RequestConfig
     */
    fun storageItemsGetRequestConfig(name: kotlin.String?, player: kotlin.String?, deckType: kotlin.String?, deckId: kotlin.String?, sortBy: kotlin.String?, projectBy: kotlin.String?, limit: kotlin.Int?, page: kotlin.Int?) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf<kotlin.String, kotlin.collections.List<kotlin.String>>()
            .apply {
                if (name != null) {
                    put("name", listOf(name.toString()))
                }
                if (player != null) {
                    put("player", listOf(player.toString()))
                }
                if (deckType != null) {
                    put("deckType", listOf(deckType.toString()))
                }
                if (deckId != null) {
                    put("deckId", listOf(deckId.toString()))
                }
                if (sortBy != null) {
                    put("sortBy", listOf(sortBy.toString()))
                }
                if (projectBy != null) {
                    put("projectBy", listOf(projectBy.toString()))
                }
                if (limit != null) {
                    put("limit", listOf(limit.toString()))
                }
                if (page != null) {
                    put("page", listOf(page.toString()))
                }
            }
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/storage/items",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Overwrites the player&#39;s deck with the supplied list of items
     * Remove all existing items and create a new deck with the list of items provided.
     * @param requestBody 
     * @param player Player (optional)
     * @param server Server (optional)
     * @param deckId Deck ID (optional)
     * @return void
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun storageOverwritePlayerDeckPut(requestBody: kotlin.collections.List<kotlin.String>, player: kotlin.String? = null, server: kotlin.String? = null, deckId: kotlin.String? = null) : Unit {
        val localVarResponse = storageOverwritePlayerDeckPutWithHttpInfo(requestBody = requestBody, player = player, server = server, deckId = deckId)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> Unit
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Overwrites the player&#39;s deck with the supplied list of items
     * Remove all existing items and create a new deck with the list of items provided.
     * @param requestBody 
     * @param player Player (optional)
     * @param server Server (optional)
     * @param deckId Deck ID (optional)
     * @return ApiResponse<Unit?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Throws(IllegalStateException::class, IOException::class)
    fun storageOverwritePlayerDeckPutWithHttpInfo(requestBody: kotlin.collections.List<kotlin.String>, player: kotlin.String?, server: kotlin.String?, deckId: kotlin.String?) : ApiResponse<Unit?> {
        val localVariableConfig = storageOverwritePlayerDeckPutRequestConfig(requestBody = requestBody, player = player, server = server, deckId = deckId)

        return request<kotlin.collections.List<kotlin.String>, Unit>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation storageOverwritePlayerDeckPut
     *
     * @param requestBody 
     * @param player Player (optional)
     * @param server Server (optional)
     * @param deckId Deck ID (optional)
     * @return RequestConfig
     */
    fun storageOverwritePlayerDeckPutRequestConfig(requestBody: kotlin.collections.List<kotlin.String>, player: kotlin.String?, server: kotlin.String?, deckId: kotlin.String?) : RequestConfig<kotlin.collections.List<kotlin.String>> {
        val localVariableBody = requestBody
        val localVariableQuery: MultiValueMap = mutableMapOf<kotlin.String, kotlin.collections.List<kotlin.String>>()
            .apply {
                if (player != null) {
                    put("player", listOf(player.toString()))
                }
                if (server != null) {
                    put("server", listOf(server.toString()))
                }
                if (deckId != null) {
                    put("deckId", listOf(deckId.toString()))
                }
            }
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.PUT,
            path = "/storage/overwrite-player-deck",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }


    private fun encodeURIComponent(uriComponent: kotlin.String): kotlin.String =
        HttpUrl.Builder().scheme("http").host("localhost").addPathSegment(uriComponent).build().encodedPathSegments[0]
}
