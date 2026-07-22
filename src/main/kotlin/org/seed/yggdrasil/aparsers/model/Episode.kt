package org.seed.yggdrasil.aparsers.model

public data class Episode(
    public val id: String,
    public val name: String,
    public val episodeNumber: Float,
    public val url: String,
    public val dateUpload: Long = 0L,
    public val scanlator: String? = null,
)
