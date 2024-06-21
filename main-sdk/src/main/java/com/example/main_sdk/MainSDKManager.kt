package com.example.main_sdk

import android.util.Log
import com.example.common.ActionHandler
import com.example.common.Status
import com.example.common.StatusListener

class MainSDKManager {

    private val streamStatusListener = arrayListOf<StreamStatusListener>()
    private var downloadService: ActionHandler? = null
    private var uploadService: ActionHandler? = null

    fun initialize() {
        isDownloadServiceAvailable()
        isUploadServiceAvailable()
    }

    private fun isDownloadServiceAvailable(): Boolean {
        return try {
            val clazz = Class.forName("com.example.download.DownloadManager")
            downloadService = clazz.getConstructor().newInstance() as ActionHandler
            Log.d("SDKManager", "download module found: ${downloadService?.javaClass?.name}")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
//            throw ClassNotFoundException("Download service is not available")
        }
    }

    private fun isUploadServiceAvailable(): Boolean {
        return try {
            val clazz = Class.forName("com.example.upload.UploadManager")
            uploadService = clazz.getConstructor().newInstance() as ActionHandler
            Log.d("SDKManager", "upload module found: ${uploadService?.javaClass?.name}")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
//            throw ClassNotFoundException("Upload service is not available")
        }
    }

    private val statusListener = object : StatusListener {
        override fun onStatusChange(status: Status) {
            when (status) {
                is Status.OnStart -> {
                    Log.d("SDKManager", "OnStart")
                    streamStatusListener.forEach {
                        it.onStatusChange(StreamStatus.OnStart(status.message))
                    }
                }

                is Status.OnInProgress -> {
                    Log.d("SDKManager", "OnInProgress ${status.percent}%")
                    streamStatusListener.forEach {
                        it.onStatusChange(StreamStatus.OnInProgress("${status.message} ${status.percent}%"))
                    }
                }

                is Status.OnError -> {
                    Log.d("SDKManager", "OnError: ${status.message}")
                    streamStatusListener.forEach {
                        it.onStatusChange(StreamStatus.OnError(status.message))
                    }
                }

                is Status.OnCompleted -> {
                    Log.d("SDKManager", "OnCompleted")
                    streamStatusListener.forEach {
                        it.onStatusChange(StreamStatus.OnCompleted(status.message))
                    }
                }
            }
        }
    }

    fun addStatusListener(listener: StreamStatusListener) {
        if (streamStatusListener.contains(listener).not()) {
            streamStatusListener.add(listener)
        }
    }

    fun removeStatusListener(listener: StreamStatusListener) {
        streamStatusListener.remove(listener)
    }

    fun start(streamParams: StreamParams) {
        if (streamParams.type == ServiceTypes.DOWNLOAD) {
            downloadService?.let {
                it.addStatusListener(statusListener)
                it.start()
            } ?: run {
                statusListener.onStatusChange(Status.OnError("Download service is not available."))
            }
        } else if (streamParams.type == ServiceTypes.UPLOAD) {
            uploadService?.let {
                uploadService?.addStatusListener(statusListener)
                uploadService?.start()
            } ?: run {
                statusListener.onStatusChange(Status.OnError("Upload service is not available."))
            }
        }
    }

    fun cancel(streamParams: StreamParams) {
        if (streamParams.type == ServiceTypes.DOWNLOAD) {
            downloadService?.cancel() ?: run {
                statusListener.onStatusChange(Status.OnError("Download service is not available."))
            }
        } else if (streamParams.type == ServiceTypes.UPLOAD) {
            uploadService?.cancel() ?: run {
                statusListener.onStatusChange(Status.OnError("Upload service is not available."))
            }
        }
    }

    fun stop(streamParams: StreamParams) {
        if (streamParams.type == ServiceTypes.DOWNLOAD) {
            downloadService?.stop() ?: run {
                statusListener.onStatusChange(Status.OnError("Download service is not available."))
            }
        } else if (streamParams.type == ServiceTypes.UPLOAD) {
            uploadService?.stop() ?: run {
                statusListener.onStatusChange(Status.OnError("Upload service is not available."))
            }
        }
    }
}