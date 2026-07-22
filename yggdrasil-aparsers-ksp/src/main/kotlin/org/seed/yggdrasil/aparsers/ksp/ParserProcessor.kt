package org.seed.yggdrasil.aparsers.ksp

import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import java.io.Writer

public class ParserProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("org.seed.yggdrasil.aparsers.AnimeSourceParser")
        val ret = symbols.filterNot { it.validate() }.toList()
        if (!symbols.iterator().hasNext()) {
            return ret
        }

        val dependencies = Dependencies.ALL_FILES

        val factoryFile = try {
            codeGenerator.createNewFile(
                dependencies = dependencies,
                packageName = "org.seed.yggdrasil.aparsers",
                fileName = "AnimeParserFactory",
            )
        } catch (e: Exception) {
            null
        }

        val sourcesFile = try {
            codeGenerator.createNewFile(
                dependencies = dependencies,
                packageName = "org.seed.yggdrasil.aparsers.model",
                fileName = "AnimeParserSource",
            )
        } catch (e: Exception) {
            null
        }

        sourcesFile?.writer().use { sourcesWriter ->
            factoryFile?.writer().use { factoryWriter ->
                writeContent(sourcesWriter, factoryWriter, symbols)
            }
        }

        return ret
    }

    private fun writeContent(
        sourcesWriter: Writer?,
        factoryWriter: Writer?,
        symbols: Sequence<KSAnnotated>,
    ) {
        factoryWriter?.write(
            """
            package org.seed.yggdrasil.aparsers

            import org.seed.yggdrasil.aparsers.model.AnimeParserSource

            public object AnimeParserFactory {
                public fun newParserInstance(source: AnimeParserSource, context: AnimeLoaderContext): AnimeParser = when (source) {

            """.trimIndent()
        )

        sourcesWriter?.write(
            """
            package org.seed.yggdrasil.aparsers.model

            public enum class AnimeParserSource(
                public val nameKey: String,
                public val title: String,
                public val locale: String,
            ) {

            """.trimIndent()
        )

        val visitor = ParserVisitor(sourcesWriter, factoryWriter)
        symbols
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }
            .forEach { it.accept(visitor, Unit) }

        factoryWriter?.write(
            """
                else -> error("Unknown source: ${'$'}source")
                }
            }
            """.trimIndent()
        )

        sourcesWriter?.write(
            """
                ;
            }
            """.trimIndent()
        )
    }

    private inner class ParserVisitor(
        private val sourcesWriter: Writer?,
        private val factoryWriter: Writer?,
    ) : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            if (classDeclaration.classKind != ClassKind.CLASS || classDeclaration.isAbstract()) {
                logger.error("Only non-abstract class can be annotated with @AnimeSourceParser", classDeclaration)
            }

            val annotation = classDeclaration.annotations.single { it.shortName.asString() == "AnimeSourceParser" }
            val nameKey = annotation.arguments.single { it.name?.asString() == "nameKey" }.value as String
            val title = annotation.arguments.single { it.name?.asString() == "title" }.value as String
            val locale = annotation.arguments.single { it.name?.asString() == "locale" }.value as String

            val enumName = nameKey.uppercase()
            val className = checkNotNull(classDeclaration.qualifiedName?.asString())

            factoryWriter?.write("        AnimeParserSource.$enumName -> $className(context)\n")
            sourcesWriter?.write("    $enumName(\"$nameKey\", \"$title\", \"$locale\"),\n")
        }
    }
}
