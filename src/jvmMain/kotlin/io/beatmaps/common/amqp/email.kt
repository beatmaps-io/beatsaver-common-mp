package io.beatmaps.common.amqp

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.Serializable
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.EmailException
import org.apache.commons.mail.SimpleEmail
import pl.jutupe.ktor_rabbitmq.RabbitMQInstance
import pl.jutupe.ktor_rabbitmq.publish
import java.util.logging.Level
import java.util.logging.Logger
import javax.mail.SendFailedException
import javax.mail.internet.AddressException

@Serializable
data class EmailInfo(val to: String, val subject: String, val body: String)

object EmailHelper {
    private val emailLogger = Logger.getLogger("bmio.Email")

    private val relayHostname: String? = System.getenv("RELAY_HOSTNAME")
    private val relayUsername: String? = System.getenv("RELAY_USERNAME")
    private val relayPassword: String? = System.getenv("RELAY_PASSWORD")

    private val isSetup = relayHostname != null

    private val authenticator = DefaultAuthenticator(relayUsername, relayPassword)

    private fun genericEmail() = SimpleEmail().apply {
        hostName = relayHostname
        setSmtpPort(465)
        setAuthenticator(authenticator)
        isSSLOnConnect = true
        setFrom("no-reply@beatsaver.com")
    }

    private fun handle(emailInfo: EmailInfo) {
        if (emailInfo.to.length < 3) {
            return
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

    internal fun listen(mq: RabbitMQInstance) =
        mq.consumeAck("email", EmailInfo.serializer()) { _, emailInfo ->
            handle(emailInfo)
        }

    internal fun queue(mq: RabbitMQInstance?, info: EmailInfo) =
        if (isSetup) {
            mq?.publish("beatmaps", "email", null, info)
        } else {
            emailLogger.warning("Email not setup")
            emailLogger.info(info.body)
        }
}

fun Application.emailQueue() =
    rabbitOptional {
        EmailHelper.listen(this)
    }

fun PipelineContext<*, ApplicationCall>.sendEmail(to: String, subject: String, body: String) =
    EmailHelper.queue(call.rb(), EmailInfo(to, subject, body))
