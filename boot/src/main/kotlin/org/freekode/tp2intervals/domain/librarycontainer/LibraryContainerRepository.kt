package org.freekode.tp2intervals.domain.librarycontainer

import org.freekode.tp2intervals.domain.Platform
import java.time.LocalDate

interface LibraryContainerRepository {
    fun platform(): Platform

    fun createLibraryContainer(name: String, startDate: LocalDate?, isPlan: Boolean): LibraryContainer

    fun getLibraryContainers(): List<LibraryContainer>
}
