package org.seed.yggdrasil.aparsers.extractor

import okhttp3.OkHttpClient
import okhttp3.Request
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.await

public class SibnetExtractor(client: OkHttpClient) : VideoExtractor(client) {
    override suspend fun videosFromUrl(url: String): List<Video> {
        val req = Request.Builder().url(url).header("Referer", url).build()
        val res = client.newCall(req).await()
        val html = res.body?.string().orEmpty()

        val match = """player\.src\(\[\{src:\s*"([^"]+)"""".toRegex().find(html) ?: return emptyList()
        val path = match.groupValues[1]
        val videoUrl = if (path.startsWith("//")) "https:$path" else "https://video.sibnet.ru$path"

        return listOf(
            Video(
                url = url,
                quality = "Sibnet",
                videoUrl = videoUrl,
                headers = mapOf("Referer" to url),
            )
        )
    }
}
