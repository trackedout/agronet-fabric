# AuthApi

All URIs are relative to *http://localhost:3000/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**authLoginPost**](AuthApi.md#authLoginPost) | **POST** /auth/login | Login
[**authLogoutPost**](AuthApi.md#authLogoutPost) | **POST** /auth/logout | Logout
[**authRefreshTokensPost**](AuthApi.md#authRefreshTokensPost) | **POST** /auth/refresh-tokens | Refresh auth tokens
[**authRegisterPost**](AuthApi.md#authRegisterPost) | **POST** /auth/register | Register as user


<a id="authLoginPost"></a>
# **authLoginPost**
> AuthRegisterPost201Response authLoginPost(authLoginPostRequest)

Login

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = AuthApi()
val authLoginPostRequest : AuthLoginPostRequest =  // AuthLoginPostRequest | 
try {
    val result : AuthRegisterPost201Response = apiInstance.authLoginPost(authLoginPostRequest)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AuthApi#authLoginPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthApi#authLoginPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **authLoginPostRequest** | [**AuthLoginPostRequest**](AuthLoginPostRequest.md)|  |

### Return type

[**AuthRegisterPost201Response**](AuthRegisterPost201Response.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a id="authLogoutPost"></a>
# **authLogoutPost**
> authLogoutPost(authLogoutPostRequest)

Logout

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = AuthApi()
val authLogoutPostRequest : AuthLogoutPostRequest =  // AuthLogoutPostRequest | 
try {
    apiInstance.authLogoutPost(authLogoutPostRequest)
} catch (e: ClientException) {
    println("4xx response calling AuthApi#authLogoutPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthApi#authLogoutPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **authLogoutPostRequest** | [**AuthLogoutPostRequest**](AuthLogoutPostRequest.md)|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a id="authRefreshTokensPost"></a>
# **authRefreshTokensPost**
> UserWithTokens authRefreshTokensPost(authLogoutPostRequest)

Refresh auth tokens

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = AuthApi()
val authLogoutPostRequest : AuthLogoutPostRequest =  // AuthLogoutPostRequest | 
try {
    val result : UserWithTokens = apiInstance.authRefreshTokensPost(authLogoutPostRequest)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AuthApi#authRefreshTokensPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthApi#authRefreshTokensPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **authLogoutPostRequest** | [**AuthLogoutPostRequest**](AuthLogoutPostRequest.md)|  |

### Return type

[**UserWithTokens**](UserWithTokens.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a id="authRegisterPost"></a>
# **authRegisterPost**
> AuthRegisterPost201Response authRegisterPost(authRegisterPostRequest)

Register as user

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = AuthApi()
val authRegisterPostRequest : AuthRegisterPostRequest =  // AuthRegisterPostRequest | 
try {
    val result : AuthRegisterPost201Response = apiInstance.authRegisterPost(authRegisterPostRequest)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AuthApi#authRegisterPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthApi#authRegisterPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **authRegisterPostRequest** | [**AuthRegisterPostRequest**](AuthRegisterPostRequest.md)|  |

### Return type

[**AuthRegisterPost201Response**](AuthRegisterPost201Response.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

