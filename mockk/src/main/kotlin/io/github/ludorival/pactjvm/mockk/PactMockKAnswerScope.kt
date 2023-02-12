package io.github.ludorival.pactjvm.mockk

import io.mockk.MockKAnswerScope

class PactMockKAnswerScope<T, B>(private val mockKAnswerScope: MockKAnswerScope<T, B>) {

    var description: String = ""

    var providerStates: List<String>? = null

    val invocation = mockKAnswerScope.invocation
    val method
        get() = mockKAnswerScope.method

    val args
        get() = mockKAnswerScope.args

    val scope = mockKAnswerScope

    inline fun <reified T> arg(n: Int) = invocation.args[n] as T
}
