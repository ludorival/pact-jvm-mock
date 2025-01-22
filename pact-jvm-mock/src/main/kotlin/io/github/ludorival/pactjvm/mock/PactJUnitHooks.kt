package io.github.ludorival.pactjvm.mock

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.reflect.full.staticProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.KClass

class PactJUnitHooks: BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {
    companion object {
        private const val NAMESPACE = "io.github.ludorival.pactjvm.mock"
        private const val PACT_CONFIG_KEY = "pactConfig"
    }

    private fun setupPactConfiguration(config: PactConfiguration, context: ExtensionContext) {
        PactMock.setPactConfiguration(config)
        
        // Store PactConfiguration in the class-level ExtensionContext
        context.root
            .getStore(ExtensionContext.Namespace.create(NAMESPACE))
            .put(PACT_CONFIG_KEY, config)
    }

    override fun beforeAll(context: ExtensionContext) {
        val annotation = context.requiredTestClass.getAnnotation(EnablePactMock::class.java) 
            ?: error("PactConsumer annotation is expected")
        val configClass = annotation.value
        
        val config = configClass.getConfiguration() ?: configClass.java.getConfiguration()
            ?: throw IllegalArgumentException("No valid PactConfiguration implementation found in ${configClass.simpleName}. The class must be a Kotlin object or Java class with a public constructor implementing PactConfiguration.")
        
        setupPactConfiguration(config, context)
    }

    override fun beforeEach(context: ExtensionContext) {
        // Set the current test info
        PactMock.currentTestInfo = TestInfo(
            testFileName = context.requiredTestClass.simpleName,
            methodName = context.requiredTestMethod.name,
            displayName = context.displayName.substringBeforeLast("(")
        )
    }

    private fun KClass<*>.getConfiguration(): PactConfiguration? {
        return objectInstance as? PactConfiguration
    }

    private fun Class<*>.getConfiguration(): PactConfiguration? {
        return try {
            // Find the first public constructor
            val constructor = declaredConstructors.firstOrNull { it.modifiers and java.lang.reflect.Modifier.PUBLIC != 0 }
                ?: return null
            constructor.newInstance() as? PactConfiguration
        } catch (e: Exception) {
            null
        }
    }

    override fun afterEach(context: ExtensionContext) {
        PactMock.currentTestInfo = null
    }

    override fun afterAll(context: ExtensionContext) {
        PactMock.writePacts()
    }
}