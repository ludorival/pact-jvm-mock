package io.github.ludorival.pactjvm.mock

import au.com.dius.pact.core.model.*
import au.com.dius.pact.core.support.Json
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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
            pactConfiguration.getPactMetaData()
        ),
        pactConfiguration.isDeterministic(),
        pactConfiguration.getPactDirectory()
    )

    init {
        if (consumer.isBlank()) error("The consumer should not be empty")
    }

    val id = "${consumer}-${providerMetaData.name}-${isDeterministic}"

    private val interactionsByDescription = ConcurrentHashMap<String, Interaction>()

    private val descriptions = mutableListOf<String>()
    private val diffMatchPatch = DiffMatchPatch()


    val pact: Pact
        get() = createPact(
            consumer,
            providerMetaData,
            descriptions.map { interactionsByDescription.getValue(it) }
        )

    fun addInteraction(interaction: Interaction): PactToWrite {
        val existing = interactionsByDescription[interaction.description]
        val hasChanged = existing != null && interaction != existing
        val countInteractions =
            if (hasChanged && !isDeterministic) interactionsByDescription.keys.count { it.startsWith(interaction.description) } else 0
        return when {
            hasChanged && isDeterministic -> {
                throw IllegalStateException(printDifferences(existing!!, interaction))
            }

            hasChanged -> {
                val newDescription = "${interaction.description} - $countInteractions"
                interaction.description = newDescription
                addInteraction(interaction).also {
                    LOGGER.warn {  printDifferences(existing!!, interaction) }
                    LOGGER.warn { "New description title has been generated : $RED$newDescription$RESET" }
                }
            }

            existing != null -> this
            else -> {
                interactionsByDescription[interaction.description] = interaction
                descriptions.add(interaction.description)
                this
            }
        }
    }

    private fun createPact(
        consumer: String,
        providerMetaData: ProviderMetaData,
        interactions: List<Interaction>
    ): Pact =
        RequestResponsePact(
            consumer = Consumer(consumer),
            provider = Provider(providerMetaData.name),
            interactions = interactions.toMutableList(),
            source = UnknownPactSource,
            metadata = BasePact.metaData(null, providerMetaData.pactMetaData)
        )

    private fun printDifferences(old: Interaction, current: Interaction): String {
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
        val previousProperty = System.getProperty("pact.writer.overwrite") ?: "false"
        System.setProperty("pact.writer.overwrite", "true")
        val pactDirectory = File(outputDirectory, pactFile).toString()
        val result = pact.write(outputDirectory, providerMetaData.pactMetaData)
        System.setProperty("pact.writer.overwrite", previousProperty)
        if (result.errorValue() == null)
            LOGGER.info {  "[$id] Successfully written pact to $pactDirectory" }

    }


    private fun Interaction.toPrettyJson(): String =
        Json.prettyPrint(this)

    companion object {
        private const val RESET = "\u001B[0m"
        private const val GREEN = "\u001B[32m"
        private const val RED = "\u001B[31m"

        fun green(s: String): String = "$GREEN$s$RESET"
        fun red(s: String): String = "$RED$s$RESET"
    }
}
