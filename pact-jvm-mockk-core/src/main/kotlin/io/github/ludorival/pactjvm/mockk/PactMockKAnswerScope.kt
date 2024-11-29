package io.github.ludorival.pactjvm.mockk

import io.mockk.MockKAnswerScope

class PactMockKAnswerScope<T, B>(mockKAnswerScope: MockKAnswerScope<T, B>): InteractionBuilder by InteractionBuilderImpl() {

    val invocation = mockKAnswerScope.invocation
    val matcher = mockKAnswerScope.matcher

    val self
        get() = invocation.self

    val method
        get() = invocation.method

    val args
        get() = invocation.args

    val nArgs
        get() = invocation.args.size

    inline fun <reified T> firstArg() = invocation.args[0] as T
    inline fun <reified T> secondArg() = invocation.args[1] as T
    inline fun <reified T> thirdArg() = invocation.args[2] as T
    inline fun <reified T> lastArg() = invocation.args.last() as T
    inline fun <reified T> arg(n: Int) = invocation.args[n] as T

    val scope = mockKAnswerScope

    fun <E : Any> anError(response: E): Nothing = throw PactMockResponseError(response)

}
