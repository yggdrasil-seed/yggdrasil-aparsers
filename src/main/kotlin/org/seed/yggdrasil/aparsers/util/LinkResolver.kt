package org.seed.yggdrasil.aparsers.util

import okhttp3.HttpUrl
import org.seed.yggdrasil.aparsers.model.Anime

public class LinkResolver {
    public fun isMatchingDomain(link: HttpUrl, domain: String): Boolean {
        return link.host.equals(domain, ignoreCase = true) || link.host.endsWith(".$domain", ignoreCase = true)
    }
}
