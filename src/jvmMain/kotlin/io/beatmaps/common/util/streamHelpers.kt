package io.beatmaps.common.util

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.InputStream
import java.io.OutputStream

class CopyException(msg: String) : Exception(msg)

private suspend fun copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    sizeLimit: Long = 0,
    getBytes: suspend (ByteArray) -> Int
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = getBytes(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes

            if (sizeLimit in 0 until bytesCopied) { throw CopyException("Zip file too big") }
        }
        return@withContext bytesCopied
    }
}

suspend fun ByteReadChannel.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    sizeLimit: Long = 0
) = copyToSuspend(out, bufferSize, yieldSize, dispatcher, sizeLimit) { buffer ->
    readAvailable(buffer)
}

suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    sizeLimit: Long = 0
) = copyToSuspend(out, bufferSize, yieldSize, dispatcher, sizeLimit) { buffer ->
    read(buffer)
}

fun InputStream.copyTo(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    sizeLimit: Long = 0
): Long {
    val buffer = ByteArray(bufferSize)
    var bytesCopied = 0L
    while (true) {
        val bytes = read(buffer).takeIf { it >= 0 } ?: break
        out.write(buffer, 0, bytes)
        bytesCopied += bytes

        if (sizeLimit in 1 until bytesCopied) { throw CopyException("File too big") }
    }
    return bytesCopied
}
