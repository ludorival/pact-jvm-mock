package io.github.ludorival.pactjvm.mock

import au.com.dius.pact.core.model.PactSpecVersion

abstract class PactConfiguration(val consumer: String, vararg adapters: PactMockAdapter<*>) {
    private val adapters: List<PactMockAdapter<*>> = adapters.toList()

    open fun getPactDirectory(): String = "./src/test/resources/pacts"

    open fun isDeterministic(): Boolean = false

    open fun getPactMetaData(): PactSpecVersion = PactSpecVersion.V3

    fun getAdapterFor(call: Call<*>) = adapters.find { it.support(call) }
} 
