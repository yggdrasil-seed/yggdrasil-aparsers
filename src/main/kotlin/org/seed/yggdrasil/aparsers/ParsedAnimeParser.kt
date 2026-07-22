package org.seed.yggdrasil.aparsers

import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.seed.yggdrasil.aparsers.util.await

public abstract class ParsedAnimeParser(
    protected val context: AnimeLoaderContext,
) : AnimeParser {

    protected suspend fun fetchDocument(url: String, headers: Map<String, String> = emptyMap()): Document {
        val builder = Request.Builder().url(url)
        headers.forEach { (k, v) -> builder.header(k, v) }
        val response = context.httpClient.newCall(builder.build()).await()
        val body = response.body?.string().orEmpty()
        return Jsoup.parse(body, url)
    }
}
