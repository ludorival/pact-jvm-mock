package io.github.ludorival.pactjvm.mock

class MatchingRulesBuilder {
    private val matchingRules = mutableMapOf<String, Matcher>()
    private fun formatPath(path: String): String = if (path.startsWith("[")) path else ".$path"


    fun body(path: String, matcher: Matcher): MatchingRulesBuilder = apply { matchingRules["$.body${formatPath(path)}"] = matcher }
    fun header(path: String, matcher: Matcher): MatchingRulesBuilder = apply { matchingRules["$.header${formatPath(path)}"] = matcher }
    fun path(path: String, matcher: Matcher): MatchingRulesBuilder = apply { matchingRules["$.path${formatPath(path)}"] = matcher }
    fun query(path: String, matcher: Matcher): MatchingRulesBuilder = apply { matchingRules["$.query${formatPath(path)}"] = matcher }

    fun build(): MatchingRules? = matchingRules.takeIf { it.isNotEmpty() }
}
