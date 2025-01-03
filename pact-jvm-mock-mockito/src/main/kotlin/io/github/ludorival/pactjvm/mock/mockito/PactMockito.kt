package io.github.ludorival.pactjvm.mock.mockito

import io.github.ludorival.pactjvm.mock.Call as MockCall
import io.github.ludorival.pactjvm.mock.CallInterceptor
import io.github.ludorival.pactjvm.mock.InteractionBuilder
import io.github.ludorival.pactjvm.mock.InteractionBuilderImpl
import io.github.ludorival.pactjvm.mock.Pact
import io.github.ludorival.pactjvm.mock.PactMockResponseError
import io.github.ludorival.pactjvm.mock.MatchingRulesBuilder
import org.mockito.internal.exceptions.Reporter.notAnException
import org.mockito.internal.progress.ThreadSafeMockingProgress.mockingProgress
import org.mockito.internal.stubbing.BaseStubbing
import org.mockito.internal.stubbing.answers.CallsRealMethods;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.internal.stubbing.answers.ThrowsExceptionForClassType;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.Mockito;

class MockitoAnswerScope<T>(private val ongoingStubbing: OngoingStubbing<T>) : InteractionBuilder by InteractionBuilderImpl() {
    fun <E : Any> anError(response: E): Nothing = throw PactMockResponseError(response)
}

class PactMockitoOngoingStubbing<T>(private val ongoingStubbing: OngoingStubbing<T>) : OngoingStubbing<T> by ongoingStubbing {

    private val interactionBuilder = InteractionBuilderImpl()
    fun withDescription(description: String): PactMockitoOngoingStubbing<T> {
        interactionBuilder.description(description)
        return this
    }

    fun withProviderState(providerState: String, providerStateParameters: Map<String, Any>): PactMockitoOngoingStubbing<T> {
        interactionBuilder.providerState(providerState, providerStateParameters)
        return this
    }

    fun withRequestMatchingRules(block: MatchingRulesBuilder.() -> Unit): PactMockitoOngoingStubbing<T> = apply {
        interactionBuilder.requestMatchingRules(block)
    }

    fun withResponseMatchingRules(block: MatchingRulesBuilder.() -> Unit): PactMockitoOngoingStubbing<T> = apply {
        interactionBuilder.responseMatchingRules(block)
    }

    override fun thenAnswer(answer: Answer<*>): OngoingStubbing<T> {
        return ongoingStubbing.thenAnswer { invocation ->
            val result = runCatching { answer.answer(invocation) }
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
                        interactionBuilder
                )
        }
    }

    override fun then(answer: Answer<*>): OngoingStubbing<T> = thenAnswer(answer)

    override fun thenReturn(value: T): OngoingStubbing<T> = thenAnswer(Returns(value))

    override fun thenReturn(value: T, vararg values: T): OngoingStubbing<T> {
        var stubbing = thenReturn(value)
        for (v in values) {
            stubbing = stubbing.thenReturn(v)
        }
        return stubbing
    }

    private fun thenThrow(throwable: Throwable): OngoingStubbing<T> =
        thenAnswer(ThrowsException(throwable))

    override fun thenThrow(vararg throwables: Throwable): OngoingStubbing<T> {
        var stubbing: OngoingStubbing<T>? = null
        for (t in throwables) {
            stubbing = if (stubbing == null) thenThrow(t) else stubbing.thenThrow(t)
        }
        return stubbing ?: error("No exception thrown")
    }

    override fun thenThrow(throwableType: Class<out Throwable>): OngoingStubbing<T> {
        return thenAnswer(ThrowsExceptionForClassType(throwableType))
    }

    override fun thenThrow(
        toBeThrown: Class<out Throwable>,
        vararg nextToBeThrown: Class<out Throwable>
    ): OngoingStubbing<T> {
        var stubbing = thenThrow(toBeThrown)
        for (t in nextToBeThrown) {
            stubbing = stubbing.thenThrow(t)
        }
        return stubbing
    }

}

object PactMockito {

    @JvmStatic
    fun <T> uponReceiving(method: T): PactMockitoOngoingStubbing<T> {
        return PactMockitoOngoingStubbing(Mockito.`when`(method))
    }

}

fun <T> uponReceiving(ongoingStubbing: OngoingStubbing<T>) = PactMockito.uponReceiving(ongoingStubbing)


// infix fun <T> OngoingStubbing<T>.willRespondWith(
//     answer: Answer<T>
// ): OngoingStubbing<T> {
//     return thenAnswer { invocation ->
//         val result = runCatching { answer.answer(invocation) }
//         val scope = MockitoAnswerScope<T>(this)
//         CallInterceptor.getInstance()
//             .interceptAndGet(
//                 MockCall(
//                     MockCall.Method(
//                         invocation.method.name,
//                         invocation.method.parameterTypes
//                     ),
//                     invocation.mock,
//                     invocation.arguments.toList()
//                 ),
//                 result,
//                 scope
//             )
//     }
// }

// infix fun <T> OngoingStubbing<T>.willRespondWith(
//     answer: (org.mockito.invocation.InvocationOnMock) -> T
// ): OngoingStubbing<T> {
//     return willRespondWith(Answer { invocation -> answer(invocation) })
// }

// infix fun <T> OngoingStubbing<T>.willRespond(
//     returnValue: T
// ): OngoingStubbing<T> {
//     return willRespondWith(Answer { returnValue })
// }