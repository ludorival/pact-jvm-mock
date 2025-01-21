package io.github.ludorival.pactjvm.mock

interface CallInterceptor {

    fun <T> interceptAndGet(interactionBuilder: InteractionBuilder<T>): T

    companion object {
        fun getInstance(): CallInterceptor = PactMock
    }
}
