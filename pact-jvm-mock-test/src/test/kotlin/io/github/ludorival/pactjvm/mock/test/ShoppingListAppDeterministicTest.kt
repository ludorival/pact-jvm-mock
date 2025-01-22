package io.github.ludorival.pactjvm.mock.test

import io.github.ludorival.pactjvm.mock.EnablePactMock
import org.junit.jupiter.api.extension.ExtendWith

@EnablePactMock(DeterministicPact::class)
open class ShoppingListAppDeterministicTest : ShoppingListAppTest()
