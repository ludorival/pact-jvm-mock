package io.github.ludorival.pactjvm.mockk.spring

import io.github.ludorival.pactjvm.mockk.Pact
import io.github.ludorival.pactjvm.mockk.Pact.Interaction.Request.Method
import io.github.ludorival.pactjvm.mockk.PactMockkAdapter
import io.mockk.Call
import io.mockk.Invocation
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode
import java.net.URI

class SpringRestTemplateMockkAdapter :
    PactMockkAdapter() {

    private val uriTemplate by lazy {
        val uriFactory = DefaultUriBuilderFactory()
        uriFactory.encodingMode = EncodingMode.URI_COMPONENT // for backwards compatibility..
        uriFactory
    }

    override fun support(call: Call): Boolean {
        return call.invocation.self is RestTemplate
    }

    override fun Call.getUri(): URI {
        // usually, the first parameter is the url
        val url = invocation.args[0]
        val requestEntity = getRequestEntity()
        return when {
            requestEntity != null        -> requestEntity.url
            url != null && url is URI    -> url
            url != null && url is String -> {
                val args = invocation.getUriVariables()
                uriTemplate.expand(url, *args)
            }

            else                         -> error("Expected to found an url")
        }
    }


    override fun Call.getHttpMethod(): Method {
        val methodName = invocation.method.name
        return Method.values().find { methodName.startsWith(it.name.lowercase()) }
            ?: invocation.args.filterIsInstance<HttpMethod>().firstOrNull()?.toPactMethod()
            ?: getRequestEntity()?.method?.toPactMethod()
            ?: error("Unable to determine the HttpMethod with method name $methodName")
    }


    override fun Call.getHttpHeaders(): Map<String, String>? {
        return getHttpEntity()?.headers?.toSingleValueMap()
            ?: getRequestEntity()?.headers?.toSingleValueMap()
    }

    override fun Call.getRequestBody(): Any? {
        val httpEntity = getHttpEntity()
        val requestEntity = getRequestEntity()
        val indexBody by lazy { invocation.method.paramTypes.indexOfFirst { it.java == Any::class.java } }
        return when {
            httpEntity != null    -> httpEntity.body
            requestEntity != null -> requestEntity.body
            indexBody >= 0        -> invocation.args[indexBody]
            else                  -> null
        }
    }

    override fun <T> Result<T>.getResponse(): Pact.Interaction.Response {
        return asResponseEntity().asResponse()
    }

    private fun HttpMethod.toPactMethod() = Method.valueOf(name)
    private fun Call.getHttpEntity(): HttpEntity<Any>? {
        return invocation.args.filterIsInstance<HttpEntity<Any>>().firstOrNull()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> Result<T>.asResponseEntity(): ResponseEntity<Any> {
        val response = getOrNull()
        val exception = exceptionOrNull()
        return when (response) {
            is Unit              -> ResponseEntity.ok().build()
            is ResponseEntity<*> -> response as ResponseEntity<Any>
            else                 -> when (exception) {
                null                       -> ResponseEntity.ok(response)
                is HttpStatusCodeException -> ResponseEntity.status(exception.statusCode)
                    .headers(exception.responseHeaders)
                    .body(exception.responseBodyAsString.ifEmpty { exception.statusText })

                else                       -> error("Not supported exception as Client exception")
            }
        }
    }


    private fun ResponseEntity<Any>.asResponse(): Pact.Interaction.Response {
        return Pact.Interaction.Response(
            status = statusCode.value(),
            headers = headers.toSingleValueMap(),
            body = body
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun Invocation.getUriVariables(): Array<out Any> {
        val lastArg = args.last()
        return if (lastArg is Array<*>) lastArg as Array<out Any> else emptyArray()
    }

    @Suppress("UNCHECKED_CAST")
    private fun Call.getRequestEntity(): RequestEntity<Any>? {
        val firstArg = invocation.args.first()
        return if (firstArg is RequestEntity<*>) firstArg as? RequestEntity<Any> else null
    }
}
