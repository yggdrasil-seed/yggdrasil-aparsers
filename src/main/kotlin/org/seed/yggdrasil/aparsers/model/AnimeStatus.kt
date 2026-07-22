package org.seed.yggdrasil.aparsers.model

public enum class AnimeStatus {
    ONGOING,
    COMPLETED,
    UPCOMING,
    UNKNOWN;

    public companion object {
        public fun fromString(status: String?): AnimeStatus {
            if (status == null) return UNKNOWN
            val s = status.lowercase()
            return when {
                s.contains("ongoing") || s.contains("airing") -> ONGOING
                s.contains("completed") || s.contains("finished") -> COMPLETED
                s.contains("upcoming") || s.contains("unreleased") -> UPCOMING
                else -> UNKNOWN
            }
        }
    }
}
