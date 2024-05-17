package com.paperline.news.data

import android.content.Context
import kotlinx.serialization.builtins.ListSerializer

class UserPreferences(
    context: Context,
) {
    private val prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun followedIds(): Set<String> {
        val stored = prefs.getStringSet(KEY_TOPICS, null) ?: return Topics.DEFAULT_IDS
        return stored.toSet().ifEmpty { Topics.DEFAULT_IDS }
    }

    fun setFollowedIds(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_TOPICS, HashSet(ids)).apply()
    }

    fun bookmarks(): List<Article> {
        val raw = prefs.getString(KEY_BOOKMARKS, null) ?: return emptyList()
        return runCatching {
            ArticleParser.json.decodeFromString(ListSerializer(Article.serializer()), raw)
        }.getOrDefault(emptyList())
    }

    fun setBookmarks(articles: List<Article>) {
        val raw = ArticleParser.json.encodeToString(ListSerializer(Article.serializer()), articles)
        prefs.edit().putString(KEY_BOOKMARKS, raw).apply()
    }

    private companion object {
        const val FILE = "paperline"
        const val KEY_TOPICS = "followed_topics"
        const val KEY_BOOKMARKS = "bookmarks"
    }
}
