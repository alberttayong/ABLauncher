package com.ablauncher.data.repository

import com.ablauncher.data.model.NewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val httpClient: OkHttpClient
) {
    suspend fun fetchTopNews(limit: Int = 5): List<NewsItem> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://news.google.com/rss")
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val body = httpClient.newCall(request).execute().use { it.body?.string() }
                ?: return@withContext emptyList()

            parseRss(body, limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseRss(xml: String, limit: Int): List<NewsItem> {
        val items = mutableListOf<NewsItem>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var inItem = false
            var title = ""
            var link = ""
            var pubDate = ""
            var source = ""
            var currentTag = ""

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT && items.size < limit) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "item") {
                            inItem = true
                            title = ""; link = ""; pubDate = ""; source = ""
                        }
                        if (inItem && currentTag == "source") {
                            source = parser.getAttributeValue(null, "url") ?: ""
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inItem) when (currentTag) {
                            "title" -> title = parser.text?.trim() ?: ""
                            "link" -> link = parser.text?.trim() ?: ""
                            "pubDate" -> pubDate = parser.text?.trim() ?: ""
                            "source" -> if (source.isEmpty()) source = parser.text?.trim() ?: ""
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item" && inItem) {
                            if (title.isNotBlank()) {
                                items.add(NewsItem(
                                    title = title,
                                    source = source.ifBlank { "Google News" },
                                    url = link,
                                    pubDate = pubDate
                                ))
                            }
                            inItem = false
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            // Return whatever was parsed
        }
        return items
    }
}
