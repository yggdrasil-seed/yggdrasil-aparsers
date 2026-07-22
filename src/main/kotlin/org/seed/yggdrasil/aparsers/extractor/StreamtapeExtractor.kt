package org.seed.yggdrasil.aparsers.extractor

import okhttp3.OkHttpClient
import okhttp3.Request
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.await

public class StreamtapeExtractor(client: OkHttpClient) : VideoExtractor(client) {
    override suspend fun videosFromUrl(url: String): List<Video> {
        val req = Request.Builder().url(url).build()
        val res = client.newCall(req).await()
        val html = res.body?.string().orEmpty()

        val targetRegex = """id="robotlink"\s*>\s*(.*?)</""".toRegex()
        val match = targetRegex.find(html) ?: return emptyList()
        val linkPath = match.groupValues[1]
        val directUrl = "https:" + linkPath

        return listOf(
            Video(
                url = url,
                quality = "Streamtape",
                videoUrl = directUrl,
            )
        )
    }
}
