# AuthApi

All URIs are relative to *http://localhost:3000/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**authForgotPasswordPost**](AuthApi.md#authForgotPasswordPost) | **POST** /auth/forgot-password | Forgot password
[**authLoginPost**](AuthApi.md#authLoginPost) | **POST** /auth/login | Login
[**authLogoutPost**](AuthApi.md#authLogoutPost) | **POST** /auth/logout | Logout
[**authRefreshTokensPost**](AuthApi.md#authRefreshTokensPost) | **POST** /auth/refresh-tokens | Refresh auth tokens
[**authRegisterPost**](AuthApi.md#authRegisterPost) | **POST** /auth/register | Register as user
[**authResetPasswordPost**](AuthApi.md#authResetPasswordPost) | **POST** /auth/reset-password | Reset password
[**authSendVerificationEmailPost**](AuthApi.md#authSendVerificationEmailPost) | **POST** /auth/send-verification-email | Send verification email
[**authVerifyEmailPost**](AuthApi.md#authVerifyEmailPost) | **POST** /auth/verify-email | verify email


<a id="authForgotPasswordPost"></a>
# **authForgotPasswordPost**
> authForgotPasswordPost(authForgotPasswordPostRequest)

Forgot password

An email will be sent to reset password.

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = AuthApi()
val authForgotPasswordPostRequest : AuthForgotPasswordPostRequest =  // AuthForgotPasswordPostRequest | 
try {
    apiInstance.authForgotPasswordPost(authForgotPasswordPostRequest)
} catch (e: ClientException) {
    println("4xx response calling AuthApi#authForgotPasswordPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthApi#authForgotPasswordPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **authForgotPasswordPostRequest** | [**AuthForgotPasswordPostRequest**](AuthForgotPasswordPostRequest.md)|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

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

<a id="authResetPasswordPost"></a>
# **authResetPasswordPost**
> authResetPasswordPost(token, authResetPasswordPostRequest)

Reset password

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = AuthApi()
val token : kotlin.String = token_example // kotlin.String | The reset password token
val authResetPasswordPostRequest : AuthResetPasswordPostRequest =  // AuthResetPasswordPostRequest | 
try {
    apiInstance.authResetPasswordPost(token, authResetPasswordPostRequest)
} catch (e: ClientException) {
    println("4xx response calling AuthApi#authResetPasswordPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthApi#authResetPasswordPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **token** | **kotlin.String**| The reset password token |
 **authResetPasswordPostRequest** | [**AuthResetPasswordPostRequest**](AuthResetPasswordPostRequest.md)|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a id="authSendVerificationEmailPost"></a>
# **authSendVerificationEmailPost**
> authSendVerificationEmailPost()

Send verification email

An email will be sent to verify email.

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = AuthApi()
try {
    apiInstance.authSendVerificationEmailPost()
} catch (e: ClientException) {
    println("4xx response calling AuthApi#authSendVerificationEmailPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthApi#authSendVerificationEmailPost")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization


Configure bearerAuth:
    ApiClient.accessToken = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="authVerifyEmailPost"></a>
# **authVerifyEmailPost**
> authVerifyEmailPost(token)

verify email

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = AuthApi()
val token : kotlin.String = token_example // kotlin.String | The verify email token
try {
    apiInstance.authVerifyEmailPost(token)
} catch (e: ClientException) {
    println("4xx response calling AuthApi#authVerifyEmailPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthApi#authVerifyEmailPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **token** | **kotlin.String**| The verify email token |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

