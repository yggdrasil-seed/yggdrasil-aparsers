package org.seed.yggdrasil.aparsers

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.seed.yggdrasil.aparsers.model.AnimeParserSource

internal class AnimeParserTest {

    @Test
    fun testAnimeParserFactoryInstantiation() {
        val source = AnimeParserSource.valueOf("GOGOANIME")
        val parser = AnimeParserFactory.newParserInstance(source, AnimeLoaderContextMock)
        assertNotNull(parser, "AnimeParser instance should be successfully created by factory")
    }
}
