package com.paperline.news.data

import java.io.IOException

class NewsException(message: String) : Exception(message)

fun Throwable.toNewsMessage(): String = when (this) {
    is NewsException -> message?.takeIf { it.isNotBlank() } ?: "News request failed."
    is IOException -> "Couldn't reach the news service."
    else -> message?.takeIf { it.isNotBlank() } ?: "Something went wrong."
}
