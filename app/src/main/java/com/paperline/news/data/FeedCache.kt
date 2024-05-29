package com.paperline.news.data

import kotlinx.serialization.builtins.ListSerializer
import java.io.File
import java.security.MessageDigest

class FeedCache(
    private val directory: File,
) {
    fun read(key: String): List<Article>? {
        val file = fileFor(key)
        if (!file.exists()) return null
        return runCatching {
            ArticleParser.json.decodeFromString(ListSerializer(Article.serializer()), file.readText())
        }.getOrNull()
    }

    fun write(key: String, articles: List<Article>) {
        if (articles.isEmpty()) return
        runCatching {
            directory.mkdirs()
            val target = fileFor(key)
            val temporary = File(directory, target.name + ".tmp")
            temporary.writeText(
                ArticleParser.json.encodeToString(ListSerializer(Article.serializer()), articles),
            )
            if (target.exists()) target.delete()
            if (!temporary.renameTo(target)) {
                target.writeText(temporary.readText())
                temporary.delete()
            }
        }
    }

    private fun fileFor(key: String): File {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray())
            .take(6)
            .joinToString("") { "%02x".format(it) }
        val slug = key.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(24)
            .ifBlank { "feed" }
        return File(directory, "$slug-$digest.json")
    }
}
