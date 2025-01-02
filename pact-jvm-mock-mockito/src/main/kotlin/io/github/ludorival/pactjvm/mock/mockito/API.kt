package io.github.ludorival.pactjvm.mock.mockito

import io.github.ludorival.pactjvm.mock.Call as MockCall
import io.github.ludorival.pactjvm.mock.CallInterceptor
import io.github.ludorival.pactjvm.mock.InteractionBuilder
import io.github.ludorival.pactjvm.mock.InteractionBuilderImpl
import io.github.ludorival.pactjvm.mock.Pact
import io.github.ludorival.pactjvm.mock.PactMockResponseError
import org.mockito.stubbing.Answer
import org.mockito.stubbing.OngoingStubbing

class MockitoAnswerScope<T> : InteractionBuilder by InteractionBuilderImpl() {
    fun <E : Any> anError(response: E): Nothing = throw PactMockResponseError(response)
}

infix fun <T> OngoingStubbing<T>.willRespondWith(
    answer: Answer<T>
): OngoingStubbing<T> {
    return thenAnswer { invocation ->
        val result = runCatching { answer.answer(invocation) }
        val scope = MockitoAnswerScope<T>()
        CallInterceptor.getInstance()
            .interceptAndGet(
                MockCall(
                    MockCall.Method(
                        invocation.method.name,
                        invocation.method.parameterTypes
                    ),
                    invocation.mock,
                    invocation.arguments.toList()
                ),
                result,
                scope
            )
    }
}

infix fun <T> OngoingStubbing<T>.willRespondWith(
    answer: (org.mockito.invocation.InvocationOnMock) -> T
): OngoingStubbing<T> {
    return willRespondWith(Answer { invocation -> answer(invocation) })
}

infix fun <T> OngoingStubbing<T>.willRespond(
    returnValue: T
): OngoingStubbing<T> {
    return thenAnswer { invocation ->
        val result = Result.success(returnValue)
        val scope = MockitoAnswerScope<T>()
        CallInterceptor.getInstance()
            .interceptAndGet<T>(
                MockCall(
                    MockCall.Method(
                        invocation.method.name,
                        invocation.method.parameterTypes
                    ),
                    invocation.mock,
                    invocation.arguments.toList()
                ),
                result,
                scope
            )
    }
} 