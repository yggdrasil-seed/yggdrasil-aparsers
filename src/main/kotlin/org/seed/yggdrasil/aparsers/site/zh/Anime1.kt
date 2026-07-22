package org.seed.yggdrasil.aparsers.site.zh

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

@AnimeSourceParser(nameKey = "anime1", title = "Anime1", locale = "zh")
internal class Anime1(context: AnimeLoaderContext) : ParsedAnimeParser(context) {

    override val configKeyDomain: ConfigKey.Domain = ConfigKey.Domain("anime1.me")

    override suspend fun getList(offset: Int, order: SortOrder, filter: AnimeListFilter): List<Anime> {
        val page = filter.page
        val query = filter.query
        val url = if (!query.isNullOrBlank()) {
            "https://$domain/?s=$query"
        } else {
            "https://$domain/page/$page"
        }

        val doc = fetchDocument(url)
        return doc.select("article.post").mapNotNull { el ->
            val titleEl = el.selectFirst("h2.entry-title a") ?: return@mapNotNull null
            val href = titleEl.attr("href")
            val title = titleEl.text().trim()

            Anime(
                id = href,
                title = title,
                url = if (href.startsWith("http")) href else "https://$domain$href",
            )
        }
    }

    override suspend fun getAnimeDetails(anime: Anime): Anime {
        val doc = fetchDocument(anime.url)
        val desc = doc.selectFirst("div.entry-content")?.text()

        return anime.copy(
            description = desc,
            status = AnimeStatus.UNKNOWN,
        )
    }

    override suspend fun getEpisodeList(anime: Anime): List<Episode> {
        val doc = fetchDocument(anime.url)
        return doc.select("article.post").mapIndexed { index, el ->
            val href = el.selectFirst("h2.entry-title a")?.attr("href").orEmpty()
            val epName = el.selectFirst("h2.entry-title")?.text().orEmpty()
            val epNum = (index + 1).toFloat()

            Episode(
                id = href,
                name = epName.ifBlank { "第 ${epNum.toInt()} 話" },
                episodeNumber = epNum,
                url = if (href.startsWith("http")) href else "https://$domain$href",
            )
        }
    }

    override suspend fun getVideoList(episode: Episode): List<Video> {
        val doc = fetchDocument(episode.url)
        val videoUrl = doc.selectFirst("video source")?.attr("src")
            ?: doc.selectFirst("iframe")?.attr("src")
            ?: return emptyList()

        return listOf(
            Video(
                url = episode.url,
                quality = "Anime1 Player",
                videoUrl = if (videoUrl.startsWith("//")) "https:$videoUrl" else videoUrl,
            )
        )
    }
}
