package io.github.ludorival.pactjvm.mock.test

import io.github.ludorival.pactjvm.mock.spring.JacksonJsonBodySerializer
import io.github.ludorival.pactjvm.mock.spring.JsonBodySerializer
import io.github.ludorival.pactjvm.mock.spring.serializerAsDefault
import io.github.ludorival.pactjvm.mock.test.shoppingservice.configureForShoppingService
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import java.time.LocalDate

object ProviderJsonSerializers {

    fun by(providerName: String): JsonBodySerializer? =
        if (providerName == "shopping-service") shoppingServiceSerializer else null

    private val shoppingServiceSerializer: JsonBodySerializer by lazy {
        val fixedDates = SimpleModule().addSerializer(LocalDate::class.java, serializerAsDefault("2023-01-01"))
        JacksonJsonBodySerializer(JsonMapper.builder().configureForShoppingService().addModule(fixedDates).build())
    }
}
