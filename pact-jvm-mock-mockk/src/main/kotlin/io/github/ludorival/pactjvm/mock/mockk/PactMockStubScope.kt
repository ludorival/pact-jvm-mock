package io.github.ludorival.pactjvm.mock.mockk

import io.github.ludorival.pactjvm.mock.CallInterceptor
import io.github.ludorival.pactjvm.mock.InteractionBuilder
import io.github.ludorival.pactjvm.mock.InteractionBuilderImpl
import io.github.ludorival.pactjvm.mock.Pact
import io.github.ludorival.pactjvm.mock.MatchingRulesBuilder
import io.github.ludorival.pactjvm.mock.Call as MockCall
import io.mockk.*

class PactMockStubScope<T, B>(
    private val mockKStubScope: MockKStubScope<T, B>
) {

    private val interactionBuilder = InteractionBuilderImpl()

    infix fun withDescription(description: String?): PactMockStubScope<T, B> = apply {
        description?.let {
            interactionBuilder.description(it)
        }
    }

    infix fun given(providerState: Pact.Interaction.ProviderState): PactMockStubScope<T, B> = apply {
        interactionBuilder.providerState(providerState.name, providerState.params)
    }

    infix fun matchingRequest(block: MatchingRulesBuilder.() -> Unit):PactMockStubScope<T, B> = apply {
        interactionBuilder.requestMatchingRules(block)
    }

    infix fun macthingResponse(block: MatchingRulesBuilder.() -> Unit): PactMockStubScope<T, B> = apply {
        interactionBuilder.responseMatchingRules(block)
    }


    infix fun answers(answer: Answer<T>): PactMockKAdditionalAnswerScope<T, B> {
        return PactMockKAdditionalAnswerScope(mockKStubScope.answers {
            answer.interceptAndGet(it, interactionBuilder)
        })
    }



    infix fun answers(answer: MockKAnswerScope<T, B>.(Call) -> T) =
        PactMockKAdditionalAnswerScope(mockKStubScope.answers{
            val functionAnswer = FunctionAnswer<T> { answer.invoke(this, it) }
            functionAnswer.interceptAndGet(it, interactionBuilder)
        })

    infix fun returns(returnValue: T) = answers(ConstantAnswer(returnValue))

    infix fun returnsMany(values: List<T>) = answers(ManyAnswersAnswer(values.map{ ConstantAnswer(it) }))

    fun returnsMany(vararg values: T) = returnsMany(values.toList())


    infix fun throws(ex: Throwable) = answers(ThrowingAnswer(ex))

    infix fun throwsMany(exList: List<Throwable>): PactMockKAdditionalAnswerScope<T, B> =
        this answers (ManyAnswersAnswer(exList.map { ThrowingAnswer(it) }))



    infix fun coAnswers(answer: suspend MockKAnswerScope<T, B>.(Call) -> T) =
        PactMockKAdditionalAnswerScope(mockKStubScope.coAnswers{
            val functionAnswer = CoFunctionAnswer<T> { answer.invoke(this, it) }
            functionAnswer.interceptAndGet(it, interactionBuilder)
        })

    class PactMockKAdditionalAnswerScope<T, B>(
        private val mockKAdditionalAnswerScope: MockKAdditionalAnswerScope<T, B>,
    ) {

        private val interactionBuilder = InteractionBuilderImpl()

        infix fun withDescription(description: String?) = apply {
            description?.let {
                interactionBuilder.description(it)
            }
        }

        infix fun given(providerState: Pact.Interaction.ProviderState) = apply {
            interactionBuilder.providerState(providerState.name, providerState.params)
        }

        infix fun matchingRequest(block: MatchingRulesBuilder.() -> Unit) = apply {
            interactionBuilder.requestMatchingRules(block)
        }

        infix fun macthingResponse(block: MatchingRulesBuilder.() -> Unit) = apply {
            interactionBuilder.responseMatchingRules(block)
        }

        infix fun andThenAnswer(answer: Answer<T>): PactMockKAdditionalAnswerScope<T, B> {
            return PactMockKAdditionalAnswerScope(mockKAdditionalAnswerScope.andThenAnswer { answer.interceptAndGet(it, interactionBuilder)})
        }

        infix fun andThenAnswer(answer: MockKAnswerScope<T, B>.(Call) -> T) =
            PactMockKAdditionalAnswerScope(mockKAdditionalAnswerScope.andThenAnswer{
                val functionAnswer = FunctionAnswer<T> { answer.invoke(this, it) }
                functionAnswer.interceptAndGet(it, interactionBuilder)
            })

        infix fun andThen(returnValue: T) = andThenAnswer(ConstantAnswer(returnValue))

        infix fun andThenMany(values: List<T>) = andThenAnswer(ManyAnswersAnswer(values.map{ ConstantAnswer(it) }))

        fun andThenMany(vararg values: T) = andThenMany(values.toList())

        infix fun andThenThrows(ex: Throwable) = andThenAnswer(ThrowingAnswer(ex))

        infix fun andThenThrowsMany(exList: List<Throwable>) =
            andThenAnswer(ManyAnswersAnswer(exList.map { ThrowingAnswer(it) }))

        infix fun coAndThen(answer: suspend MockKAnswerScope<T, B>.(Call) -> T) =
             PactMockKAdditionalAnswerScope(mockKAdditionalAnswerScope.andThenAnswer{
                val functionAnswer = CoFunctionAnswer<T> { answer.invoke(this, it) }
                functionAnswer.interceptAndGet(it, interactionBuilder)
            })
    }
}