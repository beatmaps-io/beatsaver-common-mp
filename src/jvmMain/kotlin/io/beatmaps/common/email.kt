package io.beatmaps.common

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.Serializable
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.EmailException
import org.apache.commons.mail.SimpleEmail
import java.util.logging.Level
import java.util.logging.Logger
import javax.mail.SendFailedException
import javax.mail.internet.AddressException

val relayHostname: String? = System.getenv("RELAY_HOSTNAME")
val relayUsername: String? = System.getenv("RELAY_USERNAME")
val relayPassword: String? = System.getenv("RELAY_PASSWORD")
private val emailLogger = Logger.getLogger("bmio.Email")

@Serializable
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
        consumeAck("email", EmailInfo.serializer()) { _, emailInfo ->
            if (emailInfo.to.length < 3) {
                return@consumeAck
            }

            try {
                genericEmail().apply {
                    subject = emailInfo.subject
                    setMsg(emailInfo.body)
                    addTo(emailInfo.to)
                }.send()
            } catch (e: EmailException) {
                when (val cause = e.cause) {
                    is SendFailedException -> cause.invalidAddresses.isNotEmpty()
                    is AddressException -> true
                    else -> false
                }.let { ignore ->
                    emailLogger.log(
                        if (ignore) Level.INFO else Level.WARNING,
                        "Sending email resulted in exception.\nTo: ${emailInfo.to}\nSubject: ${emailInfo.subject}\n${e.cause?.message}"
                    )

                    if (!ignore) throw e
                }
            }
        }
    }
}

fun PipelineContext<*, ApplicationCall>.sendEmail(to: String, subject: String, body: String) {
    if (relayHostname == null) {
        emailLogger.warning("Email not setup")
        emailLogger.info(body)
        return
    }

    call.pub("beatmaps", "email", null, EmailInfo(to, subject, body))
}
