package io.github.ludorival.pactjvm.mock.spring

import io.github.ludorival.pactjvm.mock.Pact
import io.github.ludorival.pactjvm.mock.Pact.Interaction.Request.Method
import io.github.ludorival.pactjvm.mock.PactMockResponseError
import io.github.ludorival.pactjvm.mock.PactMockAdapter
import io.github.ludorival.pactjvm.mock.Call
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode
import java.net.URI
import java.nio.charset.StandardCharsets

@Suppress("TooManyFunctions")
class SpringRestTemplateMockkAdapter :
    PactMockAdapter() {

    private val uriTemplate by lazy {
        val uriFactory = DefaultUriBuilderFactory()
        uriFactory.encodingMode = EncodingMode.URI_COMPONENT // for backwards compatibility..
        uriFactory
    }

    override fun support(call: Call): Boolean {
        return call.self is RestTemplate
    }

    @Suppress("SpreadOperator")
    override fun Call.getUri(): URI {
        // usually, the first parameter is the url
        val url = args[0]
        val requestEntity = getRequestEntity()
        return when {
            requestEntity != null        -> requestEntity.url
            url != null && url is URI    -> url
            url != null && url is String -> {
                val args = args.getUriVariables()
                uriTemplate.expand(url, *args)
            }

            else                         -> error("Expected to found an url")
        }
    }


    override fun Call.getHttpMethod(): Method {
    
        return Method.values().find { methodName.startsWith(it.name.lowercase()) }
            ?: args.filterIsInstance<HttpMethod>().firstOrNull()?.toPactMethod()
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
        val indexBody by lazy { method.paramTypes.indexOfFirst { it == Any::class.java } }
        return when {
            httpEntity != null    -> httpEntity.body
            requestEntity != null -> requestEntity.body
            indexBody >= 0        -> args[indexBody]
            else                  -> null
        }
    }

    override fun <T> Result<T>.getResponse(): Pact.Interaction.Response {
        return asResponseEntity().asResponse()
    }

    private fun HttpMethod.toPactMethod() = Method.valueOf(this.toString())
    private fun Call.getHttpEntity(): HttpEntity<Any>? {
        return args.filterIsInstance<HttpEntity<Any>>().firstOrNull()
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
                is PactMockResponseError   -> exception.response as? ResponseEntity<Any> ?: error("Expect to provide a ResponseEntity")
                is HttpStatusCodeException -> ResponseEntity.status(exception.statusCode)
                    .headers(exception.responseHeaders)
                    .body(exception.responseBodyAsString.ifEmpty { exception.statusText })

                else                       -> error("Not supported exception as Client exception")
            }
        }
    }
    @Suppress("UNCHECKED_CAST")
    override fun <T> returnsResult(result: Result<T>): T {
        val exception = result.exceptionOrNull()
        if (exception is PactMockResponseError) {
            val responseEntity = exception.response as ResponseEntity<Any>
            val statusCode : HttpStatus = responseEntity.statusCode as? HttpStatus ?: HttpStatus.valueOf(responseEntity.statusCode.value())
            val body = responseEntity.body?.toString()
            val bodyBytes = body?.toByteArray(StandardCharsets.UTF_8) ?: ByteArray(0)
            throw HttpClientErrorException.create(
                statusCode,
                statusCode.reasonPhrase,
                responseEntity.headers,
                bodyBytes,
                StandardCharsets.UTF_8
            )
        }
        return super.returnsResult(result)
    }

    private fun ResponseEntity<Any>.asResponse(): Pact.Interaction.Response {
        return Pact.Interaction.Response(
            status = statusCode.value(),
            headers = headers.toSingleValueMap(),
            body = body
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun List<Any?>.getUriVariables(): Array<out Any> {
        val lastArg = last()
        return if (lastArg is Array<*>) lastArg as Array<out Any> else emptyArray()
    }

    @Suppress("UNCHECKED_CAST")
    private fun Call.getRequestEntity(): RequestEntity<Any>? {
        val firstArg = args.first()
        return if (firstArg is RequestEntity<*>) firstArg as? RequestEntity<Any> else null
    }
}
