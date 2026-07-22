package org.seed.yggdrasil.aparsers.site.es

import org.seed.yggdrasil.aparsers.AnimeLoaderContext
import org.seed.yggdrasil.aparsers.AnimeSourceParser
import org.seed.yggdrasil.aparsers.ParsedAnimeParser
import org.seed.yggdrasil.aparsers.model.Anime
import org.seed.yggdrasil.aparsers.model.AnimeListFilter
import org.seed.yggdrasil.aparsers.model.AnimeStatus
import org.seed.yggdrasil.aparsers.model.Episode
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.src

@AnimeSourceParser(nameKey = "jkanime", title = "JKanime", locale = "es")
internal class JKanime(context: AnimeLoaderContext) : ParsedAnimeParser(context) {

    private val baseUrl = "https://jkanime.net"

    override suspend fun getAnimeList(filter: AnimeListFilter): List<Anime> {
        val page = filter.page
        val query = filter.query
        val url = if (!query.isNullOrBlank()) {
            "$baseUrl/buscar/$query/$page/"
        } else {
            "$baseUrl/directorio/$page/"
        }

        val doc = fetchDocument(url)
        return doc.select("div.anime__item, div.portada-box").mapNotNull { el ->
            val a = el.selectFirst("a") ?: return@mapNotNull null
            val href = a.attr("href")
            val title = el.selectFirst("h5, h2")?.text().orEmpty()
            val img = el.selectFirst("img, div.anime__item__pic")?.src() ?: el.selectFirst("div.anime__item__pic")?.attr("data-setbg")

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
        val desc = doc.selectFirst("div.sinopsis, p.plot")?.text()
        val statusText = doc.selectFirst("span:contains(Estado)")?.text()
        val genres = doc.select("div.genres a, nav.genres a").map { it.text().trim() }.toSet()

        return anime.copy(
            description = desc,
            status = AnimeStatus.fromString(statusText),
            genres = genres,
        )
    }

    override suspend fun getEpisodeList(anime: Anime): List<Episode> {
        val doc = fetchDocument(anime.url)
        return doc.select("div.anime__pagination a, div.list-episodes a").mapIndexed { index, el ->
            val href = el.attr("href").trim()
            val epName = el.text().trim()
            val epNum = epName.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: (index + 1).toFloat()

            Episode(
                id = href,
                name = epName.ifBlank { "Episodio ${epNum.toInt()}" },
                episodeNumber = epNum,
                url = if (href.startsWith("http")) href else "$baseUrl$href",
            )
        }
    }

    override suspend fun getVideoList(episode: Episode): List<Video> {
        val doc = fetchDocument(episode.url)
        val iframeUrl = doc.selectFirst("iframe#player, div.player-box iframe")?.attr("src") ?: return emptyList()

        return listOf(
            Video(
                url = episode.url,
                quality = "JKanime Stream",
                videoUrl = if (iframeUrl.startsWith("//")) "https:$iframeUrl" else iframeUrl,
            )
        )
    }
}
