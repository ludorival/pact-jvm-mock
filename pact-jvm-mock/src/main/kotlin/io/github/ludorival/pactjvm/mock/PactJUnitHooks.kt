package io.github.ludorival.pactjvm.mock

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.reflect.full.staticProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.KClass

class PactJUnitHooks: BeforeEachCallback,AfterAllCallback {

    private fun setupPactOptions(value: Any?) {
        if (value is PactOptions) {
            PactMock.setPactOptions(value)
        }
    }

    override fun beforeEach(context: ExtensionContext) {
        PactMock.currentTestName = context.displayName.substringBeforeLast("(")
        
        val annotation = context.requiredTestClass.getAnnotation(PactConsumer::class.java) ?: error("PactConsumer annotation is expected")
        val configClass = annotation.value
        
        setupPactOptions(configClass.getPactOptions() ?: configClass.java.getPactOptions() ?: throw IllegalArgumentException("No valid pactOptions found in ${configClass.simpleName}. We expect a static field named pactOptions or a Kotlin object with a property named pactOptions."))
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

    override fun afterAll(context: ExtensionContext?) {
        PactMock.writePacts()
    }
}