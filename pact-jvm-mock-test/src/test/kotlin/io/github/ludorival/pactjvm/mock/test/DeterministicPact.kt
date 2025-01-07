package io.github.ludorival.pactjvm.mock.test

import io.github.ludorival.pactjvm.mock.PactConfiguration
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockAdapter
import com.fasterxml.jackson.databind.ObjectMapper

object DeterministicPact : PactConfiguration("shopping-list", SpringRestTemplateMockAdapter()) {
    override fun getPactDirectory(): String = "./src/test/resources/pacts-deterministic"
    override fun isDeterministic(): Boolean = true
    
    override fun customizeObjectMapper(providerName: String): ObjectMapper? = 
        NonDeterministicPact.customizeObjectMapper(providerName)
}
