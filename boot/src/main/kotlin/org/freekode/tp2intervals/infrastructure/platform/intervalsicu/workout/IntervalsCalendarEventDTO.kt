package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.workout

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.LocalDateTime

/** Identity and marker fields shared by calendar listings and event-by-ID reads. */
@JsonIgnoreProperties(ignoreUnknown = true)
class IntervalsCalendarEventDTO(
    @JsonProperty(required = true)
    @JsonDeserialize(using = CalendarEventIdDeserializer::class)
    val id: Long,
    @JsonDeserialize(using = CalendarTextDeserializer::class) val description: String?,
    val start_date_local: LocalDateTime,
    @JsonDeserialize(using = CalendarTextDeserializer::class) val category: String,
    @JsonDeserialize(using = CalendarTextDeserializer::class) val type: String?,
) {
    internal fun isValid() = id > 0 && category.isNotBlank()
}

// Jackson normally coerces numeric and textual scalars. Marker and identity fields must retain their JSON types.
internal class CalendarEventIdDeserializer : JsonDeserializer<Long>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Long =
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) parser.longValue
        else context.reportInputMismatch(Long::class.java, "Expected an integer event ID")
}

internal class CalendarTextDeserializer : JsonDeserializer<String>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): String =
        if (parser.hasToken(JsonToken.VALUE_STRING)) parser.text
        else context.reportInputMismatch(String::class.java, "Expected a calendar text field")
}
