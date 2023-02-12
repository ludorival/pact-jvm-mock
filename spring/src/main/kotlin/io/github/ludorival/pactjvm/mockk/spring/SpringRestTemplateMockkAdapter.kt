package io.github.ludorival.pactjvm.mockk.spring

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.ludorival.pactjvm.mockk.ConsumerInteraction
import io.github.ludorival.pactjvm.mockk.Pact
import io.github.ludorival.pactjvm.mockk.PactMockkAdapter
import io.mockk.Call
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.net.URI

class SpringRestTemplateMockkAdapter(private val determineConsumerFromUrl: DetermineConsumerFromUrl) :
    PactMockkAdapter {
    override fun support(call: Call): Boolean {
        return call.invocation.self is RestTemplate
    }

    override fun <T> buildInteraction(call: Call, response: T): ConsumerInteraction {
        val uri = call.getUri()
        val consumerMetaData = determineConsumerFromUrl.invoke(uri)
        val interaction = Pact.Interaction(
            request = Pact.Interaction.Request(
                method = Pact.Interaction.Request.Method.valueOf(call.getHttpMethod().name()),
                path = uri.path,
                query = uri.query,
                headers = call.getHttpHeaders(),
                body = call.getRequestBody()?.let { consumerMetaData.customObjectMapper.valueToTree(it) }
            ),
            response = response.asResponseEntity().asResponse(consumerMetaData.customObjectMapper)
        )
        return ConsumerInteraction(consumerMetaData, interaction)
    }


    private fun Call.getUri(): URI {
        // usually, the first parameter is the url
        val url = invocation.args[0]
        return if (url != null && url is URI) url
        else if (url != null && url is String) URI.create(url)
        else error("Expected to found an url")
    }

    private fun Call.getHttpMethod(): HttpMethod {
        val methodName = invocation.method.name
        return HttpMethod.values().find { methodName.startsWith(it.name().lowercase()) }
            ?: invocation.args.filterIsInstance<HttpMethod>().firstOrNull()
            ?: error("Unable to determine the HttpMethod with method name $methodName")
    }


    private fun Call.getHttpEntity(): HttpEntity<Any>? {
        return invocation.args.filterIsInstance<HttpEntity<Any>>().firstOrNull()
    }

    private fun Call.getHttpHeaders(): Map<String, String>? {
        return getHttpEntity()?.headers?.toSingleValueMap()
    }

    private fun Call.getRequestBody(): Any? {
        val httpEntity = getHttpEntity()
        if (httpEntity != null)
            return httpEntity.body

        val indexBody = invocation.method.paramTypes.indexOfFirst { it.java == Any::class.java }
        return if (indexBody < 0) null else invocation.args[indexBody]
    }


    @Suppress("UNCHECKED_CAST")
    private fun <T> T.asResponseEntity(): ResponseEntity<Any> {
        return when (this) {
            is Unit -> ResponseEntity.ok().build()
            is ResponseEntity<*> -> this as ResponseEntity<Any>
            else -> ResponseEntity.ok(this)
        }
    }

    private fun ResponseEntity<Any>.asResponse(objectMapper: ObjectMapper): Pact.Interaction.Response {
        return Pact.Interaction.Response(status = statusCode.value(),
            headers = headers.toSingleValueMap(),
            body = body?.let { objectMapper.valueToTree(it) }

        )
    }
}
