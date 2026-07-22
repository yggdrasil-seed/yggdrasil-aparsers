package org.seed.yggdrasil.aparsers

import okhttp3.CookieJar
import okhttp3.OkHttpClient

public abstract class AnimeLoaderContext {
    public abstract val httpClient: OkHttpClient
    public abstract val cookieJar: CookieJar
}
