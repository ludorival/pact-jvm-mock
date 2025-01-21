package io.github.ludorival.pactjvm.mock.test

import io.github.ludorival.pactjvm.mock.PactConfiguration
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockAdapter
import com.fasterxml.jackson.databind.ObjectMapper

object DeterministicPact : PactConfiguration("shopping-list", SpringRestTemplateMockAdapter(ObjectMapperConfig::by)) {
    override fun getPactDirectory(): String = "./src/test/resources/pacts-deterministic"
    override fun isDeterministic(): Boolean = true

}
