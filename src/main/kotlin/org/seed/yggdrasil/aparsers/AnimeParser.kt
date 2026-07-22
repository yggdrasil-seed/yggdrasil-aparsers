package org.seed.yggdrasil.aparsers

import org.seed.yggdrasil.aparsers.model.Anime
import org.seed.yggdrasil.aparsers.model.AnimeListFilter
import org.seed.yggdrasil.aparsers.model.Episode
import org.seed.yggdrasil.aparsers.model.Video

public interface AnimeParser {
    public suspend fun getAnimeList(filter: AnimeListFilter): List<Anime>
    public suspend fun getAnimeDetails(anime: Anime): Anime
    public suspend fun getEpisodeList(anime: Anime): List<Episode>
    public suspend fun getVideoList(episode: Episode): List<Video>
}
