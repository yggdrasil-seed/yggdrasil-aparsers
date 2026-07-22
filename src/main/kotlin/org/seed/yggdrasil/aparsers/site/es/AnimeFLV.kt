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

@AnimeSourceParser(nameKey = "animeflv", title = "AnimeFLV", locale = "es")
internal class AnimeFLV(context: AnimeLoaderContext) : ParsedAnimeParser(context) {

    private val baseUrl = "https://animeflv.net"

    override suspend fun getAnimeList(filter: AnimeListFilter): List<Anime> {
        val page = filter.page
        val query = filter.query
        val url = if (!query.isNullOrBlank()) {
            "$baseUrl/browse?q=$query&page=$page"
        } else {
            "$baseUrl/browse?page=$page"
        }

        val doc = fetchDocument(url)
        return doc.select("ul.ListAnimes > li article").mapNotNull { el ->
            val a = el.selectFirst("a") ?: return@mapNotNull null
            val href = a.attr("href")
            val title = el.selectFirst("h3.Title")?.text().orEmpty()
            val img = el.selectFirst("img")?.src()

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
        val desc = doc.selectFirst("div.Description > p")?.text()
        val statusText = doc.selectFirst("span.fa-tv")?.text()
        val genres = doc.select("nav.NvGenres > a").map { it.text().trim() }.toSet()

        return anime.copy(
            description = desc,
            status = AnimeStatus.fromString(statusText),
            genres = genres,
        )
    }

    override suspend fun getEpisodeList(anime: Anime): List<Episode> {
        val doc = fetchDocument(anime.url)
        val scripts = doc.select("script")
        var episodesScript = ""
        for (s in scripts) {
            val html = s.html()
            if (html.contains("var episodes =")) {
                episodesScript = html
                break
            }
        }

        val epRegex = """\[(\d+),(\d+)\]""".toRegex()
        val animeId = anime.url.substringAfter("/anime/").substringBefore("/")

        return epRegex.findAll(episodesScript).map { match ->
            val epNum = match.groupValues[1].toFloatOrNull() ?: 1f
            val epId = match.groupValues[2]
            val epUrl = "$baseUrl/ver/$animeId-$epId"

            Episode(
                id = epUrl,
                name = "Episodio ${epNum.toInt()}",
                episodeNumber = epNum,
                url = epUrl,
            )
        }.toList().reversed()
    }

    override suspend fun getVideoList(episode: Episode): List<Video> {
        val doc = fetchDocument(episode.url)
        val scripts = doc.select("script")
        var videosScript = ""
        for (s in scripts) {
            val html = s.html()
            if (html.contains("var videos =")) {
                videosScript = html
                break
            }
        }

        val codeRegex = """"code"\s*:\s*"([^"]+)"""".toRegex()
        val serverRegex = """"server"\s*:\s*"([^"]+)"""".toRegex()

        val codes = codeRegex.findAll(videosScript).map { it.groupValues[1] }.toList()
        val servers = serverRegex.findAll(videosScript).map { it.groupValues[1] }.toList()

        val videoList = mutableListOf<Video>()
        for (i in codes.indices) {
            val serverName = servers.getOrNull(i) ?: "Embed"
            val codeUrl = codes[i].replace("\\/", "/")

            videoList.add(
                Video(
                    url = episode.url,
                    quality = serverName.replaceFirstChar { it.uppercase() },
                    videoUrl = codeUrl,
                )
            )
        }

        return videoList
    }
}
