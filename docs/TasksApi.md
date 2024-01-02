# TasksApi

All URIs are relative to *http://localhost:3000/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**tasksGet**](TasksApi.md#tasksGet) | **GET** /tasks | Get all tasks
[**tasksIdGet**](TasksApi.md#tasksIdGet) | **GET** /tasks/{id} | Get a task
[**tasksIdPatch**](TasksApi.md#tasksIdPatch) | **PATCH** /tasks/{id} | Update a task
[**tasksPost**](TasksApi.md#tasksPost) | **POST** /tasks | Create a task


<a id="tasksGet"></a>
# **tasksGet**
> TasksGet200Response tasksGet(server, type, state, sortBy, projectBy, limit, page)

Get all tasks

Retrieve tasks, typically filtered for a specific server for it to action.

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = TasksApi()
val server : kotlin.String = server_example // kotlin.String | Server name
val type : kotlin.String = type_example // kotlin.String | Task type
val state : kotlin.String = state_example // kotlin.String | Task state
val sortBy : kotlin.String = sortBy_example // kotlin.String | sort by query in the form of field:desc/asc (ex. name:asc)
val projectBy : kotlin.String = projectBy_example // kotlin.String | project by query in the form of field:hide/include (ex. name:hide)
val limit : kotlin.Int = 56 // kotlin.Int | Maximum number of tasks
val page : kotlin.Int = 56 // kotlin.Int | Page number
try {
    val result : TasksGet200Response = apiInstance.tasksGet(server, type, state, sortBy, projectBy, limit, page)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TasksApi#tasksGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TasksApi#tasksGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **server** | **kotlin.String**| Server name | [optional]
 **type** | **kotlin.String**| Task type | [optional]
 **state** | **kotlin.String**| Task state | [optional]
 **sortBy** | **kotlin.String**| sort by query in the form of field:desc/asc (ex. name:asc) | [optional]
 **projectBy** | **kotlin.String**| project by query in the form of field:hide/include (ex. name:hide) | [optional]
 **limit** | **kotlin.Int**| Maximum number of tasks | [optional]
 **page** | **kotlin.Int**| Page number | [optional] [default to 1]

### Return type

[**TasksGet200Response**](TasksGet200Response.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="tasksIdGet"></a>
# **tasksIdGet**
> Task tasksIdGet(id)

Get a task

Get a task by ID

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = TasksApi()
val id : kotlin.String = id_example // kotlin.String | Task ID
try {
    val result : Task = apiInstance.tasksIdGet(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TasksApi#tasksIdGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TasksApi#tasksIdGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Task ID |

### Return type

[**Task**](Task.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="tasksIdPatch"></a>
# **tasksIdPatch**
> Task tasksIdPatch(id, tasksIdPatchRequest)

Update a task

Update a task&#39;s state to  one of [ \&quot;SCHEDULED\&quot;, \&quot;IN_PROGRESS\&quot;, \&quot;SUCCEEDED\&quot;, \&quot;FAILED\&quot; ]

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = TasksApi()
val id : kotlin.String = id_example // kotlin.String | Task id
val tasksIdPatchRequest : TasksIdPatchRequest =  // TasksIdPatchRequest | 
try {
    val result : Task = apiInstance.tasksIdPatch(id, tasksIdPatchRequest)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TasksApi#tasksIdPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TasksApi#tasksIdPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Task id |
 **tasksIdPatchRequest** | [**TasksIdPatchRequest**](TasksIdPatchRequest.md)|  |

### Return type

[**Task**](Task.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a id="tasksPost"></a>
# **tasksPost**
> Task tasksPost(task)

Create a task

Schedule a remote task for one of the Decked Out 2 instances.

### Example
```kotlin
// Import classes:
//import org.trackedout.client.infrastructure.*
//import org.trackedout.client.models.*

val apiInstance = TasksApi()
val task : Task =  // Task | 
try {
    val result : Task = apiInstance.tasksPost(task)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TasksApi#tasksPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TasksApi#tasksPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **task** | [**Task**](Task.md)|  |

### Return type

[**Task**](Task.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

