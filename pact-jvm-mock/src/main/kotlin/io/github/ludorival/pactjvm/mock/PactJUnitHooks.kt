package io.github.ludorival.pactjvm.mock

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.reflect.full.staticProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.KClass

class PactJUnitHooks: BeforeAllCallback, BeforeEachCallback, AfterAllCallback {
    companion object {
        private const val NAMESPACE = "io.github.ludorival.pactjvm.mock"
        private const val PACT_OPTIONS_KEY = "pactOptions"
    }

    private fun setupPactOptions(value: Any?, context: ExtensionContext) {
        if (value is PactOptions) {
            PactMock.setPactOptions(value)
            // Store PactOptions in the class-level ExtensionContext
            context.root
                .getStore(ExtensionContext.Namespace.create(NAMESPACE))
                .put(PACT_OPTIONS_KEY, value)
        }
    }

    override fun beforeAll(context: ExtensionContext) {
        val annotation = context.requiredTestClass.getAnnotation(PactConsumer::class.java) 
            ?: error("PactConsumer annotation is expected")
        val configClass = annotation.value
        
        
        setupPactOptions(
            configClass.getPactOptions() ?: configClass.java.getPactOptions() 
                ?: throw IllegalArgumentException("No valid pactOptions found in ${configClass.simpleName}. We expect a static field named pactOptions or a Kotlin object with a property named pactOptions."),
            context
        )
    }

    override fun beforeEach(context: ExtensionContext) {
        // Only set the current test name
        PactMock.currentTestName = context.displayName.substringBeforeLast("(")
    }

    private fun KClass<*>.getPactOptions(): PactOptions? {
        val kotlinObject = objectInstance
        val property = members.firstOrNull { it.name == "pactOptions" }
        return if (kotlinObject != null && property != null) {
            val value = property.call(kotlinObject)
            value as? PactOptions
        } else null
    }

    private fun Class<*>.getPactOptions(): PactOptions? {
        val field = getDeclaredField("pactOptions")
        field.isAccessible = true
        val value = field.get(null)
        return value as? PactOptions
    }

    override fun afterAll(context: ExtensionContext) {
        PactMock.writePacts()
    }
}