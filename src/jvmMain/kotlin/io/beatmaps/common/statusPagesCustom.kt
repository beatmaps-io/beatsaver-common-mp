package io.beatmaps.common

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.Hook
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.CallFailed
import io.ktor.server.application.hooks.ResponseBodyReadyForSend
import io.ktor.server.application.isHandled
import io.ktor.server.logging.mdcProvider
import io.ktor.server.request.uri
import io.ktor.util.AttributeKey
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.pipeline.PipelinePhase
import io.ktor.util.reflect.instanceOf
import kotlin.reflect.KClass

private val LOGGER = KtorSimpleLogger("io.beatmaps.common.StatusPagesCustom")

typealias HandlerFunction = suspend (call: ApplicationCall, cause: Throwable) -> Unit

val StatusPagesCustom: ApplicationPlugin<StatusPagesCustomConfig> = createApplicationPlugin(
    "StatusPagesCustom",
    ::StatusPagesCustomConfig
) {
    val statusPageMarker = AttributeKey<Unit>("StatusPagesTriggered")

    val exceptions = HashMap(pluginConfig.exceptions)
    val statuses = HashMap(pluginConfig.statuses)
    val unhandled = pluginConfig.unhandled

    fun findHandlerByValue(cause: Throwable): HandlerFunction? {
        val keys = exceptions.keys.filter { cause.instanceOf(it) }
        if (keys.isEmpty()) return null

        if (keys.size == 1) {
            return exceptions[keys.single()]
        }

        val key = selectNearestParentClass(cause, keys)
        return exceptions[key]
    }

    on(ResponseBodyReadyForSend) { call, content ->
        if (call.attributes.contains(statusPageMarker)) return@on

        val status = content.status ?: call.response.status()
        if (status == null) {
            LOGGER.trace("No status code found for call: ${call.request.uri}")
            return@on
        }

        val handler = statuses[status]
        if (handler == null) {
            LOGGER.trace("No handler found for status code {} for call: {}", status, call.request.uri)
            return@on
        }

        call.attributes.put(statusPageMarker, Unit)
        try {
            LOGGER.trace("Executing {} for status code {} for call: {}", handler, status, call.request.uri)
            handler(call, content, status)
        } catch (cause: Throwable) {
            LOGGER.trace("Exception {} while executing {} for status code {} for call: {}", cause, handler, status, call.request.uri)
            call.attributes.remove(statusPageMarker)
            throw cause
        }
    }

    on(CallFailed) { call, cause ->
        if (call.attributes.contains(statusPageMarker)) return@on

        LOGGER.trace("Call ${call.request.uri} failed with cause $cause")

        val handler = findHandlerByValue(cause)
        if (handler == null) {
            LOGGER.trace("No handler found for exception: {} for call {}", cause, call.request.uri)
            throw cause
        }

        call.attributes.put(statusPageMarker, Unit)
        call.application.mdcProvider.withMDCBlock(call) {
            LOGGER.trace("Executing {} for exception {} for call {}", handler, cause, call.request.uri)
            handler(call, cause)
        }
    }

    on(BeforeFallback) { call ->
        if (call.isHandled) return@on
        unhandled(call)
    }
}

class StatusPagesCustomConfig {
    /**
     * Provides access to exception handlers of the exception class.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.plugins.statuspages.StatusPagesConfig.exceptions)
     */
    val exceptions: MutableMap<KClass<*>, HandlerFunction> = mutableMapOf()

    /**
     * Provides access to status handlers based on a status code.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.plugins.statuspages.StatusPagesConfig.statuses)
     */
    val statuses: MutableMap<
        HttpStatusCode,
        suspend (call: ApplicationCall, content: OutgoingContent, code: HttpStatusCode) -> Unit
        > =
        mutableMapOf()

    internal var unhandled: suspend (ApplicationCall) -> Unit = {}

    /**
     * Register an exception [handler] for the exception type [T] and its children.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.plugins.statuspages.StatusPagesConfig.exception)
     */
    inline fun <reified T : Throwable> exception(
        noinline handler: suspend (call: ApplicationCall, cause: T) -> Unit
    ): Unit = exception(T::class, handler)

    /**
     * Register an exception [handler] for the exception class [klass] and its children.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.plugins.statuspages.StatusPagesConfig.exception)
     */
    fun <T : Throwable> exception(
        klass: KClass<T>,
        handler: suspend (call: ApplicationCall, T) -> Unit
    ) {
        @Suppress("UNCHECKED_CAST")
        val cast = handler as suspend (ApplicationCall, Throwable) -> Unit

        exceptions[klass] = cast
    }

    /**
     * Register a status [handler] for the [status] code.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.plugins.statuspages.StatusPagesConfig.status)
     */
    fun status(
        vararg status: HttpStatusCode,
        handler: suspend (ApplicationCall, HttpStatusCode) -> Unit
    ) {
        status.forEach {
            statuses[it] = { call, _, code -> handler(call, code) }
        }
    }

    /**
     * Register a [handler] for the unhandled calls.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.plugins.statuspages.StatusPagesConfig.unhandled)
     */
    fun unhandled(handler: suspend (ApplicationCall) -> Unit) {
        unhandled = handler
    }

    /**
     * Register a status [handler] for the [status] code.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.plugins.statuspages.StatusPagesConfig.status)
     */
    @JvmName("statusWithContext")
    fun status(
        vararg status: HttpStatusCode,
        handler: suspend StatusContext.(HttpStatusCode) -> Unit
    ) {
        status.forEach {
            statuses[it] = { call, content, code -> handler(StatusContext(call, content), code) }
        }
    }

    /**
     * A context for [status] config method.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.plugins.statuspages.StatusPagesConfig.StatusContext)
     */
    class StatusContext(
        val call: ApplicationCall,
        val content: OutgoingContent
    )
}

internal fun selectNearestParentClass(cause: Throwable, keys: List<KClass<*>>): KClass<*>? =
    keys.minByOrNull { distance(cause.javaClass, it.java) }

private fun distance(child: Class<*>, parent: Class<*>): Int {
    var result = 0
    var current = child
    while (current != parent) {
        current = current.superclass
        result++
    }

    return result
}

internal object BeforeFallback : Hook<suspend (ApplicationCall) -> Unit> {
    override fun install(pipeline: ApplicationCallPipeline, handler: suspend (ApplicationCall) -> Unit) {
        val phase = PipelinePhase("BeforeFallback")
        pipeline.insertPhaseBefore(ApplicationCallPipeline.Fallback, phase)
        pipeline.intercept(phase) { handler(context) }
    }
}
