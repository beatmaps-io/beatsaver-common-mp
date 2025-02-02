package io.beatmaps.common.zip

import io.beatmaps.common.FileLimits
import io.beatmaps.common.beatsaber.info.PreviewInfo
import io.beatmaps.common.util.copyTo
import net.lingala.zip4j.ZipFile
import net.sourceforge.lame.lowlevel.LameEncoder
import net.sourceforge.lame.mp3.Lame
import net.sourceforge.lame.mp3.MPEGMode
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.util.ServiceLoader
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

class ZipHelperWithAudio(fs: ZipFile) : ZipHelper(fs) {
    val audioFile: File
    val previewAudioFile: File
    private val previewInfo: PreviewInfo = info.getPreviewInfo()

    init {
        val songFilename = info.getSongFilename() ?: ""
        val path = fromInfo(songFilename)
        audioFile = File.createTempFile("audio", ".ogg").also { file ->
            file.deleteOnExit()

            path?.inputStream()?.use { iss ->
                file.outputStream().use {
                    iss.copyTo(it, sizeLimit = FileLimits.SONG_LIMIT)
                }
            }
        }
        previewAudioFile = if (previewInfo.filename == songFilename) {
            audioFile
        } else {
            val previewPath = fromInfo(previewInfo.filename)
            File.createTempFile("preview", ".ogg").also { file ->
                file.deleteOnExit()

                previewPath?.inputStream()?.use { iss ->
                    file.outputStream().use {
                        iss.copyTo(it, sizeLimit = FileLimits.SONG_LIMIT)
                    }
                }
            }
        }
    }

    fun generatePreview(outputStream: OutputStream, duration: Float) =
        AudioSystem.getAudioInputStream(previewAudioFile).use { oggStream ->
            convertToPCM(
                oggStream,
                if (previewInfo.start > duration) 0f else previewInfo.start,
                10.2f
            ).use {
                encodeToMp3(it, outputStream)
            }
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

    private fun encodeToMp3(audioInputStream: AudioInputStream, mp3: OutputStream) {
        val format = audioInputStream.format

        val encoder = LameEncoder(format, 128, MPEGMode.STEREO, Lame.QUALITY_HIGH, false)
        val inputBuffer = ByteArray(encoder.pcmBufferSize)
        val outputBuffer = ByteArray(encoder.pcmBufferSize)

        var bytesRead: Int
        var bytesWritten: Int
        while (0 < audioInputStream.read(inputBuffer).also { bytesRead = it }) {
            bytesWritten = encoder.encodeBuffer(inputBuffer, 0, bytesRead, outputBuffer)
            mp3.write(outputBuffer, 0, bytesWritten)
        }

        encoder.close()
    }

    override fun close() {
        if (audioFile != previewAudioFile) {
            Files.delete(previewAudioFile.toPath())
        }
        Files.delete(audioFile.toPath())
    }
}
