package org.freekode.tp2intervals.infrastructure.utils

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Base64
import org.springframework.util.FileCopyUtils
import org.springframework.web.multipart.MultipartFile


class MyMultipartFile(
    private val name: String,
    base64: String
) : MultipartFile {
    private val content: ByteArray = Base64.getDecoder().decode(base64)

    override fun getInputStream(): ByteArrayInputStream = ByteArrayInputStream(content)

    override fun getName(): String = name

    override fun getOriginalFilename(): String = ""

    override fun getContentType(): String? = null

    override fun isEmpty(): Boolean = content.isEmpty()

    override fun getSize(): Long = content.size.toLong()

    override fun getBytes(): ByteArray = content

    override fun transferTo(dest: File) {
        FileCopyUtils.copy(content, dest)
    }
}
