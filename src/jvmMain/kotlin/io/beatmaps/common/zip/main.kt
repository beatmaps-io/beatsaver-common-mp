package io.beatmaps.common.zip

import io.beatmaps.common.api.ECharacteristic
import io.beatmaps.common.api.EDifficulty
import io.beatmaps.common.beatsaber.BSDiff
import io.beatmaps.common.beatsaber.BSDifficulty
import io.beatmaps.common.beatsaber.BSDifficultyV3
import io.beatmaps.common.beatsaber.BSLights
import io.beatmaps.common.beatsaber.BaseMapInfo
import io.beatmaps.common.beatsaber.DifficultyBeatmap
import io.beatmaps.common.beatsaber.DifficultyBeatmapInfo
import io.beatmaps.common.beatsaber.DifficultyBeatmapSet
import io.beatmaps.common.beatsaber.SongLengthInfo
import io.beatmaps.common.copyTo
import io.beatmaps.common.jsonIgnoreUnknown
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.FileHeader
import net.lingala.zip4j.model.ZipParameters
import net.sourceforge.lame.lowlevel.LameEncoder
import net.sourceforge.lame.mp3.Lame
import net.sourceforge.lame.mp3.MPEGMode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.util.ServiceLoader
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

data class ExtractedInfo(
    val allowedFiles: List<String>,
    val toHash: ByteArrayOutputStream,
    var mapInfo: BaseMapInfo,
    val score: Short,
    val diffs: MutableMap<ECharacteristic, MutableMap<DifficultyBeatmapInfo, BSDiff>> = mutableMapOf(),
    val lights: MutableMap<ECharacteristic, MutableMap<DifficultyBeatmapInfo, BSLights>> = mutableMapOf(),
    var duration: Float = 0f,
    var thumbnail: ByteArrayOutputStream? = null,
    var preview: ByteArrayOutputStream? = null,
    var songLengthInfo: SongLengthInfo? = null
)

interface IMapScorer {
    fun scoreMap(infoFile: BaseMapInfo, audio: File, block: (String) -> BSDiff): Short
}
interface IMapScorerProvider {
    fun create(): IMapScorer
}
class RarException : ZipHelperException("")
open class ZipHelperException(val msg: String) : RuntimeException()
interface IZipPath {
    fun inputStream(): InputStream
    val fileName: String?
}

class ZipPath(private val fs: ZipFile, private val originalPath: String, val header: FileHeader?) : IZipPath {
    override fun inputStream(): InputStream = fs.getInputStream(header)
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

class ZipHelperWithAudio(fs: ZipFile, filesOriginalCase: Set<String>, directories: Set<String>) : ZipHelper(fs, filesOriginalCase, directories) {
    val audioFile: File

    init {
        val path = fromInfo(info.getSongFilename() ?: "")
        audioFile = File.createTempFile("audio", ".ogg").also { file ->
            file.deleteOnExit()

            path?.inputStream()?.use { iss ->
                file.outputStream().use {
                    iss.copyTo(it, sizeLimit = 50 * 1024 * 1024)
                }
            }
        }
    }

    fun generatePreview() = AudioSystem.getAudioInputStream(audioFile).use { oggStream ->
        convertToPCM(
            oggStream,
            // TODO: Use preview file if V4?
            //info._previewStartTime.or(0f),
            0f,
            10.2f
        ).use(::encodeToMp3)
    }

    override fun scoreMap(): Short =
        ServiceLoader.load(IMapScorerProvider::class.java)
            .findFirst()
            .map { s ->
                s.create().scoreMap(info, audioFile) {
                    diff(it)
                }
            }.orElse(0)

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
        Files.delete(audioFile.toPath())
    }
}

open class ZipHelper(private val fs: ZipFile, val filesOriginalCase: Set<String>, val directories: Set<String>) : AutoCloseable {
    val files = filesOriginalCase.map { it.lowercase() }.toSet()

    val infoPath by lazy {
        getPathDirect(filesOriginalCase.filter { it.endsWith("/info.dat", true) }.minByOrNull { it.length } ?: throw ZipHelperException("Missing Info.dat"))
    }

    val info = run {
        infoPath.inputStream().use {
            val byteArrayOutputStream = ByteArrayOutputStream()
            it.copyTo(byteArrayOutputStream, sizeLimit = 50 * 1024 * 1024)

            readFromBytes(byteArrayOutputStream.toByteArray()).let { str ->
                jsonIgnoreUnknown.parseToJsonElement(str).let { jsonElement ->
                    BaseMapInfo.parse(jsonElement)
                }
            }
        }
    }

    fun infoPrefix(): String = infoPath.parent + "/"
    fun fromInfo(path: String) = getPath(infoPrefix() + path)

    private val diffs = mutableMapOf<String, BSDiff>()
    fun diff(path: String) = diffs.getOrPut(path) {
        (fromInfo(path) ?: throw ZipHelperException("Difficulty file missing")).inputStream().buffered().use { stream ->
            val jsonElement = jsonIgnoreUnknown.parseToJsonElement(readFromStream(stream))

            BSDiff.parse(jsonElement)
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
                    String(it.readNBytes(4)) == "Rar!"
                }.let { rar ->
                    if (rar) throw RarException()
                }
            }
            throw ZipHelperException("Error opening zip file")
        }

        private fun loadZipInfo(file: File): Triple<ZipFile, Set<String>, Set<String>> = ZipFile(file).let { fs ->
            val (_directories, _files) = fs.fileHeaders.partition {
                it.isDirectory
            }

            val files = _files.map { "/" + it.fileName }.toSet()
            val directories = _directories.map { "/" + it.fileName }.toSet()

            Triple(fs, files, directories)
        }

        fun <T> openZipNoAudio(file: File, block: ZipHelper.() -> T) = catchZipErrors(file) {
            val (fs, files, directories) = loadZipInfo(file)
            ZipHelper(fs, files, directories).use(block)
        }

        fun <T> openZip(file: File, block: ZipHelperWithAudio.() -> T) = catchZipErrors(file) {
            val (fs, files, directories) = loadZipInfo(file)
            ZipHelperWithAudio(fs, files, directories).use(block)
        }
    }
}
