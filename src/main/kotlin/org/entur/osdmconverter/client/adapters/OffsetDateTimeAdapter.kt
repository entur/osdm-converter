package org.entur.osdmconverter.client.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class OffsetDateTimeAdapter : TypeAdapter<OffsetDateTime>() {
  override fun write(out: JsonWriter?, value: OffsetDateTime?) {
    if (value == null) {
      out?.nullValue()
      return
    }
    out?.value(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value))
  }

  override fun read(`in`: JsonReader?): OffsetDateTime? {
    val reader = `in` ?: throw IOException()
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull()
      return null
    }
    return OffsetDateTime.parse(reader.nextString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
  }
}
