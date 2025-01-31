package io.beatmaps.common.zip

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.FileHeader
import net.lingala.zip4j.model.ZipParameters
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface IZipPath {
    fun inputStream(): InputStream
    val fileName: String?
    val compressedSize: Long
}

class ZipPath(private val fs: ZipFile, private val originalPath: String, val header: FileHeader?) : IZipPath {
    override fun inputStream(): InputStream = fs.getInputStream(header)
    override val compressedSize = header?.compressedSize ?: -1
    private val outputStream = ByteArrayOutputStream()
    fun outputStream() = object : OutputStream() {
        override fun write(b: Int) {
            outputStream.write(b)
        }

        override fun close() {
            fs.addStream(
                ByteArrayInputStream(outputStream.toByteArray()),
                ZipParameters().apply {
                    fileNameInZip = originalPath
                    lastModifiedFileTime = System.currentTimeMillis()
                }
            )
        }
    }
    override val fileName = header?.fileName
    val parent = File("/$fileName").parent.replace("\\", "/").removeSuffix("/")
    fun deleteIfExists() = header?.let { fs.removeFile(it) }
}
