package io.github.ludorival.pactjvm.mock.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.github.ludorival.pactjvm.mock.PactConfiguration
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockAdapter
import io.github.ludorival.pactjvm.mock.spring.serializerAsDefault
import io.github.ludorival.pactjvm.mock.Call

object NonDeterministicPact : PactConfiguration(
    SpringRestTemplateMockAdapter("shopping-webapp", ObjectMapperConfig::by)
)