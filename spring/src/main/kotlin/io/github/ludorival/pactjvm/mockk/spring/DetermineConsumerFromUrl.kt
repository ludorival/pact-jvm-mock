package io.github.ludorival.pactjvm.mockk.spring

import io.github.ludorival.pactjvm.mockk.ConsumerMetaData
import io.github.ludorival.pactjvm.mockk.getConsumerName
import java.net.URI

typealias DetermineConsumerFromUrl = (URI) -> ConsumerMetaData

val DEFAULT_CONSUMER_DETERMINER: DetermineConsumerFromUrl = { uri ->
    ConsumerMetaData(uri.getConsumerName())
}
