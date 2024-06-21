package com.example.main_sdk

sealed class StreamStatus {
    data class OnStart(val message: String): StreamStatus()
    data class OnInProgress(val message: String): StreamStatus()
    data class OnCompleted(val message: String): StreamStatus()
    data class OnError(val message: String): StreamStatus()
}