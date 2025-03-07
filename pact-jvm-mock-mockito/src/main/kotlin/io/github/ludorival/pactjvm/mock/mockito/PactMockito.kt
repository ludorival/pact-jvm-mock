package io.github.ludorival.pactjvm.mock.mockito

import io.github.ludorival.pactjvm.mock.*
import io.github.ludorival.pactjvm.mock.Call as MockCall
import au.com.dius.pact.core.model.RequestResponseInteraction
import io.github.ludorival.pactjvm.mock.Call
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
import java.util.function.BiFunction
import java.util.function.Function

class PactMockitoOngoingStubbing<T>(private val ongoingStubbing: OngoingStubbing<T>) :
    OngoingStubbing<T> by ongoingStubbing {

    private val interactionBuilder = InteractionBuilder<T>()

    fun withDescription(description: String): PactMockitoOngoingStubbing<T> {
        interactionBuilder.description { description }
        return this
    }

    fun withDescription(descriptionFunction: Function<InteractionBuilder<T>, String>): PactMockitoOngoingStubbing<T> = apply {
        interactionBuilder.description { descriptionFunction.apply(interactionBuilder ) }
    }

    fun given(stateFunction: Function<ProviderStateBuilder, ProviderStateBuilder>): PactMockitoOngoingStubbing<T> {
        interactionBuilder.providerState {
            stateFunction.apply(this)
        }
        return this
    }

    fun matchingRequest(rulesFunction: Function<MatchingRulesBuilder, MatchingRulesBuilder>): PactMockitoOngoingStubbing<T> = apply {
        interactionBuilder.requestMatchingRules {
            rulesFunction.apply(this)
        }
    }

    fun matchingResponse(rulesFunction: Function<MatchingRulesBuilder, MatchingRulesBuilder>): PactMockitoOngoingStubbing<T> = apply {
        interactionBuilder.responseMatchingRules {
            rulesFunction.apply(this)
        }
    }

    fun andThenAnswer(answer: Answer<*>): PactMockitoOngoingStubbing<T> {
        return PactMockitoOngoingStubbing(ongoingStubbing.thenAnswer { invocation ->
            val result = runCatching { answer.answer(invocation) } as Result<T>
            val call = MockCall(
                MockCall.Method(
                    invocation.method.name,
                    invocation.method.parameterTypes
                ),
                invocation.mock,
                invocation.arguments.toList(),
                result
            )
            interactionBuilder.call = call
            CallInterceptor.getInstance()
                .interceptAndGet(interactionBuilder)
        })
    }

    override fun thenAnswer(answer: Answer<*>): OngoingStubbing<T> = andThenAnswer(answer)

    fun andThen(answer: Answer<*>): PactMockitoOngoingStubbing<T> = andThenAnswer(answer)

    override fun then(answer: Answer<*>): OngoingStubbing<T> = andThenAnswer(answer)

    fun andThenReturn(value: T): PactMockitoOngoingStubbing<T> = andThenAnswer(Returns(value))

    override fun thenReturn(value: T): OngoingStubbing<T> = andThenAnswer(Returns(value))

    override fun thenReturn(value: T, vararg values: T): OngoingStubbing<T> {
        var stubbing = andThenReturn(value)
        for (v in values) {
            stubbing = stubbing.andThenReturn(v)
        }
        return stubbing
    }

    fun andThenThrow(throwable: Throwable): PactMockitoOngoingStubbing<T> = andThenAnswer(ThrowsException(throwable))

    override fun thenThrow(vararg throwables: Throwable): OngoingStubbing<T> {
        var stubbing: PactMockitoOngoingStubbing<T>? = null
        for (t in throwables) {
            stubbing = if (stubbing == null) andThenThrow(t) else stubbing.andThenThrow(t)
        }
        return stubbing ?: error("No exception thrown")
    }

    fun andThenThrow(throwableType: Class<out Throwable>): PactMockitoOngoingStubbing<T> =
        andThenAnswer(ThrowsExceptionForClassType(throwableType))

    override fun thenThrow(throwableType: Class<out Throwable>): OngoingStubbing<T> = andThenThrow(throwableType)

    override fun thenThrow(
        toBeThrown: Class<out Throwable>,
        vararg nextToBeThrown: Class<out Throwable>
    ): OngoingStubbing<T> {
        var stubbing = andThenThrow(toBeThrown)
        for (t in nextToBeThrown) {
            stubbing = stubbing.andThenThrow(t)
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

fun <T> uponReceiving(method: T) = PactMockito.uponReceiving(method)

