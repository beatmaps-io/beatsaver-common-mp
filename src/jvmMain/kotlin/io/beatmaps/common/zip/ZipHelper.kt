package io.beatmaps.common.zip

import io.beatmaps.common.FileLimits
import io.beatmaps.common.beatsaber.info.BaseMapInfo
import io.beatmaps.common.beatsaber.info.check
import io.beatmaps.common.beatsaber.map.BSDiff
import io.beatmaps.common.jsonIgnoreUnknown
import io.beatmaps.common.util.copyTo
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.io.ByteArrayOutputStream
import java.io.File

open class ZipHelper(private val fs: ZipFile) : AutoCloseable {
    val filesOriginalCase: Set<String>
    val directories: Set<String>

    init {
        val (localDirectories, localFiles) = fs.fileHeaders.partition {
            it.isDirectory
        }

        filesOriginalCase = localFiles.map { "/" + it.fileName }.toSet()
        directories = localDirectories.map { "/" + it.fileName }.toSet()
    }

    val files = filesOriginalCase.map { it.lowercase() }.toSet()

    val infoPath by lazy {
        getPathDirect(filesOriginalCase.filter { it.endsWith("/info.dat", true) }.minByOrNull { it.length } ?: throw ZipHelperException("Missing Info.dat"))
    }

    val info = infoPath.inputStream().use {
        val byteArrayOutputStream = ByteArrayOutputStream()
        it.copyTo(byteArrayOutputStream, sizeLimit = FileLimits.DIFF_LIMIT)

        readFromBytes(byteArrayOutputStream.toByteArray()).let { str ->
            jsonIgnoreUnknown.parseToJsonElement(str).let { jsonElement ->
                BaseMapInfo.parse(jsonElement).check()
            }
        }
    }

    fun infoPrefix(): String = infoPath.parent + "/"
    fun fromInfo(path: String) = getPath(infoPrefix() + path)

    private val diffs = mutableMapOf<String, BSDiff>()
    fun diff(path: String) = diffs.getOrPut(path) {
        (fromInfo(path) ?: throw ZipHelperException("Difficulty file missing")).inputStream().buffered().use { stream ->
            val jsonElement = jsonIgnoreUnknown.parseToJsonElement(readFromStream(stream))

            BSDiff.parse(jsonElement).check()
        }
    }

    fun getPath(path: String) =
        filesOriginalCase.find { it.equals(path, true) }?.let {
            getPathDirect(it)
        }

    fun getPathDirect(path: String) = path.removePrefix("/").let { op -> ZipPath(fs, op, fs.getFileHeader(op)) }

    fun moveFile(old: ZipPath?, new: String) = if (old != null) {
        fs.renameFile(old.header, new.removePrefix("/"))
    } else {
        Unit
    }

    open fun scoreMap(): Short = 0

    override fun close() {
        // Do nothing
    }

    companion object {
        private fun <T> catchZipErrors(file: File, block: () -> T) = try {
            block()
        } catch (e: ZipException) {
            if (file.exists()) {
                file.inputStream().use {
                    if (String(it.readNBytes(4)) == "Rar!") throw RarException()
                }
            }
            throw ZipHelperException("Error opening zip file")
        }

        fun <T> openZipNoAudio(file: File, block: ZipHelper.() -> T) = catchZipErrors(file) {
            ZipHelper(ZipFile(file)).use(block)
        }

        fun <T> openZip(file: File, block: ZipHelperWithAudio.() -> T) = catchZipErrors(file) {
            ZipHelperWithAudio(ZipFile(file)).use(block)
        }
    }
}
