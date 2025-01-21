package io.github.ludorival.pactjvm.mock
import java.lang.reflect.Method

data class Call<T>(
    val method: Method,
    val self: Any,
    val args: List<Any?>,
    val result: Result<T>
) {
    data class Method(
        val name: String,
        val paramTypes: Array<Class<*>>,
    )

    val methodName: String
        get() = method.name

    @Suppress("UNCHECKED_CAST")
    fun <R> arg(index: Int) = args[index] as R

    fun <R> firstArg() = arg<R>(0)

    fun <R> secondArg() = arg<R>(1)

    fun <R> thirdArg() = arg<R>(2)
}
