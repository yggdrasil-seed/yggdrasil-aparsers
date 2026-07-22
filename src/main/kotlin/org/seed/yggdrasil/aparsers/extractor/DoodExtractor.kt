package org.seed.yggdrasil.aparsers.extractor

import okhttp3.OkHttpClient
import okhttp3.Request
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.await

public class DoodExtractor(client: OkHttpClient) : VideoExtractor(client) {
    override suspend fun videosFromUrl(url: String): List<Video> {
        val newUrl = url.replace("dood.to", "dood.so")
            .replace("dood.la", "dood.so")
            .replace("dood.ws", "dood.so")
            .replace("dood.pm", "dood.so")
            .replace("dood.wf", "dood.so")

        val req = Request.Builder().url(newUrl).build()
        val res = client.newCall(req).await()
        val html = res.body?.string().orEmpty()

        val passPath = html.substringAfter("/pass_md5/", "").substringBefore("'")
        if (passPath.isEmpty()) return emptyList()

        val md5Url = "https://dood.so/pass_md5/$passPath"
        val passReq = Request.Builder().url(md5Url).header("User-Agent", "Mozilla/5.0").header("Referer", newUrl).build()
        val passRes = client.newCall(passReq).await()
        val token = passRes.body?.string().orEmpty()

        val randomString = (1..10).map { (('a'..'z') + ('A'..'Z') + ('0'..'9')).random() }.joinToString("")
        val videoUrl = "$token$randomString?token=$passPath&expiry=${System.currentTimeMillis()}"

        return listOf(
            Video(
                url = newUrl,
                quality = "Doodstream",
                videoUrl = videoUrl,
                headers = mapOf("User-Agent" to "Mozilla/5.0", "Referer" to newUrl),
            )
        )
    }
}
