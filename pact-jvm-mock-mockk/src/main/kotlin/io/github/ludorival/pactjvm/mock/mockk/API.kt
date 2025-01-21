package io.github.ludorival.pactjvm.mock.mockk

import io.github.ludorival.pactjvm.mock.Call as MockCall
import io.github.ludorival.pactjvm.mock.CallInterceptor
import io.github.ludorival.pactjvm.mock.InteractionBuilder
import io.mockk.*

fun <T> uponReceiving(stubBlock: MockKMatcherScope.() -> T) = PactMockKStubScope(every(stubBlock))

fun <T> uponCoReceiving(stubBlock: suspend MockKMatcherScope.() -> T) = PactMockKStubScope(coEvery(stubBlock))
fun <T> Answer<T>.interceptAndGet(
    it: Call,
    builder: InteractionBuilder<T>
): T {
    val result = runCatching { answer(it) }
    val call = it.invocation.let {
        MockCall(
            MockCall.Method(
                it.method.name,
                it.method.paramTypes.map { it.java }.toTypedArray()
            ),
            it.self,
            it.args,
            result
        )
    }
    builder.call = call
    return CallInterceptor.getInstance()
        .interceptAndGet(builder)
}