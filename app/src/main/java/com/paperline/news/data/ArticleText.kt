package com.paperline.news.data

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object ArticleText {
    fun paragraphs(body: String, description: String): List<String> {
        val source = body.ifBlank { description }.trim()
        if (source.isEmpty()) return emptyList()
        val blocks = source
            .split(Regex("\\n\\s*\\n"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (blocks.size > 1) return blocks
        val lines = source.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
        return lines.ifEmpty { listOf(source) }
    }

    fun publishedLabel(iso: String?, now: Instant = Instant.now()): String {
        if (iso.isNullOrBlank()) return ""
        val published = runCatching { Instant.parse(iso) }.getOrNull() ?: return ""
        val minutes = Duration.between(published, now).toMinutes()
        if (minutes < 1) return "Just now"
        return when {
            minutes < 60 -> "${minutes}m ago"
            minutes < 60 * 24 -> "${minutes / 60}h ago"
            minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)}d ago"
            else -> MONTH_DAY.format(published)
        }
    }

    private val MONTH_DAY: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM d", Locale.US).withZone(ZoneId.systemDefault())
}
