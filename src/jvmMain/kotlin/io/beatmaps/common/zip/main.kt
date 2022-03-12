package io.beatmaps.common.zip

import com.fasterxml.jackson.module.kotlin.readValue
import io.beatmaps.common.beatsaber.BSDiff
import io.beatmaps.common.beatsaber.BSDifficulty
import io.beatmaps.common.beatsaber.BSDifficultyV3
import io.beatmaps.common.beatsaber.DifficultyBeatmap
import io.beatmaps.common.beatsaber.DifficultyBeatmapSet
import io.beatmaps.common.beatsaber.MapInfo
import io.beatmaps.common.copyTo
import io.beatmaps.common.jackson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import net.sourceforge.lame.lowlevel.LameEncoder
import net.sourceforge.lame.mp3.Lame
import net.sourceforge.lame.mp3.MPEGMode
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.security.DigestOutputStream
import java.util.ServiceLoader
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

val KProperty0<*>.isLazyInitialized: Boolean
    get() {
        if (this !is Lazy<*>) return true

        // Prevent IllegalAccessException from JVM access check on private properties.
        val originalAccessLevel = isAccessible
        isAccessible = true
        val isLazyInitialized = (getDelegate() as Lazy<*>).isInitialized()
        // Reset access level.
        isAccessible = originalAccessLevel
        return isLazyInitialized
    }

data class ExtractedInfo(
    val allowedFiles: List<String>,
    val md: DigestOutputStream,
    var mapInfo: MapInfo,
    val score: Short,
    val diffs: MutableMap<DifficultyBeatmapSet, MutableMap<DifficultyBeatmap, BSDiff>> = mutableMapOf(),
    var duration: Float = 0f,
    var thumbnail: ByteArrayOutputStream? = null,
    var preview: ByteArrayOutputStream? = null
)

interface IMapScorer {
    fun scoreMap(infoFile: MapInfo, audio: File, block: (String) -> BSDiff): Short
}
interface IMapScorerProvider {
    fun create(): IMapScorer
}
class ZipHelperException(val msg: String) : RuntimeException()

class ZipHelper(private val fs: FileSystem, val filesOriginalCase: Set<String>, val files: Set<String>, val directories: Set<String>) : AutoCloseable {
    val infoPath: Path by lazy {
        fs.getPath(filesOriginalCase.firstOrNull { it.endsWith("/Info.dat", true) } ?: throw ZipHelperException("Missing Info.dat"))
    }

    val audioFile: File by lazy {
        val path = fromInfo(info._songFilename)
        File.createTempFile("audio", ".ogg").also { file ->
            file.deleteOnExit()

            path?.inputStream()?.use { iss ->
                file.outputStream().use {
                    iss.copyTo(it, sizeLimit = 50 * 1024 * 1024)
                }
            }
        }
    }

    private val audioInitialized = ::audioFile.isLazyInitialized

    val info by lazy {
        infoPath.inputStream().use {
            val byteArrayOutputStream = ByteArrayOutputStream()
            it.copyTo(byteArrayOutputStream, sizeLimit = 50 * 1024 * 1024)

            jackson.readValue<MapInfo>(byteArrayOutputStream.toByteArray())
        }
    }

    fun infoPrefix(): String = infoPath.parent.toString().removeSuffix("/") + "/"
    fun fromInfo(path: String) = getPath(infoPrefix() + path)

    private val diffs = mutableMapOf<String, BSDiff>()
    fun diff(path: String) = diffs.getOrPut(path) {
        (fromInfo(path) ?: throw ZipHelperException("Difficulty file missing")).inputStream().buffered().use { stream ->
            val jsonElement = Json.parseToJsonElement(stream.readAllBytes().decodeToString())

            if (jsonElement.jsonObject.containsKey("version")) {
                Json.decodeFromJsonElement<BSDifficultyV3>(jsonElement)
            } else {
                Json.decodeFromJsonElement<BSDifficulty>(jsonElement)
            }
        }
    }

    fun getPath(path: String) =
        filesOriginalCase.find { it.equals(path, true) }?.let {
            fs.getPath(it)
        }

    fun newPath(path: String): Path = fs.getPath(path)

    fun scoreMap() =
        ServiceLoader.load(IMapScorerProvider::class.java)
            .findFirst()
            .map { s ->
                s.create().scoreMap(info, audioFile) {
                    diff(it)
                }
            }.orElse(0)

    fun generatePreview() = AudioSystem.getAudioInputStream(audioFile).use { oggStream ->
        convertToPCM(
            oggStream,
            info._previewStartTime,
            10.2f
        ).use(::encodeToMp3)
    }

    private fun convertToPCM(input: AudioInputStream, skip: Float, length: Float): AudioInputStream {
        val sourceFormat = input.format
        val pcmFormat = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.sampleRate, 16, sourceFormat.channels, sourceFormat.channels * 2, sourceFormat.sampleRate, false)
        val conv = AudioSystem.getAudioInputStream(pcmFormat, input)
        conv.skip((skip * conv.format.frameSize * conv.format.frameRate).toLong())
        val framesOfAudioToCopy = (length * pcmFormat.frameRate).toLong()

        return AudioInputStream(conv, pcmFormat, framesOfAudioToCopy)
    }

    private fun encodeToMp3(audioInputStream: AudioInputStream): ByteArray {
        val format = audioInputStream.format

        val encoder = LameEncoder(format, 128, MPEGMode.STEREO, Lame.QUALITY_HIGH, false)
        val mp3 = ByteArrayOutputStream()
        val inputBuffer = ByteArray(encoder.pcmBufferSize)
        val outputBuffer = ByteArray(encoder.pcmBufferSize)

        var bytesRead: Int
        var bytesWritten: Int
        while (0 < audioInputStream.read(inputBuffer).also { bytesRead = it }) {
            bytesWritten = encoder.encodeBuffer(inputBuffer, 0, bytesRead, outputBuffer)
            mp3.write(outputBuffer, 0, bytesWritten)
        }

        encoder.close()
        return mp3.toByteArray()
    }

    override fun close() {
        if (audioInitialized) {
            Files.delete(audioFile.toPath())
        }
    }

    companion object {
        fun <T> openZip(file: File, block: ZipHelper.() -> T) =
            FileSystems.newFileSystem(file.toPath(), mapOf("create" to "false")).use { fs ->
                val lists = fs.rootDirectories.map {
                    Files.walk(it).toList().partition { p ->
                        p.isDirectory()
                    }
                }

                val files = lists.flatMap { it.second }.map { it.toString() }
                val directories = lists.flatMap { it.first }.map { it.toString() }

                ZipHelper(fs, files.toSet(), files.map { it.lowercase() }.toSet(), directories.toSet()).use(block)
            }
    }
}

operator fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>) = value
