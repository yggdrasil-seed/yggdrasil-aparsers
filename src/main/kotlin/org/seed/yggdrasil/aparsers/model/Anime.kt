package org.seed.yggdrasil.aparsers.model

public data class Anime(
    public val id: String,
    public val title: String,
    public val altTitle: String? = null,
    public val url: String,
    public val coverUrl: String? = null,
    public val description: String? = null,
    public val status: AnimeStatus = AnimeStatus.UNKNOWN,
    public val genres: Set<String> = emptySet(),
    public val studio: String? = null,
    public val nsfw: Boolean = false,
)
