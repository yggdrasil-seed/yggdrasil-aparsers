package org.seed.yggdrasil.aparsers.extractor

import okhttp3.OkHttpClient
import org.seed.yggdrasil.aparsers.model.Video

public abstract class VideoExtractor(
    protected val client: OkHttpClient,
) {
    public abstract suspend fun videosFromUrl(url: String): List<Video>
}
