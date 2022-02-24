package ru.spbsu.kotlin

import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.experimental.xor

class DecodeStream(rootFolder: File) : InputStream() {
    private data class InfoForDecode(
        val password: List<Int> = arrayListOf(),
        val message: ByteArray = byteArrayOf()
    ) {
        operator fun plus(info: InfoForDecode): InfoForDecode {
            return InfoForDecode(password + info.password, message + info.message)
        }
    }

    private var info = InfoForDecode()
    private var posInMessage = 0

    init {
        posInMessage = 0
        val rawInfo = readDirectory(rootFolder)
        info = InfoForDecode(rawInfo.password.takeWhile { it != 0 }, rawInfo.message)
    }

    private fun readDirectory(rootFolder: File): InfoForDecode {
        if (!rootFolder.exists()) {
            return InfoForDecode()
        }
        require(rootFolder.isDirectory) { "rootFolder is not a dir" }

        val (directories, files) = rootFolder.walk()
            .toList()
            .filter { it.absolutePath.count { it == '/' } == rootFolder.absolutePath.count { it == '/' } + 1 }
            .sortedBy { it.name }
            .partition { it.isDirectory }
        var info = InfoForDecode()
        directories.forEach { dir ->
            info += readDirectory(dir)
        }
        files.forEach { file ->
            info += InfoForDecode(listOf((Date(file.lastModified()).seconds % 10)), file.readBytes())
        }
        return info
    }

    override fun read(): Int {
        if (posInMessage == info.message.size) {
            return -1
        }
        require(info.password.isNotEmpty()) { "password is empty" }
        val curChar = info.message[posInMessage].xor(info.password[posInMessage % info.password.size].toByte())
        posInMessage++
        return curChar.toInt()
    }
}
