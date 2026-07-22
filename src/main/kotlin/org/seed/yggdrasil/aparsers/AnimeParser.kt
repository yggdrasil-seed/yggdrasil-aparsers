package org.seed.yggdrasil.aparsers

import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Interceptor
import org.seed.yggdrasil.aparsers.config.ConfigKey
import org.seed.yggdrasil.aparsers.config.AnimeSourceConfig
import org.seed.yggdrasil.aparsers.model.*
import org.seed.yggdrasil.aparsers.util.LinkResolver

public interface AnimeParser : Interceptor {

    public val source: AnimeParserSource

    public val availableSortOrders: Set<SortOrder>

    public val filterCapabilities: AnimeListFilterCapabilities

    public val config: AnimeSourceConfig

    public val authorizationProvider: AnimeParserAuthProvider?
        get() = this as? AnimeParserAuthProvider

    public val configKeyDomain: ConfigKey.Domain

    public val domain: String

    public suspend fun getList(offset: Int, order: SortOrder, filter: AnimeListFilter): List<Anime>

    public suspend fun getAnimeList(filter: AnimeListFilter): List<Anime>

    public suspend fun getAnimeDetails(anime: Anime): Anime

    public suspend fun getEpisodeList(anime: Anime): List<Episode>

    public suspend fun getVideoList(episode: Episode): List<Video>

    public suspend fun getFilterOptions(): AnimeListFilterOptions

    public suspend fun getFavicons(): Favicons

    public fun onCreateConfig(keys: MutableCollection<ConfigKey<*>>)

    public suspend fun getRelatedAnime(seed: Anime): List<Anime>

    public fun getRequestHeaders(): Headers

    @InternalAnimeParsersApi
    public suspend fun resolveLink(resolver: LinkResolver, link: HttpUrl): Anime?
}
