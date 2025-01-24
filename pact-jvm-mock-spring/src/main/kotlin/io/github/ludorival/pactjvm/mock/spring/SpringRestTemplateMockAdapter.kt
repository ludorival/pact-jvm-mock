package io.github.ludorival.pactjvm.mock.spring

import au.com.dius.pact.core.model.*
import org.springframework.http.HttpMethod as SpringHttpMethod
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode
import java.net.URI
import java.nio.charset.StandardCharsets
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.ludorival.pactjvm.mock.*
import org.springframework.http.*

@Suppress("TooManyFunctions")
open class SpringRestTemplateMockAdapter(private val consumer: String, private val objectMapperByProvider: (String) -> ObjectMapper? = { null }) :
    PactMockAdapter<RequestResponseInteraction>() {

    constructor(consumer: String) : this(consumer, { null })
    private val defaultObjectMapper = ObjectMapper().apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
    private val uriTemplate by lazy {
        val uriFactory = DefaultUriBuilderFactory()
        uriFactory.encodingMode = EncodingMode.URI_COMPONENT // for backwards compatibility..
        uriFactory
    }

    override fun support(call: Call<*>): Boolean {
        return call.self is RestTemplate
    }

    override fun <T> buildInteraction(
        interactionBuilder: InteractionBuilder<T>,
        providerName: String
    ): RequestResponseInteraction {
        val call = interactionBuilder.call
        val uri = call.getUri()
        val body = call.getRequestBody()
        val queryParams = uri.query?.split("&")
            ?.filter { it.isNotEmpty() }
            ?.map { param ->
                val parts = param.split("=", limit = 2)
                parts[0] to listOf(if (parts.size > 1) parts[1] else null)
            }?.toMap()?.toMutableMap() ?: mutableMapOf()
        val objectMapper = objectMapperByProvider.invoke(providerName) ?: this.defaultObjectMapper

        val requestHeaders = call.getHttpHeaders()
        val request = Request(
            method = call.getHttpMethod().toString(),
            path = uri.path,
            query = queryParams,
            headers = requestHeaders.toSingleValueMap().mapValues { (_, v) -> listOf(v) }.toMutableMap(),
            body = serializeBody(body, requestHeaders, objectMapper)
        )
        val responseEntity = call.asResponseEntity()
        val response = with(responseEntity) {
            Response(
                status = statusCode.value(),
                headers = headers.toSingleValueMap().mapValues { (_, v) -> listOf(v) }.toMutableMap(),
                body = serializeBody(this.body, headers, objectMapper)
            )
        }
        return interactionBuilder.build {
            RequestResponseInteraction(
                description, providerStates, request.apply {
                    matchingRules = requestMatchingRules
                }, response.apply {
                    matchingRules = responseMatchingRules
                }
            )
        }
    }

    override fun determineConsumerAndProvider(call: Call<*>): Pair<String, String> {
        val uri = call.getUri()
        return Pair(consumer, uri.path.split("/").first { it.isNotBlank() })
    }

    private fun <T> serializeBody(
        body: T,
        httpHeaders: HttpHeaders,
        objectMapper: ObjectMapper
    ): OptionalBody {
        val contentType = if (httpHeaders.contentType == null && body != null && body !is String)
            ContentType.JSON else
            ContentType(org.apache.tika.mime.MediaType.parse(httpHeaders.contentType?.toString()))
        return when {
            contentType.isJson() -> OptionalBody.body(objectMapper.writeValueAsBytes(body), contentType)
            else -> OptionalBody.body(
                body?.toString(),
                contentType
            )
        }
    }

    @Suppress("SpreadOperator")
    private fun <T> Call<T>.getUri(): URI {
        // usually, the first parameter is the url
        val url = args[0]
        val requestEntity = getRequestEntity()
        return when {
            requestEntity != null -> requestEntity.url
            url != null && url is URI -> url
            url != null && url is String -> {
                val args = args.getUriVariables()
                uriTemplate.expand(url, *args)
            }

            else -> error("Expected to found an url")
        }
    }


    private fun <T> Call<T>.getHttpMethod(): HttpMethod {
        return HttpMethod.entries.find { methodName.startsWith(it.name.lowercase()) }
            ?: args.filterIsInstance<SpringHttpMethod>().firstOrNull()?.toPactMethod()
            ?: getRequestEntity()?.method?.toPactMethod()
            ?: error("Unable to determine the HttpMethod with method name $methodName")
    }


    private fun <T> Call<T>.getHttpHeaders(): HttpHeaders {
        return getHttpEntity()?.headers
            ?: getRequestEntity()?.headers ?: HttpHeaders.EMPTY
    }

    private fun <T> Call<T>.getRequestBody(): Any? {
        val httpEntity = getHttpEntity()
        val requestEntity = getRequestEntity()
        val indexBody by lazy { method.paramTypes.indexOfFirst { it == Any::class.java } }
        return when {
            httpEntity != null -> httpEntity.body
            requestEntity != null -> requestEntity.body
            indexBody >= 0 -> args[indexBody]
            else -> null
        }
    }

    private fun SpringHttpMethod.toPactMethod() = HttpMethod.valueOf(this.toString())
    private fun Call<*>.getHttpEntity(): HttpEntity<Any>? {
        return args.filterIsInstance<HttpEntity<Any>>().firstOrNull()
    }


    @Suppress("UNCHECKED_CAST")
    private fun <T> Call<T>.asResponseEntity(): ResponseEntity<Any> {
        val response = result.getOrNull()
        val exception = result.exceptionOrNull()
        return when (response) {
            is Unit -> ResponseEntity.ok().build()
            is ResponseEntity<*> -> response as ResponseEntity<Any>
            else -> when (exception) {
                null -> ResponseEntity.ok(response)
                is PactMockResponseError -> exception.response as? ResponseEntity<Any>
                    ?: error("Expect to provide a ResponseEntity")

                is HttpStatusCodeException -> ResponseEntity.status(exception.statusCode)
                    .headers(exception.responseHeaders)
                    .body(exception.responseBodyAsString)

                else -> error("Not supported exception as Client exception")
            }
        }
    }

    private fun String.asJson(): Any? = runCatching { defaultObjectMapper.readValue(this, Any::class.java) }.getOrNull()

    @Suppress("UNCHECKED_CAST")
    override fun <T> returnsResult(result: Result<T>, providerName: String): T {
        val exception = result.exceptionOrNull()
        val objectMapper = objectMapperByProvider.invoke(providerName) ?: this.defaultObjectMapper
        if (exception is PactMockResponseError) {
            val responseEntity = exception.response as ResponseEntity<Any>
            val statusCode: HttpStatus =
                responseEntity.statusCode as? HttpStatus ?: HttpStatus.valueOf(responseEntity.statusCode.value())
            val optionalBody = serializeBody(responseEntity.body, responseEntity.headers, objectMapper)
            throw HttpClientErrorException.create(
                statusCode,
                statusCode.reasonPhrase,
                responseEntity.headers,
                optionalBody.value ?: ByteArray(0),
                StandardCharsets.UTF_8
            )
        }
        return super.returnsResult(result, providerName)
    }

    private fun ResponseEntity<Any>.asResponse(): Response {
        return Response(
            status = statusCode.value(),
            headers = headers.toSingleValueMap().mapValues { (_, v) -> listOf(v) }.toMutableMap(),
            body = OptionalBody.body(body?.toString()?.toByteArray(StandardCharsets.UTF_8) ?: ByteArray(0))
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun List<Any?>.getUriVariables(): Array<out Any> {
        val lastArg = last()
        return if (lastArg is Array<*>) lastArg as Array<out Any> else if (lastArg != null) arrayOf(lastArg) else emptyArray()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> Call<T>.getRequestEntity(): RequestEntity<Any>? {
        val firstArg = args.first()
        return if (firstArg is RequestEntity<*>) firstArg as? RequestEntity<Any> else null
    }

}
