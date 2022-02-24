package ru.spbsu.kotlin

import java.io.File
import java.io.OutputStream

class EncodeStream(private val rootFolder: File, private val password: String) : OutputStream() {
    init {
        require(rootFolder.exists())
        require(password.toIntOrNull() != null && !password.contains("0"))
        require(password.isNotEmpty())
    }

    val buffer = mutableListOf<Byte>()
    var passwordPos = 0
    override fun write(b: Int) {
        buffer.add(
            (b.xor(password[passwordPos].code - '0'.code)).toByte()
        )
        passwordPos = (passwordPos + 1) % password.length
    }

    private val millisecondsInOnSecond = 1000L

    override fun close() {


        val files = password.indices.toList().map { File(rootFolder, it.toString()) }.sortedBy { it.name }
        files[0].writeBytes(buffer.toByteArray())
        files.indices.forEach { i ->
            files[i].createNewFile()
            files[i].setLastModified((password[i % password.length].code - '0'.code) * millisecondsInOnSecond)
        }
    }
}