package com.paperline.news.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ArticleTextTest {
    private val now = Instant.parse("2024-06-01T12:00:00Z")

    @Test
    fun formatsRecentTimes() {
        assertEquals("Just now", ArticleText.publishedLabel("2024-06-01T11:59:30Z", now))
        assertEquals("Just now", ArticleText.publishedLabel("2024-06-01T12:05:00Z", now))
        assertEquals("2h ago", ArticleText.publishedLabel("2024-06-01T10:00:00Z", now))
        assertEquals("3d ago", ArticleText.publishedLabel("2024-05-29T12:00:00Z", now))
        assertEquals("", ArticleText.publishedLabel("not-a-date", now))
    }

    @Test
    fun splitsParagraphsAndFallsBackToDescription() {
        assertEquals(
            listOf("One", "Two"),
            ArticleText.paragraphs("One\n\nTwo", "unused"),
        )
        assertEquals(
            listOf("Only the standfirst"),
            ArticleText.paragraphs("", "Only the standfirst"),
        )
    }
}
