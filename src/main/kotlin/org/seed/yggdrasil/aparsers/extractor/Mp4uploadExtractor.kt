package org.seed.yggdrasil.aparsers.extractor

import okhttp3.OkHttpClient
import okhttp3.Request
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.await

public class Mp4uploadExtractor(client: OkHttpClient) : VideoExtractor(client) {
    override suspend fun videosFromUrl(url: String): List<Video> {
        val req = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
        val res = client.newCall(req).await()
        val html = res.body?.string().orEmpty()

        val match = """player\.src\("([^"]+)"""".toRegex().find(html) ?: return emptyList()
        val src = match.groupValues[1]

        return listOf(
            Video(
                url = url,
                quality = "Mp4upload",
                videoUrl = src,
                headers = mapOf("User-Agent" to "Mozilla/5.0", "Referer" to url),
            )
        )
    }
}
