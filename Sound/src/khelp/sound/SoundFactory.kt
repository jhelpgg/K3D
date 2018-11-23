package khelp.sound

import khelp.io.createDirectory
import khelp.io.createFile
import khelp.io.outsideDirectory
import khelp.io.write
import khelp.resources.Resources
import khelp.sound.midi.SoundMidi
import khelp.sound.mp3.SoundMP3
import khelp.sound.other.SoundOther
import java.io.File
import java.io.InputStream
import java.net.URL

val DIRECTORY_SOUNDS: File by lazy {
    val directory = File(outsideDirectory, "media/sounds/")
    createDirectory(directory)
    directory
}

private fun soundFromFile(name: String, file: File): KHelpSound
{
    val sound =
            when (file.extension.toLowerCase())
            {
                "mp3"  -> SoundMP3(file)
                "mid"  -> SoundMidi(file)
                "midi" -> SoundMidi(file)
                else   -> SoundOther(file)
            }

    return KHelpSound(sound, name)
}

fun soundFromFile(file: File) = soundFromFile(file.absolutePath, file)

private fun soundFromStream(name: String, streamProducer: () -> InputStream, fileName: String): KHelpSound
{
    val destination = File(DIRECTORY_SOUNDS, fileName)

    if (!destination.exists())
    {
        val stream = streamProducer()
        createFile(destination)
        write(stream, destination)
        stream.close()
    }

    return soundFromFile(name, destination)
}

fun soundFromResource(resourceName: String, resources: Resources) =
        soundFromStream(resourceName, { resources.obtainResourceStream(resourceName) }, resourceName)

fun soundFromURL(url: URL): KHelpSound
{
    val name = url.toString()
    val fileName = name.replace("://", "/")
            .replace(":/", "/")
            .replace(":", "/")
            .replace("?", "/")
            .replace("&", "/")
            .replace("=", "/")
    return soundFromStream(name, { url.openStream() }, fileName)
}
