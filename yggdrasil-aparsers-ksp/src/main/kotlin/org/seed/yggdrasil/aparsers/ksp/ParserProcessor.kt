package org.seed.yggdrasil.aparsers.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

public class ParserProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    private var isProcessed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (isProcessed) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation("org.seed.yggdrasil.aparsers.AnimeSourceParser")
        val classes = symbols.filterIsInstance<KSClassDeclaration>().toList()

        if (classes.isNotEmpty()) {
            generateSourceEnum(classes)
            generateFactory(classes)
            isProcessed = true
        }

        return emptyList()
    }

    private fun generateSourceEnum(classes: List<KSClassDeclaration>) {
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false, *classes.mapNotNull { it.containingFile }.toTypedArray()),
            packageName = "org.seed.yggdrasil.aparsers.model",
            fileName = "AnimeParserSource",
        )

        file.writer().use { writer ->
            writer.write("""
                package org.seed.yggdrasil.aparsers.model

                public enum class AnimeParserSource(
                    public val nameKey: String,
                    public val title: String,
                    public val locale: String,
                ) {
            """.trimIndent())
            writer.write("\n")

            classes.forEachIndexed { index, cls ->
                val annotation = cls.annotations.first { it.shortName.asString() == "AnimeSourceParser" }
                val nameKey = annotation.getArgumentValue("nameKey") as String
                val title = annotation.getArgumentValue("title") as String
                val locale = annotation.getArgumentValue("locale") as String
                val enumName = nameKey.uppercase()

                val comma = if (index < classes.size - 1) "," else ";"
                writer.write("    $enumName(\"$nameKey\", \"$title\", \"$locale\")$comma\n")
            }

            writer.write("}\n")
        }
    }

    private fun generateFactory(classes: List<KSClassDeclaration>) {
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false, *classes.mapNotNull { it.containingFile }.toTypedArray()),
            packageName = "org.seed.yggdrasil.aparsers",
            fileName = "AnimeParserFactory",
        )

        file.writer().use { writer ->
            writer.write("""
                package org.seed.yggdrasil.aparsers

                import org.seed.yggdrasil.aparsers.model.AnimeParserSource

                public object AnimeParserFactory {
                    public fun newParserInstance(source: AnimeParserSource, context: AnimeLoaderContext): AnimeParser {
                        return when (source) {
            """.trimIndent())
            writer.write("\n")

            classes.forEach { cls ->
                val annotation = cls.annotations.first { it.shortName.asString() == "AnimeSourceParser" }
                val nameKey = annotation.getArgumentValue("nameKey") as String
                val enumName = nameKey.uppercase()
                val className = cls.qualifiedName?.asString() ?: return@forEach

                writer.write("            AnimeParserSource.$enumName -> $className(context)\n")
            }

            writer.write("""
                        }
                    }
                }
            """.trimIndent())
            writer.write("\n")
        }
    }
}
