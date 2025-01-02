package io.github.ludorival.pactjvm.mock

interface CallInterceptor {

    fun <T> interceptAndGet(call: Call, response: Result<T>, interactionBuilder: InteractionBuilder): T

    companion object {
        fun getInstance(): CallInterceptor = PactMock
    }
}
