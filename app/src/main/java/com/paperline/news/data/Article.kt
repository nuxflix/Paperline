package com.paperline.news.data

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val id: String,
    val url: String,
    val title: String,
    val description: String = "",
    val body: String = "",
    val publishedAt: String? = null,
    val publisher: String = "",
    val image: String? = null,
    val author: String? = null,
)

fun List<Article>.dedupeArticles(): List<Article> {
    val ids = HashSet<String>()
    val titles = HashSet<String>()
    val unique = ArrayList<Article>(size)
    for (article in this) {
        val titleKey = article.title.trim().lowercase()
        if (!ids.add(article.id)) continue
        if (!titles.add(titleKey)) continue
        unique += article
    }
    return unique
}
