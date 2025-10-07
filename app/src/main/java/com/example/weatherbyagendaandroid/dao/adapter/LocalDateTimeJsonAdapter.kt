package com.example.weatherbyagendaandroid.dao.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeJsonAdapter: JsonAdapter<LocalDateTime>() {

    @FromJson
    override fun fromJson(jsonReader: JsonReader): LocalDateTime {
        val value = jsonReader.nextString()

        return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @ToJson
    override fun toJson(jsonWriter: JsonWriter, localDateTime: LocalDateTime?) {
        if(localDateTime == null) {
            jsonWriter.nullValue()
            return
        }

        jsonWriter.value(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }
}