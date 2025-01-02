package io.github.ludorival.pactjvm.mock.spring.test

import io.github.ludorival.pactjvm.mock.spring.DeterministicPact
import io.github.ludorival.pactjvm.mock.PactConsumer
import org.junit.jupiter.api.extension.ExtendWith

@PactConsumer(DeterministicPact::class)
open class ShoppingListAppDeterministicTest : ShoppingListAppTest()
