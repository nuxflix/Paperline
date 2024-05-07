package com.paperline.news.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class NewsApi(
    private val client: OkHttpClient,
) {
    suspend fun search(
        query: String?,
        date: String,
        sort: String,
        size: Int,
        offset: Int,
    ): List<Article> = withContext(Dispatchers.IO) {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(HOST)
            .addPathSegments("v1/search")
            .addQueryParameter("lang", "en")
            .addQueryParameter("date", date)
            .addQueryParameter("sort", sort)
            .addQueryParameter("size", size.toString())
            .addQueryParameter("offset", offset.toString())
            .apply {
                if (!query.isNullOrBlank()) addQueryParameter("q", query)
            }
            .build()
        get(url, ArticleParser::parseSearch)
    }

    suspend fun article(url: String): Article = withContext(Dispatchers.IO) {
        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host(HOST)
            .addPathSegments("v1/article")
            .addQueryParameter("url", url)
            .build()
        get(httpUrl, ArticleParser::parseArticle)
    }

    private fun <T> get(url: HttpUrl, parse: (String) -> T): T {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("User-Agent", USER_AGENT)
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val detail = runCatching {
                    ArticleParser.json.decodeFromString(ErrorDto.serializer(), body).detail
                }.getOrNull()
                val message = when (response.code) {
                    404 -> "This story is no longer in the news index."
                    else -> detail ?: "News service returned ${response.code}."
                }
                throw NewsException(message)
            }
            return parse(body)
        }
    }

    private companion object {
        const val HOST = "freenewsapi.ai"
        const val USER_AGENT = "Paperline/1.0 (Android news reader)"
    }
}

@Serializable
internal data class ErrorDto(
    val detail: String? = null,
)
