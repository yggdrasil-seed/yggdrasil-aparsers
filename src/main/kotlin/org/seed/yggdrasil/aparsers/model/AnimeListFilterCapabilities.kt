package org.seed.yggdrasil.aparsers.model

public data class AnimeListFilterCapabilities(
    public val isSearchSupported: Boolean = true,
    public val isGenreFilterSupported: Boolean = true,
)

public data class AnimeListFilterOptions(
    public val availableGenres: Set<String> = emptySet(),
)
