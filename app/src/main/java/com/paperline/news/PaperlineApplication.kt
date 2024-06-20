package com.paperline.news

import android.app.Application
import com.paperline.news.data.FeedCache
import com.paperline.news.data.NewsApi
import com.paperline.news.data.NewsRepository
import com.paperline.news.data.UserPreferences
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class PaperlineApplication : Application() {
    lateinit var repository: NewsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
        repository = NewsRepository(
            api = NewsApi(client),
            preferences = UserPreferences(this),
            feedCache = FeedCache(File(filesDir, "feeds")),
        )
    }
}
