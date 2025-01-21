package io.github.ludorival.pactjvm.mock

import au.com.dius.pact.core.model.matchingrules.*

class MatchingRulesBuilder {
    private val matchingRules = mutableMapOf<String, MatchingRuleGroup>()
    private fun formatPath(path: String): String = if (path.startsWith("[")) path else ".$path"

    fun body(path: String, matcher: MatchingRule): MatchingRulesBuilder = apply {
        val rules = mutableListOf<MatchingRule>()
        rules.add(matcher)
        matchingRules["$.body${formatPath(path)}"] = MatchingRuleGroup(rules)
    }

    fun header(path: String, matcher: MatchingRule): MatchingRulesBuilder = apply {
        val rules = mutableListOf<MatchingRule>()
        rules.add(matcher)
        matchingRules["$.header${formatPath(path)}"] = MatchingRuleGroup(rules)
    }

    fun path(path: String, matcher: MatchingRule): MatchingRulesBuilder = apply {
        val rules = mutableListOf<MatchingRule>()
        rules.add(matcher)
        matchingRules["$.path${formatPath(path)}"] = MatchingRuleGroup(rules)
    }

    fun query(path: String, matcher: MatchingRule): MatchingRulesBuilder = apply {
        val rules = mutableListOf<MatchingRule>()
        rules.add(matcher)
        matchingRules["$.query${formatPath(path)}"] = MatchingRuleGroup(rules)
    }

    fun build(): MatchingRules = MatchingRulesImpl().apply {
        matchingRules.toMap().forEach { (key, group) ->
            val parts = key.split(".")
            val category = parts[1]
            val path = parts.drop(2).joinToString(".")
            val matchingRuleCategory = addCategory(category)
            group.rules.forEach { rule ->
                if (path.isNotEmpty()) {
                    matchingRuleCategory.addRule(path, rule, RuleLogic.AND)
                } else {
                    matchingRuleCategory.addRule(rule, RuleLogic.AND)
                }
            }
        }
    }
}

// Helper functions to create matchers
fun regex(pattern: String): MatchingRule = RegexMatcher(pattern)
fun type(): MatchingRule = TypeMatcher
