package org.freekode.tp2intervals.infrastructure.utils

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Base64
import org.springframework.web.multipart.MultipartFile


class MyMultipartFile(
    base64: String
) : MultipartFile {
    private val content: ByteArray = Base64.getDecoder().decode(base64)

    override fun getInputStream(): ByteArrayInputStream = ByteArrayInputStream(content)

    override fun getName(): String = "file"

    override fun getOriginalFilename(): String? = null

    override fun getContentType(): String? = null

    override fun isEmpty(): Boolean = content.isEmpty()

    override fun getSize(): Long = content.size.toLong()

    override fun getBytes(): ByteArray = content

    override fun transferTo(dest: File) {
        FileOutputStream(dest).use { fos ->
            fos.write(content)
        }
    }
}
