package io.github.ludorival.pactjvm.mock.test

import io.github.ludorival.pactjvm.mock.PactConfiguration
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockAdapter

object DeterministicPact : PactConfiguration(SpringRestTemplateMockAdapter("shopping-webapp", ProviderJsonSerializers::by)) {
    override fun getPactDirectory(): String = "./src/test/resources/pacts-deterministic"
    override fun isDeterministic(): Boolean = true

}
