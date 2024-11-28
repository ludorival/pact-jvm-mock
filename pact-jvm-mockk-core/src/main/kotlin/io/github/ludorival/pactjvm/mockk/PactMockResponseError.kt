package io.github.ludorival.pactjvm.mockk

class PactMockResponseError(val response: Any)
        : RuntimeException("This interaction returns an error $response")