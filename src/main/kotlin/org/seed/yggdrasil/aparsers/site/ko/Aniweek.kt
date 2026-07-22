package org.seed.yggdrasil.aparsers.site.ko

import org.seed.yggdrasil.aparsers.AnimeLoaderContext
import org.seed.yggdrasil.aparsers.AnimeSourceParser
import org.seed.yggdrasil.aparsers.ParsedAnimeParser
import org.seed.yggdrasil.aparsers.model.Anime
import org.seed.yggdrasil.aparsers.model.AnimeListFilter
import org.seed.yggdrasil.aparsers.model.AnimeStatus
import org.seed.yggdrasil.aparsers.model.Episode
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.src

@AnimeSourceParser(nameKey = "aniweek", title = "Aniweek", locale = "ko")
internal class Aniweek(context: AnimeLoaderContext) : ParsedAnimeParser(context) {

    private val baseUrl = "https://aniweek.com"

    override suspend fun getAnimeList(filter: AnimeListFilter): List<Anime> {
        val page = filter.page
        val query = filter.query
        val url = if (!query.isNullOrBlank()) {
            "$baseUrl/?s=$query&page=$page"
        } else {
            "$baseUrl/anime/page/$page"
        }

        val doc = fetchDocument(url)
        return doc.select("div.item, article.anime").mapNotNull { el ->
            val a = el.selectFirst("a") ?: return@mapNotNull null
            val href = a.attr("href")
            val title = el.selectFirst("h3, h2, div.title")?.text().orEmpty()
            val img = el.selectFirst("img")?.src()

            if (title.isBlank()) return@mapNotNull null

            Anime(
                id = href,
                title = title,
                url = if (href.startsWith("http")) href else "$baseUrl$href",
                coverUrl = img,
            )
        }
    }

    override suspend fun getAnimeDetails(anime: Anime): Anime {
        val doc = fetchDocument(anime.url)
        val desc = doc.selectFirst("div.synopsis, div.entry-content")?.text()

        return anime.copy(
            description = desc,
            status = AnimeStatus.UNKNOWN,
        )
    }

    override suspend fun getEpisodeList(anime: Anime): List<Episode> {
        val doc = fetchDocument(anime.url)
        return doc.select("ul.episodes li a, div.episode-list a").mapIndexed { index, el ->
            val href = el.attr("href").trim()
            val epName = el.text().trim()
            val epNum = epName.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: (index + 1).toFloat()

            Episode(
                id = href,
                name = epName.ifBlank { "${epNum.toInt()}화" },
                episodeNumber = epNum,
                url = if (href.startsWith("http")) href else "$baseUrl$href",
            )
        }
    }

    override suspend fun getVideoList(episode: Episode): List<Video> {
        val doc = fetchDocument(episode.url)
        val iframeUrl = doc.selectFirst("iframe")?.attr("src") ?: return emptyList()

        return listOf(
            Video(
                url = episode.url,
                quality = "Aniweek Stream",
                videoUrl = if (iframeUrl.startsWith("//")) "https:$iframeUrl" else iframeUrl,
            )
        )
    }
}
