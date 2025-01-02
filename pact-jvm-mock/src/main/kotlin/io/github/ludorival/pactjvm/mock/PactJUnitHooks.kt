package io.github.ludorival.pactjvm.mock

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.reflect.full.staticProperties
import kotlin.reflect.full.memberProperties


class PactJUnitHooks: BeforeEachCallback,AfterAllCallback {

    override fun beforeEach(context: ExtensionContext) {
        val annotation = context.requiredTestClass.getAnnotation(PactConsumer::class.java) ?: error("PactConsumer annotation is expected")
        val configClass = annotation.value
        val options = (configClass.staticProperties + configClass.memberProperties)
            .firstOrNull { it.name == "pactOptions" }
            ?.getter
            ?.call(configClass.objectInstance) as? PactOptions
            ?: throw IllegalArgumentException("No static field 'pactOptions' found in ${configClass.simpleName}")
        PactMock.setPactOptions(options)
        PactMock.currentTestName = context.displayName.substringBeforeLast("(")
    }

    override fun afterAll(context: ExtensionContext?) {
        PactMock.writePacts()
    }
}