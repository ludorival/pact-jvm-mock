package io.github.ludorival.pactjvm.mockk.spring.test

import io.github.ludorival.pactjvm.mockk.spring.DeterministicPact
import io.github.ludorival.pactjvm.mockk.PactConsumer
import org.junit.jupiter.api.extension.ExtendWith

@PactConsumer(DeterministicPact::class)
open class ShoppingListAppDeterministicTest : ShoppingListAppTest()
