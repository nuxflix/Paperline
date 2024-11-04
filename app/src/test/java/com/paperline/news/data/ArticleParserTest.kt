package com.paperline.news.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleParserTest {
    @Test
    fun parsesHeadlinesAndDropsBlankTitles() {
        val articles = ArticleParser.parseSearch(
            """
            {
              "results": [
                {
                  "id": "abc",
                  "url": "https://example.com/a",
                  "title": "Cities test new flood barriers",
                  "description": "A short standfirst.",
                  "published_at": "2024-06-01T15:04:00Z",
                  "host": "www.example.com",
                  "sitename": "Harbor Desk",
                  "image": "https://example.com/a.jpg",
                  "author": "Ada Shore"
                },
                {
                  "id": "skip",
                  "url": "https://example.com/blank",
                  "title": "   "
                },
                {
                  "id": "ghi",
                  "url": "https://example.com/c",
                  "title": "Markets &amp; open calls",
                  "host": "www.markets.example"
                }
              ]
            }
            """.trimIndent(),
        )

        assertEquals(2, articles.size)
        assertEquals("Harbor Desk", articles[0].publisher)
        assertEquals("https://example.com/a.jpg", articles[0].image)
        assertEquals("Ada Shore", articles[0].author)
        assertEquals("2024-06-01T15:04:00Z", articles[0].publishedAt)
        assertEquals("markets.example", articles[1].publisher)
        assertEquals("Markets & open calls", articles[1].title)
        assertNull(articles[1].image)
    }

    @Test
    fun parsesArticleBody() {
        val article = ArticleParser.parseArticle(
            """
            {
              "id": "abc",
              "url": "https://example.com/a",
              "title": "Cities test new flood barriers",
              "text": "First paragraph.\n\nSecond paragraph."
            }
            """.trimIndent(),
        )

        assertTrue(article.body.contains("Second paragraph."))
    }

    @Test
    fun dedupesByIdAndTitle() {
        val first = article("1", "Same headline")
        val duplicateId = article("1", "Other")
        val duplicateTitle = article("2", "same headline")
        val unique = article("3", "Different")

        val result = listOf(first, duplicateId, duplicateTitle, unique).dedupeArticles()

        assertEquals(listOf("1", "3"), result.map { it.id })
    }

    private fun article(id: String, title: String) = Article(
        id = id,
        url = "https://example.com/$id",
        title = title,
        publisher = "Desk",
    )
}
