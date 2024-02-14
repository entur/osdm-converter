package org.entur.osdmconverter.client.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZonedDateTimeAdapter : TypeAdapter<ZonedDateTime>() {
  override fun write(out: JsonWriter?, value: ZonedDateTime?) {
    if (value == null) {
      out?.nullValue()
      return
    }
    out?.value(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(value))
  }

  override fun read(`in`: JsonReader?): ZonedDateTime? {
    val reader = `in` ?: throw IOException()
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull()
      return null
    }
    return ZonedDateTime.parse(reader.nextString(), DateTimeFormatter.ISO_ZONED_DATE_TIME)
  }
}
