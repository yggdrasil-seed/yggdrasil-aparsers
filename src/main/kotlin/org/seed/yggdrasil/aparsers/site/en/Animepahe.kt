package org.seed.yggdrasil.aparsers.site.en

import org.seed.yggdrasil.aparsers.AnimeLoaderContext
import org.seed.yggdrasil.aparsers.AnimeSourceParser
import org.seed.yggdrasil.aparsers.ParsedAnimeParser
import org.seed.yggdrasil.aparsers.config.ConfigKey
import org.seed.yggdrasil.aparsers.extractor.KwikExtractor
import org.seed.yggdrasil.aparsers.model.Anime
import org.seed.yggdrasil.aparsers.model.AnimeListFilter
import org.seed.yggdrasil.aparsers.model.AnimeParserSource
import org.seed.yggdrasil.aparsers.model.AnimeStatus
import org.seed.yggdrasil.aparsers.model.Episode
import org.seed.yggdrasil.aparsers.model.SortOrder
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.src

@AnimeSourceParser(nameKey = "animepahe", title = "Animepahe", locale = "en")
internal class Animepahe(context: AnimeLoaderContext) : ParsedAnimeParser(context) {

    override val configKeyDomain: ConfigKey.Domain = ConfigKey.Domain("animepahe.ru")

    override suspend fun getList(offset: Int, order: SortOrder, filter: AnimeListFilter): List<Anime> {
        val page = filter.page
        val query = filter.query
        val url = if (!query.isNullOrBlank()) {
            "https://$domain/api?m=search&q=$query"
        } else {
            "https://$domain/api?m=airing&page=$page"
        }

        val doc = fetchDocument(url)
        return doc.select("div.search-results a, div.tab-content a").mapNotNull { el ->
            val href = el.attr("href")
            val title = el.selectFirst("h2, div.title")?.text().orEmpty()
            val img = el.selectFirst("img")?.src()

            if (title.isBlank()) return@mapNotNull null

            Anime(
                id = href,
                title = title,
                url = if (href.startsWith("http")) href else "https://$domain$href",
                coverUrl = img,
            )
        }
    }

    override suspend fun getAnimeDetails(anime: Anime): Anime {
        val doc = fetchDocument(anime.url)
        val desc = doc.selectFirst("div.anime-synopsis")?.text()
        val statusText = doc.selectFirst("p:contains(Status)")?.text()
        val genres = doc.select("div.anime-genre a").map { it.text().trim() }.toSet()

        return anime.copy(
            description = desc,
            status = AnimeStatus.fromString(statusText),
            genres = genres,
        )
    }

    override suspend fun getEpisodeList(anime: Anime): List<Episode> {
        val doc = fetchDocument(anime.url)
        return doc.select("div.episode-list a").mapIndexed { index, el ->
            val href = el.attr("href").trim()
            val epName = el.text().trim()
            val epNum = epName.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: (index + 1).toFloat()

            Episode(
                id = href,
                name = epName.ifBlank { "Episode ${epNum.toInt()}" },
                episodeNumber = epNum,
                url = if (href.startsWith("http")) href else "https://$domain$href",
            )
        }
    }

    override suspend fun getVideoList(episode: Episode): List<Video> {
        val doc = fetchDocument(episode.url)
        val kwikUrl = doc.selectFirst("div#resolutionMenu a")?.attr("href") ?: return emptyList()

        return KwikExtractor(context.httpClient).videosFromUrl(kwikUrl)
    }
}
