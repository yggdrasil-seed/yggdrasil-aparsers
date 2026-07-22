package org.seed.yggdrasil.aparsers

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class AnimeSourceParser(
    val nameKey: String,
    val title: String,
    val locale: String = "en",
)
