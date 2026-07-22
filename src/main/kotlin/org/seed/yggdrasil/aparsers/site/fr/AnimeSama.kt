package org.seed.yggdrasil.aparsers.site.fr

import org.seed.yggdrasil.aparsers.AnimeLoaderContext
import org.seed.yggdrasil.aparsers.AnimeSourceParser
import org.seed.yggdrasil.aparsers.ParsedAnimeParser
import org.seed.yggdrasil.aparsers.model.Anime
import org.seed.yggdrasil.aparsers.model.AnimeListFilter
import org.seed.yggdrasil.aparsers.model.AnimeStatus
import org.seed.yggdrasil.aparsers.model.Episode
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.src

@AnimeSourceParser(nameKey = "animesama", title = "AnimeSama", locale = "fr")
internal class AnimeSama(context: AnimeLoaderContext) : ParsedAnimeParser(context) {

    private val baseUrl = "https://anime-sama.fr"

    override suspend fun getAnimeList(filter: AnimeListFilter): List<Anime> {
        val query = filter.query
        val url = if (!query.isNullOrBlank()) {
            "$baseUrl/catalogue/?search=$query"
        } else {
            "$baseUrl/catalogue/"
        }

        val doc = fetchDocument(url)
        return doc.select("div#cardContainer div.card, div.anime-card").mapNotNull { el ->
            val a = el.selectFirst("a") ?: return@mapNotNull null
            val href = a.attr("href")
            val title = el.selectFirst("h1, h2, h3, div.card-title")?.text().orEmpty()
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
        val desc = doc.selectFirst("p#synopsis, div.synopsis")?.text()
        val genres = doc.select("div.genres span, div.tags a").map { it.text().trim() }.toSet()

        return anime.copy(
            description = desc,
            status = AnimeStatus.UNKNOWN,
            genres = genres,
        )
    }

    override suspend fun getEpisodeList(anime: Anime): List<Episode> {
        val doc = fetchDocument(anime.url)
        return doc.select("div#episodesList a, div.episodes a").mapIndexed { index, el ->
            val href = el.attr("href").trim()
            val epName = el.text().trim()
            val epNum = epName.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: (index + 1).toFloat()

            Episode(
                id = href,
                name = epName.ifBlank { "Épisode ${epNum.toInt()}" },
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
                quality = "AnimeSama Stream",
                videoUrl = if (iframeUrl.startsWith("//")) "https:$iframeUrl" else iframeUrl,
            )
        )
    }
}
