package io.github.ludorival.pactjvm.mock.mockito

import org.mockito.stubbing.OngoingStubbing
import java.util.function.Function

object MockitoUtils {
    @JvmStatic
    fun <T> willRespond(stubbing: OngoingStubbing<T>, response: T): OngoingStubbing<T> {
        return stubbing.willRespondWith { response }
    }

    @JvmStatic
    fun <T> willRespondWith(stubbing: OngoingStubbing<T>, scopeFunction: Function<MockitoAnswerScope<T>, T>): OngoingStubbing<T> {
        return stubbing.willRespondWith { invocation ->
            val scope = MockitoAnswerScope<T>()
            scopeFunction.apply(scope)
        }
    }

    @JvmStatic
    fun <T> willRespondWithError(stubbing: OngoingStubbing<T>, scopeFunction: Function<MockitoAnswerScope<T>, Any>): OngoingStubbing<T> {
        return stubbing.willRespondWith { invocation ->
            val scope = MockitoAnswerScope<T>()
            val response = scopeFunction.apply(scope)
            scope.anError(response)
        }
    }
} 