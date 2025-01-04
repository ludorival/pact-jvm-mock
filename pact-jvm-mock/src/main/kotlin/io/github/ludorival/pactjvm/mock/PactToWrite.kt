package io.github.ludorival.pactjvm.mock

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch

internal data class PactToWrite(
        val consumer: String,
        val providerMetaData: ProviderMetaData,
        private val isDeterministic: Boolean = false,
        private val outputDirectory: String
) {

    constructor(
            providerName: String,
            pactConfiguration: PactConfiguration
    ) : this(
            pactConfiguration.consumer,
            ProviderMetaData(
                    providerName,
                    pactConfiguration.customizeObjectMapper(providerName),
                    pactConfiguration.getPactMetaData()
            ),
            pactConfiguration.isDeterministic(),
            pactConfiguration.getPactDirectory()
    )
    init {
        if (consumer.isBlank()) error("The consumer should not be empty")
    }

    val id = "${consumer}-${providerMetaData.name}-${isDeterministic}"
    
    private val interactionsByDescription = ConcurrentHashMap<String, Pact.Interaction>()

    private val descriptions = mutableListOf<String>()
    private val diffMatchPatch = DiffMatchPatch()

    val objectMapper
        get() = providerMetaData.customObjectMapper ?: PACT_OBJECT_MAPPER

    val pact: Pact
        get() = Pact(
            consumer,
            providerMetaData,
            descriptions.map { interactionsByDescription.getValue(it) }
    )

    fun addInteraction(interaction: Pact.Interaction): PactToWrite {
        val existing = interactionsByDescription[interaction.description]
        val hasChanged = existing != null && interaction != existing
        return when {
            hasChanged && isDeterministic -> {
                throw IllegalStateException(printDifferences(existing!!, interaction))
            }
            hasChanged ->
                    addInteraction(
                                    interaction.copy(
                                            description =
                                                    "${interaction.description} - ${interactionsByDescription.keys.count { it.startsWith(interaction.description) }}"
                                    )
                            )
                            .also {
                                println(printDifferences(existing!!, interaction))
                                println(
                                        "New description title has been generated : " +
                                                "$RED${interaction.description} - ${interaction.hashCode()}$RESET"
                                )
                            }
            existing != null -> this
            else -> {
                interactionsByDescription[interaction.description] = interaction
                descriptions.add(interaction.description)
                this
            }
        }
    }

    private fun printDifferences(old: Pact.Interaction, current: Pact.Interaction): String {

        val diffs = diffMatchPatch.diffMain(old.toPrettyJson(), current.toPrettyJson())
        return """The interaction with description "${current.description}" has changed
                    |The changes are:
                    |${toReadableConsoleMessage(current.description, diffs)}
                    |The Pact contract should be deterministic.
                    |Possible solutions:
                    |1) Force the Pact to be deterministic
                    |PactConfiguration {
                    |  consumer = "..."
                    |  addAdapter(...)
                    |  isDeterministic = true // <-- set to true 
                    |}
                    |2) Set a description for each conflicted interaction
                    |every { ... } willRespondWith {
                    |   options {
                    |    description = "This is different call"
                    |   }
                    |}
                    |====================================================
                """.trimMargin()
    }

    private fun printDiff(diff: DiffMatchPatch.Diff): String =
            when (diff.operation) {
                DiffMatchPatch.Operation.EQUAL, null -> " ${diff.text}\n"
                DiffMatchPatch.Operation.DELETE -> green("- ${diff.text}\n")
                DiffMatchPatch.Operation.INSERT -> red("+ ${diff.text}\n")
            }

    private fun toReadableConsoleMessage(
            description: String,
            diffs: LinkedList<DiffMatchPatch.Diff>
    ): String {
        val sb =
                StringBuilder(
                        "${red("Received interaction")} does not match ${
                green("captured interaction: \"$description\"")
            }\n\n${
                green("-Captured")
            }\n${red("+Received")}\n\n"
                )

        diffMatchPatch.diffCleanupSemantic(diffs)
        diffs.forEach { diff -> sb.append(printDiff(diff)) }
        return sb.toString()
    }

    private val pactFile
        get() = "${consumer}-${providerMetaData.name}.json"
    internal fun write() {
        if (descriptions.isEmpty()) return
        File(outputDirectory, pactFile).apply {
            val path = Paths.get(parentFile.path)
            if (!exists()) {
                Files.createDirectories(path)
            }
            val pact =
                    Pact(
                            consumer,
                            providerMetaData,
                            descriptions.map { interactionsByDescription.getValue(it) }
                    )
            writeText(pact.toPrettyJson())
            println("[$id] Successfully written pact to ${this.path}")
        }
    }

    private fun Any.toPrettyJson() =
            PACT_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this)

    companion object {
        private const val RESET = "\u001B[0m"
        private const val GREEN = "\u001B[32m"
        private const val RED = "\u001B[31m"

        fun green(s: String): String = "$GREEN$s$RESET"
        fun red(s: String): String = "$RED$s$RESET"

        private val PACT_OBJECT_MAPPER =
                ObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
    }
}
