package com.example.weatherbyagendaandroid.dao.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class OffsetDateTimeJsonAdapter: JsonAdapter<OffsetDateTime>() {

    @FromJson
    override fun fromJson(jsonReader: JsonReader): OffsetDateTime {
        val value = jsonReader.nextString()

        return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    @ToJson
    override fun toJson(jsonWriter: JsonWriter, offsetDateTime: OffsetDateTime?) {
        if(offsetDateTime == null) {
            jsonWriter.nullValue()
            return
        }

        jsonWriter.value(offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
    }
}