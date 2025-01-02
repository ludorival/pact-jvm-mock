package io.github.ludorival.pactjvm.mockk

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.reflect.KClass

@Retention(RUNTIME)
@Target(CLASS)
@ExtendWith(PactJUnitHooks::class)
annotation class PactConsumer(val value: KClass<*>)