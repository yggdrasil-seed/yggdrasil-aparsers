package org.seed.yggdrasil.aparsers.model

public data class Video(
    public val url: String,
    public val quality: String,
    public val videoUrl: String,
    public val headers: Map<String, String> = emptyMap(),
    public val subtitleTracks: List<Track> = emptyList(),
    public val audioTracks: List<Track> = emptyList(),
)
