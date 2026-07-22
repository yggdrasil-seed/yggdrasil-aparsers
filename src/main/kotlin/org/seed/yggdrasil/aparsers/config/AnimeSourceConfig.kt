package org.seed.yggdrasil.aparsers.config

public interface AnimeSourceConfig {
    public operator fun <T> get(key: ConfigKey<T>): T
}
