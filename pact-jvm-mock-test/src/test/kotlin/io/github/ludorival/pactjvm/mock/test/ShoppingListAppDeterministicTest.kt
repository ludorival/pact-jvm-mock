package io.github.ludorival.pactjvm.mock.test

import io.github.ludorival.pactjvm.mock.PactConsumer
import org.junit.jupiter.api.extension.ExtendWith

@PactConsumer(DeterministicPact::class)
open class ShoppingListAppDeterministicTest : ShoppingListAppTest()
