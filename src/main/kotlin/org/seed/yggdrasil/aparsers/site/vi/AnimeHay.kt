package org.seed.yggdrasil.aparsers.site.vi

import org.seed.yggdrasil.aparsers.AnimeLoaderContext
import org.seed.yggdrasil.aparsers.AnimeSourceParser
import org.seed.yggdrasil.aparsers.ParsedAnimeParser
import org.seed.yggdrasil.aparsers.config.ConfigKey
import org.seed.yggdrasil.aparsers.model.Anime
import org.seed.yggdrasil.aparsers.model.AnimeListFilter
import org.seed.yggdrasil.aparsers.model.AnimeStatus
import org.seed.yggdrasil.aparsers.model.Episode
import org.seed.yggdrasil.aparsers.model.SortOrder
import org.seed.yggdrasil.aparsers.model.Video
import org.seed.yggdrasil.aparsers.util.src

@AnimeSourceParser(nameKey = "animehay", title = "AnimeHay", locale = "vi")
internal class AnimeHay(context: AnimeLoaderContext) : ParsedAnimeParser(context) {

    override val configKeyDomain: ConfigKey.Domain = ConfigKey.Domain("animehay.work", "animehay.club")

    override suspend fun getList(offset: Int, order: SortOrder, filter: AnimeListFilter): List<Anime> {
        val page = filter.page
        val query = filter.query
        val url = if (!query.isNullOrBlank()) {
            "https://$domain/tim-kiem/$query"
        } else if (page > 1) {
            "https://$domain/page/$page"
        } else {
            "https://$domain"
        }

        val doc = fetchDocument(url)
        return doc.select(".card-item").mapNotNull { element ->
            val a = element.selectFirst(".card-item__img-href") ?: return@mapNotNull null
            val href = a.attr("href")
            val img = element.selectFirst(".card-item-img")?.attr("data-original") ?: element.selectFirst("img")?.src()
            val title = element.selectFirst("h3 a")?.text().orEmpty()

            if (title.isBlank()) return@mapNotNull null

            val coverUrl = when {
                img.orEmpty().startsWith("http") -> img
                img.orEmpty().startsWith("//") -> "https:$img"
                !img.isNullOrBlank() -> "https://$domain$img"
                else -> null
            }

            Anime(
                id = href,
                title = title,
                url = if (href.startsWith("http")) href else "https://$domain$href",
                coverUrl = coverUrl,
            )
        }
    }

    override suspend fun getAnimeDetails(anime: Anime): Anime {
        val doc = fetchDocument(anime.url)
        val description = doc.select(".box").find { it.selectFirst(".box-header")?.text()?.contains("Nội dung") == true }
            ?.selectFirst(".box-content")?.text()
            ?: doc.selectFirst(".box-content")?.text()
            ?: doc.selectFirst(".info-movie p")?.text()
            ?: doc.selectFirst(".film-description")?.text()

        val genres = doc.select(".list-info li:contains(Thể loại) a, .movie-des dd a[href*=/genres/]").map { it.text().trim() }.toSet()

        return anime.copy(
            description = description,
            genres = genres,
            status = AnimeStatus.UNKNOWN,
        )
    }

    override suspend fun getEpisodeList(anime: Anime): List<Episode> {
        val doc = fetchDocument(anime.url)
        val watchBtn = doc.selectFirst(".btn-watch") ?: return emptyList()

        var watchUrl = watchBtn.attr("href")
        if (watchUrl.startsWith("//")) {
            watchUrl = "https:$watchUrl"
        } else if (!watchUrl.startsWith("http")) {
            watchUrl = "https://$domain$watchUrl"
        }

        val watchDoc = fetchDocument(watchUrl)
        val items = watchDoc.select(".me-list a.me-item, .me-list a, .list-item-episode a")

        return items.mapIndexed { index, el ->
            var epUrl = el.attr("href").trim()
            if (!epUrl.startsWith("http")) {
                epUrl = "https://$domain$epUrl"
            }
            val titleText = el.text().trim().ifBlank { el.attr("title") }
            val epNum = titleText.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: (index + 1).toFloat()

            Episode(
                id = epUrl,
                name = titleText.ifBlank { "Tập ${epNum.toInt()}" },
                episodeNumber = epNum,
                url = epUrl,
            )
        }
    }

    override suspend fun getVideoList(episode: Episode): List<Video> {
        val doc = fetchDocument(episode.url)
        val videos = mutableListOf<Video>()

        val serverButtons = doc.select(".streaming-server")
        for (btn in serverButtons) {
            val link = btn.attr("data-link").trim()
            val type = btn.attr("data-type").trim()
            if (link.isNotEmpty()) {
                videos.add(
                    Video(
                        url = episode.url,
                        quality = if (type == "m3u8") "HLS (m3u8)" else "AnimeHay Server",
                        videoUrl = link,
                    )
                )
            }
        }

        if (videos.isEmpty()) {
            val iframe = doc.selectFirst("iframe")
            if (iframe != null) {
                var src = iframe.attr("src").trim()
                if (src.startsWith("//")) {
                    src = "https:$src"
                }
                videos.add(
                    Video(
                        url = episode.url,
                        quality = "Default Embed",
                        videoUrl = src,
                    )
                )
            }
        }

        return videos
    }
}
