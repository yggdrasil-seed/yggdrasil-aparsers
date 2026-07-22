package org.seed.yggdrasil.aparsers.model

public interface AnimeParserAuthProvider {
    public val isAuthorized: Boolean
    public suspend fun login(username: String, password: String): Boolean
    public suspend fun logout()
}
