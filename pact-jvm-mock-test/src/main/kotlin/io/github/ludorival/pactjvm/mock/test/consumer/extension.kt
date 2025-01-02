package io.github.ludorival.pactjvm.mock.test.consumer

import org.springframework.http.ResponseEntity

 fun <T> ResponseEntity<T>.safeValue(lazyMessage: () -> String = { "Expect to have a non null value"}): T {
    return body ?: error(lazyMessage())
}
