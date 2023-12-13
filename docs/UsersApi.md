# UsersApi

All URIs are relative to *http://localhost:3000/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**usersGet**](UsersApi.md#usersGet) | **GET** /users | Get all users
[**usersIdDelete**](UsersApi.md#usersIdDelete) | **DELETE** /users/{id} | Delete a user
[**usersIdGet**](UsersApi.md#usersIdGet) | **GET** /users/{id} | Get a user
[**usersIdPatch**](UsersApi.md#usersIdPatch) | **PATCH** /users/{id} | Update a user
[**usersPost**](UsersApi.md#usersPost) | **POST** /users | Create a user


<a id="usersGet"></a>
# **usersGet**
> UsersGet200Response usersGet(name, role, sortBy, projectBy, limit, page)

Get all users

Only admins can retrieve all users.

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = UsersApi()
val name : kotlin.String = name_example // kotlin.String | User name
val role : kotlin.String = role_example // kotlin.String | User role
val sortBy : kotlin.String = sortBy_example // kotlin.String | sort by query in the form of field:desc/asc (ex. name:asc)
val projectBy : kotlin.String = projectBy_example // kotlin.String | project by query in the form of field:hide/include (ex. name:hide)
val limit : kotlin.Int = 56 // kotlin.Int | Maximum number of users
val page : kotlin.Int = 56 // kotlin.Int | Page number
try {
    val result : UsersGet200Response = apiInstance.usersGet(name, role, sortBy, projectBy, limit, page)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling UsersApi#usersGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling UsersApi#usersGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **kotlin.String**| User name | [optional]
 **role** | **kotlin.String**| User role | [optional]
 **sortBy** | **kotlin.String**| sort by query in the form of field:desc/asc (ex. name:asc) | [optional]
 **projectBy** | **kotlin.String**| project by query in the form of field:hide/include (ex. name:hide) | [optional]
 **limit** | **kotlin.Int**| Maximum number of users | [optional]
 **page** | **kotlin.Int**| Page number | [optional] [default to 1]

### Return type

[**UsersGet200Response**](UsersGet200Response.md)

### Authorization


Configure bearerAuth:
    ApiClient.accessToken = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="usersIdDelete"></a>
# **usersIdDelete**
> usersIdDelete(id)

Delete a user

Logged in users can delete only themselves. Only admins can delete other users.

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = UsersApi()
val id : kotlin.String = id_example // kotlin.String | User id
try {
    apiInstance.usersIdDelete(id)
} catch (e: ClientException) {
    println("4xx response calling UsersApi#usersIdDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling UsersApi#usersIdDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| User id |

### Return type

null (empty response body)

### Authorization


Configure bearerAuth:
    ApiClient.accessToken = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="usersIdGet"></a>
# **usersIdGet**
> User usersIdGet(id)

Get a user

Logged in users can fetch only their own user information. Only admins can fetch other users.

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = UsersApi()
val id : kotlin.String = id_example // kotlin.String | User id
try {
    val result : User = apiInstance.usersIdGet(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling UsersApi#usersIdGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling UsersApi#usersIdGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| User id |

### Return type

[**User**](User.md)

### Authorization


Configure bearerAuth:
    ApiClient.accessToken = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="usersIdPatch"></a>
# **usersIdPatch**
> User usersIdPatch(id, usersIdPatchRequest)

Update a user

Logged in users can only update their own information. Only admins can update other users.

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = UsersApi()
val id : kotlin.String = id_example // kotlin.String | User id
val usersIdPatchRequest : UsersIdPatchRequest =  // UsersIdPatchRequest | 
try {
    val result : User = apiInstance.usersIdPatch(id, usersIdPatchRequest)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling UsersApi#usersIdPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling UsersApi#usersIdPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| User id |
 **usersIdPatchRequest** | [**UsersIdPatchRequest**](UsersIdPatchRequest.md)|  |

### Return type

[**User**](User.md)

### Authorization


Configure bearerAuth:
    ApiClient.accessToken = ""

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a id="usersPost"></a>
# **usersPost**
> User usersPost(usersPostRequest)

Create a user

Only admins can create other users.

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = UsersApi()
val usersPostRequest : UsersPostRequest =  // UsersPostRequest | 
try {
    val result : User = apiInstance.usersPost(usersPostRequest)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling UsersApi#usersPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling UsersApi#usersPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **usersPostRequest** | [**UsersPostRequest**](UsersPostRequest.md)|  |

### Return type

[**User**](User.md)

### Authorization


Configure bearerAuth:
    ApiClient.accessToken = ""

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

