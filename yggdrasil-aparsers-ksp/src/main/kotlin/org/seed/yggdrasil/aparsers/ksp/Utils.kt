package org.seed.yggdrasil.aparsers.ksp

import com.google.devtools.ksp.symbol.KSAnnotation

internal fun KSAnnotation.getArgumentValue(name: String): Any? {
    return arguments.find { it.name?.asString() == name }?.value
}
