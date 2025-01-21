package io.github.ludorival.pactjvm.mock.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.github.ludorival.pactjvm.mock.PactConfiguration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.LocalDate
import io.github.ludorival.pactjvm.mock.test.shoppingservice.objectMapperBuilder
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockAdapter
import io.github.ludorival.pactjvm.mock.spring.serializerAsDefault

object NonDeterministicPact : PactConfiguration("shopping-list", SpringRestTemplateMockAdapter(ObjectMapperConfig::by))
