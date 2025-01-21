package io.github.ludorival.pactjvm.mock

import au.com.dius.pact.core.model.ProviderState

class ProviderStateBuilder(val call: Call<*>) {
        private val providerStates = mutableListOf<ProviderState>()

        fun state(state: String, params: Map<String, Any?>? = null): ProviderStateBuilder = apply {
            providerStates.add(ProviderState(state, params ?: emptyMap()))
        }

        internal fun get(): List<ProviderState> = providerStates.toList()
    }