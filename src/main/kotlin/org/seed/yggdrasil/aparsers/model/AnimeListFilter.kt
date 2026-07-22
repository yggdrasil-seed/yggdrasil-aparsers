package org.seed.yggdrasil.aparsers.model

public data class AnimeListFilter(
    public val query: String? = null,
    public val genres: Set<String> = emptySet(),
    public val status: AnimeStatus? = null,
    public val page: Int = 1,
)
