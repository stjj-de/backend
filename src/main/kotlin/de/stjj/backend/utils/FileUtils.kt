package de.stjj.backend.utils

import de.stjj.backend.models.UploadedFile
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.security.MessageDigest

fun getFileForUploadedFile(uploadedFile: UploadedFile): File = Paths.get(dataDir, "uploads", uploadedFile.id.value + (uploadedFile.mimeType?.extension ?: "")).toFile()

private val digest = MessageDigest.getInstance("SHA-256")

fun getSha256OfFile(file: File): String {
    val inputStream = FileInputStream(file)

    val data = ByteArray(1024)
    var bytesCount: Int

    while (true) {
        bytesCount = inputStream.read(data)
        if (bytesCount == -1) break
        digest.update(data, 0, bytesCount)
    }

    inputStream.close()
    val hashBytes = digest.digest()

    val hexString = StringBuffer()
    for (i in hashBytes.indices) {
        val hex = Integer.toHexString(0xff and hashBytes[i].toInt())
        if (hex.length == 1) hexString.append('0')
        hexString.append(hex)
    }

    return hexString.toString()
}
