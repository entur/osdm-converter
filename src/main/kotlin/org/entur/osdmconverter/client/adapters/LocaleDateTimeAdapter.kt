package org.entur.osdmconverter.client.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocaleDateTimeAdapter : TypeAdapter<LocalDateTime>() {
  override fun write(out: JsonWriter?, value: LocalDateTime?) {
    if (value == null) {
      out?.nullValue()
      return
    }
    out?.value(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").format(value))
  }

  override fun read(`in`: JsonReader?): LocalDateTime? {
    val reader = `in` ?: throw IOException()
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull()
      return null
    }
    return LocalDateTime.parse(
        reader.nextString(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
  }
}
