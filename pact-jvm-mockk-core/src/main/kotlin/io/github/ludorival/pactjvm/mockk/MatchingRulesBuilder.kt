package io.github.ludorival.pactjvm.mockk

class MatchingRulesBuilder {
        private val body = mutableMapOf<String, Matcher>()
        private val header = mutableMapOf<String, Matcher>()
        private val path = mutableMapOf<String, Matcher>()
        private val query = mutableMapOf<String, Matcher>()

        fun body(path: String, matcher: Matcher ): MatchingRulesBuilder = apply { this.body[path] = matcher } 
        fun header(path: String, matcher: Matcher ): MatchingRulesBuilder = apply { this.header[path] = matcher }   
        fun path(path: String, matcher: Matcher ): MatchingRulesBuilder = apply { this.path[path] = matcher } 
        fun query(path: String, matcher: Matcher ): MatchingRulesBuilder = apply { this.query[path] = matcher } 

        fun build(): Pact.Interaction.MatchingRules? = 
            Pact.Interaction.MatchingRules(
                body = body.toMap().ifEmpty { null },
                header = header.toMap().ifEmpty { null },
                path = path.toMap().ifEmpty { null },
                query = query.toMap().ifEmpty { null }
            ).takeIf { it.body != null || it.header != null || it.path != null || it.query != null  }
    }
