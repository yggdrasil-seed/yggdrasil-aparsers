package org.seed.yggdrasil.aparsers.model

public data class Favicon(
    public val url: String,
    public val size: Int = 0,
)

public data class Favicons(
    public val icons: List<Favicon> = emptyList(),
) {
    public fun getBestIconUrl(): String? = icons.maxByOrNull { it.size }?.url ?: icons.firstOrNull()?.url
}
