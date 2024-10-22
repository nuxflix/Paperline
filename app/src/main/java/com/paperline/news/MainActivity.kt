package com.paperline.news

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.paperline.news.ui.PaperlineApp
import com.paperline.news.ui.theme.PaperlineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as PaperlineApplication).repository
        setContent {
            PaperlineTheme {
                PaperlineApp(repository)
            }
        }
    }
}
