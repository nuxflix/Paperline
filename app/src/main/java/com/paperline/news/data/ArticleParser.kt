package com.paperline.news.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object ArticleParser {
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parseSearch(body: String): List<Article> {
        val response = json.decodeFromString(SearchResponse.serializer(), body)
        return response.results.mapNotNull { it.toArticle() }
    }

    fun parseArticle(body: String): Article {
        val dto = json.decodeFromString(ArticleDto.serializer(), body)
        return dto.toArticle() ?: throw NewsException("The article was missing a title.")
    }
}

@Serializable
internal data class SearchResponse(
    val results: List<ArticleDto> = emptyList(),
)

@Serializable
internal data class ArticleDto(
    val id: String = "",
    val url: String = "",
    val title: String = "",
    val description: String? = null,
    val text: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    val host: String? = null,
    val sitename: String? = null,
    val author: String? = null,
    val image: String? = null,
)

private fun ArticleDto.toArticle(): Article? {
    val cleanTitle = unescape(title).replace(Regex("\\s+"), " ").trim()
    if (id.isBlank() || url.isBlank() || cleanTitle.isBlank()) return null
    val publisher = sitename?.let(::unescape)?.trim()?.takeIf { it.isNotEmpty() }
        ?: host?.removePrefix("www.")?.takeIf { it.isNotBlank() }
        ?: "Unknown"
    return Article(
        id = id,
        url = url,
        title = cleanTitle,
        description = unescape(description.orEmpty()).trim(),
        body = unescape(text.orEmpty()).trim(),
        publishedAt = publishedAt?.takeIf { it.isNotBlank() },
        publisher = publisher,
        image = image?.takeIf { it.startsWith("http://") || it.startsWith("https://") },
        author = author?.let(::unescape)?.trim()?.takeIf { it.isNotEmpty() },
    )
}

private fun unescape(value: String): String = value
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("&apos;", "'")
    .replace("&amp;", "&")
