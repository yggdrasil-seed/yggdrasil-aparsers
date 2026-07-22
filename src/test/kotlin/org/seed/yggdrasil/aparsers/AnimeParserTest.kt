package org.seed.yggdrasil.aparsers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.seed.yggdrasil.aparsers.model.AnimeParserSource

internal class AnimeParserTest {

    @Test
    fun testAnimeParserFactoryInstantiation() {
        val parser = AnimeParserFactory.newParserInstance(AnimeParserSource.GOGOANIME, AnimeLoaderContextMock)
        assertNotNull(parser, "AnimeParser instance should be successfully created by factory")
        assertEquals(AnimeParserSource.GOGOANIME.nameKey, "gogoanime")
        assertEquals(AnimeParserSource.GOGOANIME.title, "Gogoanime")
    }
}
