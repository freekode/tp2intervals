package org.freekode.tp2intervals.app.schedule

import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleCrudRepository
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestEntity
import org.springframework.stereotype.Service

@Service
class ScheduleService(
    private val scheduleCrudRepository: ScheduleCrudRepository,
    private val objectMapper: ObjectMapper,
) {
    fun addScheduledRequest(request: Any) {
        val className = request.javaClass.name
        if (scheduleCrudRepository.findByClassName(className).isEmpty()) {
            throw IllegalArgumentException("Can't add another request of the same type")
        }

        val json = objectMapper.writeValueAsString(request)
        val entity = ScheduleRequestEntity()
        entity.className = className
        entity.json = json
        scheduleCrudRepository.save(entity)
    }

    fun <T> getScheduledRequest(clazz: Class<T>): T? {
        return scheduleCrudRepository.findByClassName(clazz.name)
            .map { objectMapper.readValue(it.json, clazz) }
            .firstOrNull()
    }
}
