package io.github.ludorival.pactjvm.mockk.spring

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

abstract class SpringPactMockkExtension(private val provider: String) : BeforeEachCallback, AfterEachCallback,
    AfterAllCallback {
    override fun beforeEach(context: ExtensionContext?) {
        // TODO Do we need to do something before the test begins
    }

    override fun afterEach(context: ExtensionContext?) {
        // TODO Do we need to do something when the test is completed
    }

    override fun afterAll(context: ExtensionContext?) {
        TODO("Here we should write the contracts")
    }
}
