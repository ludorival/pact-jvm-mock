package io.github.ludorival.pactjvm.mock

import au.com.dius.pact.core.model.matchingrules.*

class MatchingRulesBuilder {
    private val matchingRules = MatchingRulesImpl()
    private fun formatPath(path: String): String = when {
        path.startsWith("\$") -> path
        path.startsWith("[") -> "\$$path"
        else -> "\$.$path"
    }


    fun body(path: String, matcher: MatchingRule): MatchingRulesBuilder = rule("body", path, matcher)

    fun header(path: String, matcher: MatchingRule): MatchingRulesBuilder = rule("header", path, matcher)

    fun path(path: String, matcher: MatchingRule): MatchingRulesBuilder = rule("path", path, matcher)

    fun query(path: String, matcher: MatchingRule): MatchingRulesBuilder = rule("query", path, matcher)

    fun rule(name: String, item: String, matcher: MatchingRule): MatchingRulesBuilder = apply {
        matchingRules.rulesForCategory(name).addRule(formatPath(item), matcher)
    }

    fun build(): MatchingRules = matchingRules
}

