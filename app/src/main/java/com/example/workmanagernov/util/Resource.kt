package com.example.workmanagernov.util

sealed class Resource<T>(data: T? = null, message: String? = null) {
    data class Success<T>(val data: T): Resource<T>(data)
    data class Error<T>(val message: String): Resource<T>(null, message)
}

