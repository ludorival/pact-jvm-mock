package io.github.ludorival.pactjvm.mockk

import io.github.ludorival.pactjvm.mockk.Pact.Companion.writePacts
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

object PactExtension : AfterAllCallback, ParameterResolver {

    override fun afterAll(context: ExtensionContext) {
        context.writePacts()
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.type == Pact::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return Pact(extensionContext)
    }
}
