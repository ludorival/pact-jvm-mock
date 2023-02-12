package io.github.ludorival.pactjvm.mockk

import io.mockk.Answer
import io.mockk.Call
import io.mockk.ConstantAnswer
import io.mockk.MockKAdditionalAnswerScope
import io.mockk.MockKAnswerScope
import io.mockk.MockKStubScope

infix fun <T, B> MockKStubScope<T, B>.willRespondWith(answer: Answer<T>): MockKAdditionalAnswerScope<T, B> {
    return answers(answer)
}

infix fun <T, B> MockKStubScope<T, B>.willRespondWith(answer: MockKAnswerScope<T, B>.(Call) -> T): MockKAdditionalAnswerScope<T, B> {
    return answers(answer)
}

infix fun <T, B> MockKStubScope<T, B>.willRespond(returnValue: T): MockKAdditionalAnswerScope<T, B> {
    return willRespondWith(ConstantAnswer(returnValue))
}
