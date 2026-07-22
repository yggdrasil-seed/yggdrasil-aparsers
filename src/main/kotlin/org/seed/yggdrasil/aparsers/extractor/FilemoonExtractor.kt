package org.seed.yggdrasil.aparsers.extractor

import okhttp3.OkHttpClient
import okhttp3.Request
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.await

public class FilemoonExtractor(client: OkHttpClient) : VideoExtractor(client) {
    override suspend fun videosFromUrl(url: String): List<Video> {
        val req = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
        val res = client.newCall(req).await()
        val html = res.body?.string().orEmpty()

        val match = """file:\s*"([^"]+\.m3u8[^"]*)"""".toRegex().find(html) ?: return emptyList()
        val m3u8 = match.groupValues[1]

        return listOf(
            Video(
                url = url,
                quality = "Filemoon",
                videoUrl = m3u8,
            )
        )
    }
}
