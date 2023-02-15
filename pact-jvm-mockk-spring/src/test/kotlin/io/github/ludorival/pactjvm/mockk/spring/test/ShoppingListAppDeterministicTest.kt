package io.github.ludorival.pactjvm.mockk.spring.test

import io.github.ludorival.pactjvm.mockk.spring.DeterministicPact
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(DeterministicPact::class)
open class ShoppingListAppDeterministicTest : ShoppingListAppTest()
