package org.freekode.tp2intervals.app.workout

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.freekode.tp2intervals.Application
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.jdbc.core.JdbcTemplate
import java.net.Socket
import java.nio.file.Path

class ScheduleDeletionHttpTest {
    @TempDir
    lateinit var temp: Path

    private val host = "tp2intervals.example.test"
    private val origin = "https://$host"
    private val schedule = """{"types":["BIKE"],"sourcePlatform":"TRAINING_PEAKS","targetPlatform":"INTERVALS","skipSynced":true}"""
    private val database get() = temp.resolve("delete.sqlite")

    private val providers = WireMockServer(options().bindAddress("127.0.0.1").dynamicPort())

    @BeforeEach
    fun startProviders() = providers.start()

    @AfterEach
    fun stopProviders() = providers.stop()

    @Test
    fun `same-origin HTTPS deletion through the trusted proxy changes only the requested row and survives restart`() {
        app().use { context ->
            val port = context.webServer.port
            val jdbc = context.getBean(JdbcTemplate::class.java)
            jdbc.update("INSERT INTO schedule_requests(id, request_json) VALUES (3, ?), (4, ?)", schedule, schedule)
            jdbc.update("INSERT INTO config(key,value) VALUES ('fixture.secret','synthetic-only')")
            jdbc.update("""
                INSERT INTO trainingpeaks_legacy_baseline(id,source_account_id,destination_athlete_id,generation,revision,start_date,end_date,review_json,state)
                VALUES (1,'source','destination',0,'fixture','2026-11-22','2026-11-22','{}','APPROVED')
            """.trimIndent())
            jdbc.update("""
                INSERT INTO trainingpeaks_sync_ledger(source_account_id,destination_athlete_id,source_workout_id,state,remote_event_id)
                VALUES ('source','destination','workout','CONFIRMED',12)
            """.trimIndent())
            val tables = listOf("config", "trainingpeaks_sync_generation", "trainingpeaks_sync_ledger", "trainingpeaks_legacy_baseline")
            val before = tables.associateWith { jdbc.queryForList("SELECT * FROM $it") }
            assertCorsRejected(delete(port, 3, origin))
            assertCorsRejected(delete(port, 3, origin, "invalid"))
            assertCorsRejected(delete(port, 3, "https://unrelated.invalid", "https"))
            assertEquals(listOf(3, 4), ids(jdbc))
            val accepted = delete(port, 3, origin, "https")
            assertEquals(200, accepted.status, accepted.raw)
            assertEquals(200, delete(port, 3, origin, "https").status)
            assertEquals(200, delete(port, 999, null).status)
            assertEquals(listOf(4), ids(jdbc))
            before.forEach { (table, rows) -> assertEquals(rows, jdbc.queryForList("SELECT * FROM $table"), table) }
        }
        app(previewOnly = true).use { context ->
            val jdbc = context.getBean(JdbcTemplate::class.java)
            assertEquals(listOf(4), ids(jdbc))
            val rejected = delete(context.webServer.port, 4, origin, "https")
            assertEquals(403, rejected.status)
            assertTrue(rejected.raw.contains("PREVIEW_ONLY"))
            assertEquals(listOf(4), ids(jdbc))
        }
        app(trustedProxy = null).use { context ->
            assertCorsRejected(delete(context.webServer.port, 4, origin, "https"))
            assertEquals(200, delete(context.webServer.port, 4, null).status)
            assertTrue(ids(context.getBean(JdbcTemplate::class.java)).isEmpty())
        }
        providers.verify(0, anyRequestedFor(anyUrl()))
    }

    private fun ids(jdbc: JdbcTemplate) = jdbc.queryForList("SELECT id FROM schedule_requests ORDER BY id", Int::class.java)

    private fun assertCorsRejected(response: WireResponse) {
        assertEquals(403, response.status, response.raw)
        assertTrue(response.raw.contains("Invalid CORS request"))
    }

    private fun app(previewOnly: Boolean = false, trustedProxy: String? = "127[.]0[.]0[.]1") =
        SpringApplicationBuilder(Application::class.java).run(
            "--server.port=0", "--server.address=127.0.0.1", "--spring.datasource.url=jdbc:sqlite:$database",
            "--app.preview-only=$previewOnly", "--app.training-peaks.scheduled-forward-sync-enabled=false",
            "--app.training-peaks.api-url=${providers.baseUrl()}", "--app.intervals.api-url=${providers.baseUrl()}",
            "--app.trainer-road.api-url=${providers.baseUrl()}",
            "--server.forward-headers-strategy=native",
            *listOfNotNull(trustedProxy?.let { "--server.tomcat.remoteip.internal-proxies=$it" }).toTypedArray(),
            "--logging.file.name=${temp.resolve("app.log")}",
        ) as ServletWebServerApplicationContext

    private data class WireResponse(val status: Int, val raw: String)

    private fun delete(port: Int, id: Int, origin: String?, proto: String? = null): WireResponse {
        val headers = buildList {
            add("DELETE /api/workout/copy-calendar-to-calendar/schedule/$id HTTP/1.1")
            add("Host: $host")
            add("Connection: close")
            if (origin != null) add("Origin: $origin")
            if (proto != null) {
                add("X-Forwarded-Proto: $proto")
                add("X-Forwarded-Host: $host")
                add("X-Forwarded-Port: 443")
            }
        }
        return Socket("127.0.0.1", port).use { socket ->
            socket.soTimeout = 10_000
            socket.getOutputStream().write((headers.joinToString("\r\n") + "\r\n\r\n").toByteArray())
            val raw = socket.getInputStream().bufferedReader().readText()
            WireResponse(raw.substringBefore("\r\n").split(' ')[1].toInt(), raw)
        }
    }
}
