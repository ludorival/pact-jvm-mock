package io.github.ludorival.pactjvm.mockk

import java.lang.reflect.Method

object ExtractInteractionDescription {

    private val TEST_ANNOTATION_EXPRESSION = Regex("org.junit(?s)(.*).Test")

    fun getDescriptionFromStackTrace(): String? {
        val stackTrace = Thread.currentThread().stackTrace
        return stackTrace.toList().firstOrNull { trace ->
            runCatching {
                val traceClass = Class.forName(trace.className)
                val method = traceClass.getMethod(trace.methodName)
                isTestMethod(method)
            }.getOrNull() == true

        }?.methodName
    }

    private fun isTestMethod(method: Method): Boolean =
        method.annotations.any {
            it.annotationClass.qualifiedName?.matches(TEST_ANNOTATION_EXPRESSION) ?: false
        }
}
