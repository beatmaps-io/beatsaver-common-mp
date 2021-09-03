package io.beatmaps.common

import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail
import java.util.logging.Logger

val relayHostname: String? = System.getenv("RELAY_HOSTNAME")
val relayUsername: String? = System.getenv("RELAY_USERNAME")
val relayPassword: String? = System.getenv("RELAY_PASSWORD")
private val emailLogger = Logger.getLogger("bmio.Email")

fun sendEmail(to: String, subject: String, body: String) {
    if (relayHostname == null) {
        emailLogger.warning("Email not setup")
        emailLogger.info(body)
        return
    }

    val email = SimpleEmail()

    email.hostName = relayHostname
    email.setSmtpPort(465)
    email.setAuthenticator(DefaultAuthenticator(relayUsername, relayPassword))
    email.isSSLOnConnect = true
    email.setFrom("no-reply@beatsaver.com")
    email.subject = subject
    email.setMsg(body)
    email.addTo(to)
    email.send()
}
