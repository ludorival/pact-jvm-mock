package io.github.ludorival.pactjvm.mockk.spring

import io.github.ludorival.pactjvm.mockk.PactMockk
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

abstract class SpringPactMockkExtension(
    provider: String,
    determineConsumerFromUrl: DetermineConsumerFromUrl = DEFAULT_CONSUMER_DETERMINER
) : BeforeEachCallback, AfterEachCallback,
    AfterAllCallback {

    init {
        PactMockk.provider = provider
        PactMockk.addAdapter(SpringRestTemplateMockkAdapter(determineConsumerFromUrl))
    }

    override fun beforeEach(context: ExtensionContext?) {
        // TODO Do we need to do something before the test begins
    }

    override fun afterEach(context: ExtensionContext?) {
        // TODO Do we need to do something when the test is completed
    }

    override fun afterAll(context: ExtensionContext?) {
        PactMockk.writePacts()
    }

}
