package io.github.ludorival.pactjvm.mockk

import io.mockk.Call

interface PactMockkAdapter {

    fun support(call: Call): Boolean

    fun <T> buildInteraction(call: Call, response: T): ConsumerInteraction

}
