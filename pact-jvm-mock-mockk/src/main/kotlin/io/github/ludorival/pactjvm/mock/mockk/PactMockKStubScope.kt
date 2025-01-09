package io.github.ludorival.pactjvm.mock.mockk

import io.github.ludorival.pactjvm.mock.*
import io.github.ludorival.pactjvm.mock.Call as MockCall
import io.mockk.*

class PactMockKStubScope<T, B>(
    private val mockKStubScope: MockKStubScope<T, B>
) {

    private val interactionBuilder = InteractionBuilder()

    infix fun withDescription(description: InteractionHandler<String>) = apply {
        interactionBuilder.description(description)
    }

    infix fun withDescription(description: String?) = apply {
        description?.let { value ->
            interactionBuilder.description { value }
        }
    }

    infix fun given(block: InteractionBuilder.ProviderStateBuilder.(Pact.Interaction) -> InteractionBuilder.ProviderStateBuilder) = apply {
        interactionBuilder.providerState(block)
    }

    infix fun matchingRequest(block: MatchingRulesBuilder.() -> MatchingRulesBuilder) = apply {
        interactionBuilder.requestMatchingRules(block)
    }

    infix fun macthingResponse(block: MatchingRulesBuilder.() -> MatchingRulesBuilder) = apply {
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

        private val interactionBuilder = InteractionBuilder()

        infix fun withDescription(description: InteractionHandler<String>) = apply {
            interactionBuilder.description(description)
        }

        infix fun given(block: InteractionBuilder.ProviderStateBuilder.(Pact.Interaction) -> InteractionBuilder.ProviderStateBuilder) = apply {
            interactionBuilder.providerState(block)
        }

        infix fun matchingRequest(block: MatchingRulesBuilder.() -> MatchingRulesBuilder) = apply {
            interactionBuilder.requestMatchingRules(block)
        }

        infix fun macthingResponse(block: MatchingRulesBuilder.() -> MatchingRulesBuilder) = apply {
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