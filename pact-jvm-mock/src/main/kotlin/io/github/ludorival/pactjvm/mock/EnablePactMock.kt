package io.github.ludorival.pactjvm.mock

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.reflect.KClass

@Retention(RUNTIME)
@Target(CLASS)
@ExtendWith(PactJUnitHooks::class)
annotation class EnablePactMock(val value: KClass<out PactConfiguration>)
@Deprecated("Use EnablePactMock instead")
typealias PactConsumer = EnablePactMock