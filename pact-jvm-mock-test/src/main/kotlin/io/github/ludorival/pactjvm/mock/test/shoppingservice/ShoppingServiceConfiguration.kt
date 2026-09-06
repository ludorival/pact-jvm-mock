package io.github.ludorival.pactjvm.mock.test.shoppingservice

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper

@Configuration
open class ShoppingServiceConfiguration {

    @Bean
    open fun shoppingServiceJsonCustomizer() = JsonMapperBuilderCustomizer { it.configureForShoppingService() }
}

fun JsonMapper.Builder.configureForShoppingService(): JsonMapper.Builder = this
    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
    .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
