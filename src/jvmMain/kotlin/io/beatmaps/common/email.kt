package io.beatmaps.common

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.util.pipeline.PipelineContext
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail
import java.util.logging.Logger

val relayHostname: String? = System.getenv("RELAY_HOSTNAME")
val relayUsername: String? = System.getenv("RELAY_USERNAME")
val relayPassword: String? = System.getenv("RELAY_PASSWORD")
private val emailLogger = Logger.getLogger("bmio.Email")

data class EmailInfo(val to: String, val subject: String, val body: String)

fun genericEmail() = SimpleEmail().apply {
    hostName = relayHostname
    setSmtpPort(465)
    setAuthenticator(DefaultAuthenticator(relayUsername, relayPassword))
    isSSLOnConnect = true
    setFrom("no-reply@beatsaver.com")
}

fun Application.emailQueue() {
    rabbitOptional {
        consumeAck("email", EmailInfo::class) { _, emailInfo ->
            genericEmail().apply {
                subject = emailInfo.subject
                setMsg(emailInfo.body)
                addTo(emailInfo.to)
            }.send()
        }
    }
}

fun PipelineContext<Unit, ApplicationCall>.sendEmail(to: String, subject: String, body: String) {
    if (relayHostname == null) {
        emailLogger.warning("Email not setup")
        emailLogger.info(body)
        return
    }

    call.pub("beatmaps", "email", null, EmailInfo(to, subject, body))
}
