package io.github.ludorival.pactjvm.mock

class PactMockResponseError(val response: Any)
        : RuntimeException("This interaction returns an error $response")