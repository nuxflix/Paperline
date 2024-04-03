package com.paperline.news.data

data class Topic(
    val id: String,
    val title: String,
    val description: String,
    val query: String?,
)

object Topics {
    val all: List<Topic> = listOf(
        Topic(
            id = "world",
            title = "World",
            description = "Front pages from the last day, in English.",
            query = null,
        ),
        Topic(
            id = "technology",
            title = "Technology",
            description = "Software, chips, and the networks they run on.",
            query = "technology",
        ),
        Topic(
            id = "business",
            title = "Business",
            description = "Markets, companies, and work.",
            query = "business",
        ),
        Topic(
            id = "science",
            title = "Science",
            description = "Research, space, and the natural world.",
            query = "science",
        ),
        Topic(
            id = "health",
            title = "Health",
            description = "Medicine, public health, and care.",
            query = "health",
        ),
        Topic(
            id = "sports",
            title = "Sports",
            description = "Games, athletes, and the places they play.",
            query = "sports",
        ),
        Topic(
            id = "climate",
            title = "Climate",
            description = "Weather, energy, and a changing planet.",
            query = "climate",
        ),
        Topic(
            id = "politics",
            title = "Politics",
            description = "Governments, elections, and policy.",
            query = "politics",
        ),
        Topic(
            id = "culture",
            title = "Culture",
            description = "Books, film, music, and art.",
            query = "culture",
        ),
    )

    val DEFAULT_IDS: Set<String> = setOf("world", "technology", "business")

    fun byId(id: String): Topic = all.firstOrNull { it.id == id } ?: all.first()
}
