package org.seed.yggdrasil.aparsers.extractor

import okhttp3.OkHttpClient
import okhttp3.Request
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.await

public class MixdropExtractor(client: OkHttpClient) : VideoExtractor(client) {
    override suspend fun videosFromUrl(url: String): List<Video> {
        val embedUrl = if (!url.contains("/e/")) {
            url.replace("mixdrop.co/", "mixdrop.co/e/")
        } else url

        val req = Request.Builder().url(embedUrl).header("User-Agent", "Mozilla/5.0").build()
        val res = client.newCall(req).await()
        val html = res.body?.string().orEmpty()

        val match = """MDCore\.wurl\s*=\s*"([^"]+)"""".toRegex().find(html) ?: return emptyList()
        var streamUrl = match.groupValues[1]
        if (streamUrl.startsWith("//")) {
            streamUrl = "https:$streamUrl"
        }

        return listOf(
            Video(
                url = embedUrl,
                quality = "MixDrop",
                videoUrl = streamUrl,
                headers = mapOf("User-Agent" to "Mozilla/5.0", "Referer" to embedUrl),
            )
        )
    }
}
