package org.seed.yggdrasil.aparsers.util

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

public fun Element.attrOrNull(key: String): String? = attr(key).takeUnless { it.isBlank() }?.trim()

public fun Element.textOrNull(): String? = text().takeUnless { it.isBlank() }?.trim()

public fun Elements.textOrNull(): String? = text().takeUnless { it.isBlank() }?.trim()

public fun Element.src(): String? {
    val attrs = arrayOf("data-src", "data-original", "data-lazy-src", "src")
    for (attr in attrs) {
        val value = attrOrNull(attr)
        if (value != null) return value
    }
    return null
}
