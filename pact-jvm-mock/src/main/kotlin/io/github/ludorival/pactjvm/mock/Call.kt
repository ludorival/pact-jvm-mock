package io.github.ludorival.pactjvm.mock
import java.lang.reflect.Method

data class Call(
    val method: Method,
    val self: Any,
    val args: List<Any?>,
) {
    data class Method(
        val name: String,
        val paramTypes: Array<Class<*>>,
    )

    val methodName: String
        get() = method.name

}
