package io.github.ludorival.pactjvm.mockk

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.Answer
import io.mockk.Call
import io.mockk.ConstantAnswer
import io.mockk.MockKAdditionalAnswerScope
import io.mockk.MockKStubScope
import java.net.URI

infix fun <T, B> MockKStubScope<T, B>.willRespondWith(answer: Answer<T>): MockKAdditionalAnswerScope<T, B> {
    return willRespondWith {
        answer.answer(it)
    }
}

infix fun <T, B> MockKStubScope<T, B>.willRespondWith(answer: PactMockKAnswerScope<T, B>.(Call) -> T):
    MockKAdditionalAnswerScope<T, B> {
    return answers {
        val pactScope = PactMockKAnswerScope<T, B>(this)
        val result = answer.invoke(pactScope, it)
        PactMockk.intercept(it, result, pactScope.description, pactScope.providerStates)
        result
    }
}

infix fun <T, B> MockKStubScope<T, B>.willRespond(returnValue: T): MockKAdditionalAnswerScope<T, B> {
    return willRespondWith(ConstantAnswer(returnValue))
}

//inline fun <reified T> serializerWith(crossinline supplier: (JsonGenerator) -> Unit) = object : JsonSerializer<T>() {
//    override fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider?) {
//        supplier(gen)
//    }
//}
//
//inline fun <reified T> serializerAsDefault(defaultValue: String) =
//    serializerWith<T> { it.writeString(defaultValue) }


fun URI.getConsumerName() = path.split("/").first { it.isNotBlank() }

internal val DEFAULT_OBJECT_MAPPER = ObjectMapper()


