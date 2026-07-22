package org.seed.yggdrasil.aparsers.site.id

import org.seed.yggdrasil.aparsers.AnimeLoaderContext
import org.seed.yggdrasil.aparsers.AnimeSourceParser
import org.seed.yggdrasil.aparsers.ParsedAnimeParser
import org.seed.yggdrasil.aparsers.model.Anime
import org.seed.yggdrasil.aparsers.model.AnimeListFilter
import org.seed.yggdrasil.aparsers.model.AnimeStatus
import org.seed.yggdrasil.aparsers.model.Episode
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.src

@AnimeSourceParser(nameKey = "samehadaku", title = "Samehadaku", locale = "id")
internal class Samehadaku(context: AnimeLoaderContext) : ParsedAnimeParser(context) {

    private val baseUrl = "https://samehadaku.email"

    override suspend fun getAnimeList(filter: AnimeListFilter): List<Anime> {
        val page = filter.page
        val query = filter.query
        val url = if (!query.isNullOrBlank()) {
            "$baseUrl/page/$page/?s=$query"
        } else {
            "$baseUrl/anime-terbaru/page/$page/"
        }

        val doc = fetchDocument(url)
        return doc.select("main#main div.animpost, main#main article").mapNotNull { el ->
            val a = el.selectFirst("a") ?: return@mapNotNull null
            val href = a.attr("href")
            val title = el.selectFirst("h2.title, h2, div.title")?.text().orEmpty()
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
        val desc = doc.selectFirst("div.entry-content, div.desc")?.text()
        val statusText = doc.selectFirst("span:contains(Status)")?.text()
        val genres = doc.select("div.genre-info a, span.genre a").map { it.text().trim() }.toSet()

        return anime.copy(
            description = desc,
            status = AnimeStatus.fromString(statusText),
            genres = genres,
        )
    }

    override suspend fun getEpisodeList(anime: Anime): List<Episode> {
        val doc = fetchDocument(anime.url)
        return doc.select("div.lister ul li a").mapIndexed { index, el ->
            val href = el.attr("href").trim()
            val epName = el.text().trim()
            val epNum = epName.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: (index + 1).toFloat()

            Episode(
                id = href,
                name = epName.ifBlank { "Episode ${epNum.toInt()}" },
                episodeNumber = epNum,
                url = if (href.startsWith("http")) href else "$baseUrl$href",
            )
        }
    }

    override suspend fun getVideoList(episode: Episode): List<Video> {
        val doc = fetchDocument(episode.url)
        val iframeUrl = doc.selectFirst("div.server-item iframe, div#embed_holder iframe")?.attr("src") ?: return emptyList()

        return listOf(
            Video(
                url = episode.url,
                quality = "Samehadaku Stream",
                videoUrl = if (iframeUrl.startsWith("//")) "https:$iframeUrl" else iframeUrl,
            )
        )
    }
}
