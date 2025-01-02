package io.github.ludorival.pactjvm.mock.mockk

import io.github.ludorival.pactjvm.mock.Call as MockCall
import io.github.ludorival.pactjvm.mock.CallInterceptor
import io.mockk.Answer
import io.mockk.Call
import io.mockk.ConstantAnswer
import io.mockk.MockKAdditionalAnswerScope
import io.mockk.MockKStubScope

infix fun <T, B> MockKStubScope<T, B>.willRespondWith(
        answer: Answer<T>
): MockKAdditionalAnswerScope<T, B> {
    return willRespondWith { answer.answer(it) }
}

infix fun <T, B> MockKStubScope<T, B>.willRespondWith(
        answer: PactMockKAnswerScope<T, B>.(Call) -> T
): MockKAdditionalAnswerScope<T, B> {
    return answers {
        val pactScope = PactMockKAnswerScope<T, B>(this)
        val result = runCatching { answer.invoke(pactScope, it) }
        CallInterceptor.getInstance()
                .interceptAndGet(
                        it.invocation.let {
                            MockCall(
                                    MockCall.Method(
                                            it.method.name,
                                            it.method.paramTypes.map { it.java }.toTypedArray()
                                    ),
                                    it.self,
                                    it.args
                            )
                        },
                        result,
                        pactScope
                )
    }
}

infix fun <T, B> MockKStubScope<T, B>.willRespond(
        returnValue: T
): MockKAdditionalAnswerScope<T, B> {
    return willRespondWith(ConstantAnswer(returnValue))
}
