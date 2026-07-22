package org.seed.yggdrasil.aparsers.extractor

import okhttp3.OkHttpClient
import okhttp3.Request
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.await

public class KwikExtractor(client: OkHttpClient) : VideoExtractor(client) {
    override suspend fun videosFromUrl(url: String): List<Video> {
        val req = Request.Builder().url(url).header("Referer", "https://animepahe.ru").build()
        val res = client.newCall(req).await()
        val html = res.body?.string().orEmpty()

        val match = """const\s+source\s*=\s*'([^']+)'""".toRegex().find(html) ?: return emptyList()
        val videoUrl = match.groupValues[1]

        return listOf(
            Video(
                url = url,
                quality = "Kwik",
                videoUrl = videoUrl,
                headers = mapOf("Referer" to "https://kwik.cx/"),
            )
        )
    }
}
