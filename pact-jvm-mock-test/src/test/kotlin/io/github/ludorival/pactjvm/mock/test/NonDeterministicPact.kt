package io.github.ludorival.pactjvm.mock.test

import io.github.ludorival.pactjvm.mock.PactConfiguration
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockAdapter

object NonDeterministicPact : PactConfiguration(
    SpringRestTemplateMockAdapter("shopping-webapp", ProviderJsonSerializers::by)
)
