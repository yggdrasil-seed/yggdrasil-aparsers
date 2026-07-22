package org.seed.yggdrasil.aparsers.site.pt

import org.seed.yggdrasil.aparsers.AnimeLoaderContext
import org.seed.yggdrasil.aparsers.AnimeSourceParser
import org.seed.yggdrasil.aparsers.ParsedAnimeParser
import org.seed.yggdrasil.aparsers.model.Anime
import org.seed.yggdrasil.aparsers.model.AnimeListFilter
import org.seed.yggdrasil.aparsers.model.AnimeStatus
import org.seed.yggdrasil.aparsers.model.Episode
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.src

@AnimeSourceParser(nameKey = "animefire", title = "AnimeFire", locale = "pt")
internal class AnimeFire(context: AnimeLoaderContext) : ParsedAnimeParser(context) {

    private val baseUrl = "https://animefire.plus"

    override suspend fun getAnimeList(filter: AnimeListFilter): List<Anime> {
        val page = filter.page
        val query = filter.query
        val url = if (!query.isNullOrBlank()) {
            "$baseUrl/pesquisar/$query/$page"
        } else {
            "$baseUrl/animes/pagina/$page"
        }

        val doc = fetchDocument(url)
        return doc.select("article.card, div.row.cards > div").mapNotNull { el ->
            val a = el.selectFirst("a") ?: return@mapNotNull null
            val href = a.attr("href")
            val title = el.selectFirst("h3, h4, span.card-title")?.text().orEmpty()
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
        val desc = doc.selectFirst("div.divSinopse > span")?.text()
        val statusText = doc.selectFirst("span.badge")?.text()
        val genres = doc.select("a.btn-generos").map { it.text().trim() }.toSet()

        return anime.copy(
            description = desc,
            status = AnimeStatus.fromString(statusText),
            genres = genres,
        )
    }

    override suspend fun getEpisodeList(anime: Anime): List<Episode> {
        val doc = fetchDocument(anime.url)
        return doc.select("div.divListEpisodes a").mapIndexed { index, el ->
            val href = el.attr("href").trim()
            val epName = el.text().trim()
            val epNum = epName.replace("Episódio", "").trim().toFloatOrNull() ?: (index + 1).toFloat()

            Episode(
                id = href,
                name = epName.ifBlank { "Episódio ${epNum.toInt()}" },
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
                quality = "Default (Embed)",
                videoUrl = if (iframeUrl.startsWith("//")) "https:$iframeUrl" else iframeUrl,
            )
        )
    }
}
