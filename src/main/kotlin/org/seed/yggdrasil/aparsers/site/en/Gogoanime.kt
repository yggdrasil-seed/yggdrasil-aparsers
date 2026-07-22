package org.seed.yggdrasil.aparsers.site.en

import org.seed.yggdrasil.aparsers.AnimeLoaderContext
import org.seed.yggdrasil.aparsers.AnimeSourceParser
import org.seed.yggdrasil.aparsers.ParsedAnimeParser
import org.seed.yggdrasil.aparsers.model.Anime
import org.seed.yggdrasil.aparsers.model.AnimeListFilter
import org.seed.yggdrasil.aparsers.model.AnimeStatus
import org.seed.yggdrasil.aparsers.model.Episode
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.src

@AnimeSourceParser(nameKey = "gogoanime", title = "Gogoanime", locale = "en")
internal class Gogoanime(context: AnimeLoaderContext) : ParsedAnimeParser(context) {

    private val baseUrl = "https://gogoanime3.co"

    override suspend fun getAnimeList(filter: AnimeListFilter): List<Anime> {
        val page = filter.page
        val query = filter.query
        val url = if (!query.isNullOrBlank()) {
            "$baseUrl/search.html?keyword=$query&page=$page"
        } else {
            "$baseUrl/popular.html?page=$page"
        }

        val doc = fetchDocument(url)
        return doc.select("ul.items > li").mapNotNull { element ->
            val titleEl = element.selectFirst("p.name > a") ?: return@mapNotNull null
            val href = titleEl.attr("href")
            val title = titleEl.text()
            val img = element.selectFirst("img")?.src()

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
        val description = doc.selectFirst("div.anime_info_body_bg > p.type:contains(Plot Summary)")?.text()
            ?.replace("Plot Summary:", "")?.trim()
        val statusText = doc.selectFirst("div.anime_info_body_bg > p.type:contains(Status)")?.text()
        val genres = doc.select("div.anime_info_body_bg > p.type:contains(Genre) > a")
            .map { it.text().trim() }
            .toSet()

        return anime.copy(
            description = description,
            status = AnimeStatus.fromString(statusText),
            genres = genres,
        )
    }

    override suspend fun getEpisodeList(anime: Anime): List<Episode> {
        val doc = fetchDocument(anime.url)
        val animeId = doc.selectFirst("input#movie_id")?.attr("value") ?: return emptyList()
        val defaultEp = doc.selectFirst("input#default_ep")?.attr("value") ?: "0"
        val alias = doc.selectFirst("input#alias_anime")?.attr("value") ?: ""

        val epListUrl = "https://ajax.gogocdn.net/ajax/load-list-episode?ep_start=0&ep_end=9999&id=$animeId&default_ep=$defaultEp&alias=$alias"
        val epDoc = fetchDocument(epListUrl)

        return epDoc.select("li > a").mapIndexed { index, element ->
            val href = element.attr("href").trim()
            val epNumStr = element.selectFirst("div.name")?.text()?.replace("EP", "")?.trim()
            val epNum = epNumStr?.toFloatOrNull() ?: (index + 1).toFloat()

            Episode(
                id = href,
                name = "Episode ${epNum.toInt()}",
                episodeNumber = epNum,
                url = if (href.startsWith("http")) href else "$baseUrl$href",
            )
        }.reversed()
    }

    override suspend fun getVideoList(episode: Episode): List<Video> {
        val doc = fetchDocument(episode.url)
        val iframeUrl = doc.selectFirst("iframe")?.attr("src") ?: return emptyList()
        val fullIframeUrl = if (iframeUrl.startsWith("//")) "https:$iframeUrl" else iframeUrl

        return listOf(
            Video(
                url = episode.url,
                quality = "Default (Embed)",
                videoUrl = fullIframeUrl,
            )
        )
    }
}
