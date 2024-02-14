package org.entur.osdmconverter.client.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateAdapter : TypeAdapter<LocalDate>() {
  override fun write(out: JsonWriter?, value: LocalDate?) {
    if (value == null) {
      out?.nullValue()
      return
    }
    out?.value(DateTimeFormatter.ISO_LOCAL_DATE.format(value))
  }

  override fun read(`in`: JsonReader?): LocalDate? {
    val reader = `in` ?: throw IOException()
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull()
      return null
    }
    return LocalDate.parse(reader.nextString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  }
}
