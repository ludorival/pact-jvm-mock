package io.github.ludorival.pactjvm.mockk

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.Answer
import io.mockk.Call
import io.mockk.ConstantAnswer
import io.mockk.MockKAdditionalAnswerScope
import io.mockk.MockKStubScope

fun pactOptions(builder: PactOptions.Builder.() -> Unit) =
    PactMockk.setPactOptions(PactOptions.Builder().apply(builder).build())

fun writePacts() = PactMockk.writePacts()

infix fun <T, B> MockKStubScope<T, B>.willRespondWith(answer: Answer<T>): MockKAdditionalAnswerScope<T, B> {
    return willRespondWith {
        answer.answer(it)
    }
}

infix fun <T, B> MockKStubScope<T, B>.willRespondWith(answer: PactMockKAnswerScope<T, B>.(Call) -> T):
    MockKAdditionalAnswerScope<T, B> {
    return answers {
        val pactScope = PactMockKAnswerScope<T, B>(this)
        val result = runCatching { answer.invoke(pactScope, it) }
        PactMockk.intercept(it, result, pactScope.pactOptions)
        result.getOrThrow()
    }
}

infix fun <T, B> MockKStubScope<T, B>.willRespond(returnValue: T): MockKAdditionalAnswerScope<T, B> {
    return willRespondWith(ConstantAnswer(returnValue))
}


typealias DetermineProviderFromInteraction = (Pact.Interaction) -> String

typealias ObjectMapperCustomizer = (String) -> ObjectMapper


