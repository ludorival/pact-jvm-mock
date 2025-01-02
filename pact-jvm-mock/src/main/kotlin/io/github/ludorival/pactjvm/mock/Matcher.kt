package io.github.ludorival.pactjvm.mock

import com.fasterxml.jackson.annotation.JsonProperty

data class Matcher(
            val match: MatchEnum,
            val regex: String? = null,
            val max: Number? = null,
            val min: Number? = null,
            val value: String? = null,
            val format: String? = null
        ) {
            enum class MatchEnum {
                @JsonProperty("regex") REGEX,
                @JsonProperty("type") TYPE,
                @JsonProperty("boolean") BOOLEAN,
                @JsonProperty("contentType") CONTENT_TYPE,
                @JsonProperty("date") DATE,
                @JsonProperty("datetime") DATETIME,
                @JsonProperty("decimal") DECIMAL,
                @JsonProperty("equality") EQUALITY,
                @JsonProperty("include") INCLUDE,
                @JsonProperty("integer") INTEGER,
                @JsonProperty("null") NULL,
                @JsonProperty("number") NUMBER,
                @JsonProperty("time") TIME,
                @JsonProperty("values") VALUES
            }
}
