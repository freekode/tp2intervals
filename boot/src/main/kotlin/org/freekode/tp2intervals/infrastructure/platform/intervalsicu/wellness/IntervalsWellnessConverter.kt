package org.freekode.tp2intervals.infrastructure.platform.intervalsicu.wellness

import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.wellness.Wellness
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class IntervalsWellnessConverter {
    private var wellnessDTO: IntervalsWellnessDTO? = null
    private var wellness: Wellness? = null

    // Construtor para quando você tem o DTO (vindo do Intervals)
    constructor(wellnessDTO: IntervalsWellnessDTO) {
        this.wellnessDTO = wellnessDTO
    }

    // Construtor para quando você tem o Domain (para enviar ao Intervals)
    constructor(wellness: Wellness?) {
        this.wellness = wellness
    }

    fun toDTO(): IntervalsWellnessDTO {
        val outputFormatter = DateTimeFormatter.ofPattern(Wellness.DATE_FORMAT)

        // 1. Calculamos a data formatada corretamente
        val formattedDate = wellness?.date?.let { dateStr ->
            try {
                val date = if (dateStr.contains("T")) {
                    java.time.OffsetDateTime.parse(dateStr).toLocalDate()
                } else {
                    // Pega os 10 primeiros caracteres e transforma em LocalDate para validar
                    LocalDate.parse(dateStr.take(10))
                }
                date.format(outputFormatter)
            } catch (e: Exception) {
                // Se falhar, tenta retornar apenas os 10 caracteres ou o que estiver disponível
                dateStr.take(10)
            }
        }

        // 2. Usamos a 'formattedDate' no campo 'id' do DTO
        return IntervalsWellnessDTO(
            id = formattedDate ?: "", // Aqui estava o erro: você usava wellness?.date
            weight = wellness?.weight
        )
    }

    fun toDomain(): Wellness {
        return Wellness(
            date = wellnessDTO?.id,
            type = TrainingType.WEIGHT.toString(),
            weight = wellnessDTO?.weight ?: -1.0
        )
    }
}
