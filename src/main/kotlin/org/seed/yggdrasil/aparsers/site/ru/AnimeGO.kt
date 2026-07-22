package org.seed.yggdrasil.aparsers.site.ru

import org.seed.yggdrasil.aparsers.AnimeLoaderContext
import org.seed.yggdrasil.aparsers.AnimeSourceParser
import org.seed.yggdrasil.aparsers.ParsedAnimeParser
import org.seed.yggdrasil.aparsers.config.ConfigKey
import org.seed.yggdrasil.aparsers.model.Anime
import org.seed.yggdrasil.aparsers.model.AnimeListFilter
import org.seed.yggdrasil.aparsers.model.AnimeParserSource
import org.seed.yggdrasil.aparsers.model.AnimeStatus
import org.seed.yggdrasil.aparsers.model.Episode
import org.seed.yggdrasil.aparsers.model.SortOrder
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.src

@AnimeSourceParser(nameKey = "animego", title = "AnimeGO", locale = "ru")
internal class AnimeGO(context: AnimeLoaderContext) : ParsedAnimeParser(context) {

    override val configKeyDomain: ConfigKey.Domain = ConfigKey.Domain("animego.org")

    override suspend fun getList(offset: Int, order: SortOrder, filter: AnimeListFilter): List<Anime> {
        val page = filter.page
        val query = filter.query
        val url = if (!query.isNullOrBlank()) {
            "https://$domain/search/anime?q=$query&page=$page"
        } else {
            "https://$domain/anime?page=$page"
        }

        val doc = fetchDocument(url)
        return doc.select("div.animes-grid div.media-body, div#anime-list div.animes-list-item").mapNotNull { el ->
            val a = el.selectFirst("a") ?: return@mapNotNull null
            val href = a.attr("href")
            val title = a.text().trim()
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
        val desc = doc.selectFirst("div.description")?.text()
        val statusText = doc.selectFirst("dt:contains(Статус) + dd")?.text()
        val genres = doc.select("dt:contains(Жанр) + dd a").map { it.text().trim() }.toSet()

        return anime.copy(
            description = desc,
            status = AnimeStatus.fromString(statusText),
            genres = genres,
        )
    }

    override suspend fun getEpisodeList(anime: Anime): List<Episode> {
        val doc = fetchDocument(anime.url)
        return doc.select("div#video-carousel div.item, ul.list-group li").mapIndexed { index, el ->
            val epNumStr = el.attr("data-episode").ifBlank { (index + 1).toString() }
            val epNum = epNumStr.toFloatOrNull() ?: (index + 1).toFloat()

            Episode(
                id = "${anime.url}#ep-$epNumStr",
                name = "Серия ${epNum.toInt()}",
                episodeNumber = epNum,
                url = anime.url,
            )
        }
    }

    override suspend fun getVideoList(episode: Episode): List<Video> {
        val doc = fetchDocument(episode.url)
        val iframeUrl = doc.selectFirst("div#video-player iframe")?.attr("src") ?: return emptyList()

        return listOf(
            Video(
                url = episode.url,
                quality = "AnimeGO Player",
                videoUrl = if (iframeUrl.startsWith("//")) "https:$iframeUrl" else iframeUrl,
            )
        )
    }
}
