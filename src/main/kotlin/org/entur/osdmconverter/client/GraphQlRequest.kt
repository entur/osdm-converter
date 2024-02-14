package org.entur.osdmconverter.client

data class GraphQlRequest(
    private val query: String,
    private val variables: Map<String, Any>? = null
) {
  val requestBody = "{\"query\": \"$query\", ${getVariablesString()}}"

  private fun getVariablesString(): String =
      if (variables == null || variables.isEmpty()) "\"variables\":null"
      else
          "\"variables\": " +
              variables.asIterable().joinToString(",", "{", "}") {
                "\"${it.key}\":${getValueString(it.value)}"
              }

  private fun getValueString(value: Any): String =
      when (value) {
        is String -> "\"$value\""
        is List<*> -> value.joinToString(",", "[", "]") { getValueString(it!!) }
        else -> value.toString().let { "\"$it\"" }
      }
}
