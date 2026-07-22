package org.seed.yggdrasil.aparsers.core

import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.seed.yggdrasil.aparsers.AnimeLoaderContext
import org.seed.yggdrasil.aparsers.AnimeParser
import org.seed.yggdrasil.aparsers.InternalAnimeParsersApi
import org.seed.yggdrasil.aparsers.config.AnimeSourceConfig
import org.seed.yggdrasil.aparsers.config.ConfigKey
import org.seed.yggdrasil.aparsers.model.*
import org.seed.yggdrasil.aparsers.util.LinkResolver
import org.seed.yggdrasil.aparsers.util.await
import java.util.EnumSet

public abstract class AbstractAnimeParser(
    protected val context: AnimeLoaderContext,
) : AnimeParser {

    override lateinit var source: AnimeParserSource

    override val availableSortOrders: Set<SortOrder>
        get() = EnumSet.of(SortOrder.POPULARITY)

    override val filterCapabilities: AnimeListFilterCapabilities
        get() = AnimeListFilterCapabilities()

    override val config: AnimeSourceConfig = object : AnimeSourceConfig {
        override fun <T> get(key: ConfigKey<T>): T = key.defaultValue
    }

    override val domain: String
        get() = configKeyDomain.defaultValue

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        return chain.proceed(req)
    }

    override suspend fun getAnimeList(filter: AnimeListFilter): List<Anime> {
        return getList(0, SortOrder.POPULARITY, filter)
    }

    override suspend fun getFilterOptions(): AnimeListFilterOptions = AnimeListFilterOptions()

    override suspend fun getFavicons(): Favicons = Favicons()

    override fun onCreateConfig(keys: MutableCollection<ConfigKey<*>>) {
        keys.add(configKeyDomain)
    }

    override suspend fun getRelatedAnime(seed: Anime): List<Anime> = emptyList()

    override fun getRequestHeaders(): Headers = Headers.Builder().build()

    @InternalAnimeParsersApi
    override suspend fun resolveLink(resolver: LinkResolver, link: HttpUrl): Anime? = null

    protected suspend fun fetchDocument(url: String): Document {
        val req = okhttp3.Request.Builder().url(url).headers(getRequestHeaders()).build()
        val res = context.httpClient.newCall(req).await()
        val html = res.body?.string().orEmpty()
        return Jsoup.parse(html, url)
    }
}
