package com.example.basketballhelp.data.network.util

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class ApiException(message: String) : Exception(message)

fun Throwable.toApiException(): Exception = when (this) {
    is ApiException -> this
    is HttpException -> ApiException(errorBody().extractErrorMessage() ?: message())
    is IOException -> ApiException("Network request failed. Check connection and backend URL.")
    else -> ApiException(message ?: "Unexpected error")
}

private fun HttpException.errorBody(): ResponseBody? = response()?.errorBody()

private fun ResponseBody?.extractErrorMessage(): String? {
    val text = runCatching { this?.string() }.getOrNull() ?: return null
    return runCatching { JSONObject(text).optString("error").takeIf { it.isNotBlank() } }.getOrNull()
}
