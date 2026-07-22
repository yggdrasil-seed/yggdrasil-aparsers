package org.seed.yggdrasil.aparsers.model

public enum class SortOrder {
    UPDATED,
    POPULARITY,
    ALPHABETICAL,
    RATING,
    NEWEST;

    public companion object {
        public val DEFAULT: SortOrder = POPULARITY
    }
}
