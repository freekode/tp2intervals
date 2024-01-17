package org.freekode.tp2intervals.infrastructure.utils

import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import org.springframework.lang.NonNull
import org.springframework.lang.Nullable
import org.springframework.util.Assert
import org.springframework.util.FileCopyUtils
import org.springframework.web.multipart.MultipartFile

class MockMultipartFile(
    name: String,
    @Nullable originalFilename: String?,
    @Nullable contentType: String?,
    @Nullable content: ByteArray?
) :
    MultipartFile {
    private val name: String

    private val originalFilename: String

    @Nullable
    private val contentType: String?

    private val content: ByteArray


    /**
     * Create a new MockMultipartFile with the given content.
     * @param name the name of the file
     * @param content the content of the file
     */
    constructor(name: String, @Nullable content: ByteArray?) : this(name, "", null, content)

    /**
     * Create a new MockMultipartFile with the given content.
     * @param name the name of the file
     * @param contentStream the content of the file as stream
     * @throws IOException if reading from the stream failed
     */
    constructor(name: String, contentStream: InputStream?) : this(
        name,
        "",
        null,
        FileCopyUtils.copyToByteArray(contentStream)
    )

    /**
     * Create a new MockMultipartFile with the given content.
     * @param name the name of the file
     * @param originalFilename the original filename (as on the client's machine)
     * @param contentType the content type (if known)
     * @param content the content of the file
     */
    init {
        Assert.hasLength(name, "Name must not be empty")
        this.name = name
        this.originalFilename = (originalFilename ?: "")
        this.contentType = contentType
        this.content = (content ?: ByteArray(0))
    }

    /**
     * Create a new MockMultipartFile with the given content.
     * @param name the name of the file
     * @param originalFilename the original filename (as on the client's machine)
     * @param contentType the content type (if known)
     * @param contentStream the content of the file as stream
     * @throws IOException if reading from the stream failed
     */
    constructor(
        name: String,
        @Nullable originalFilename: String?,
        @Nullable contentType: String?,
        contentStream: InputStream?
    ) : this(name, originalFilename, contentType, FileCopyUtils.copyToByteArray(contentStream))


    override fun getName(): String {
        return this.name
    }

    @NonNull
    override fun getOriginalFilename(): String? {
        return this.originalFilename
    }

    @Nullable
    override fun getContentType(): String? {
        return this.contentType
    }

    override fun isEmpty(): Boolean {
        return (content.size == 0)
    }

    override fun getSize(): Long {
        return content.size.toLong()
    }

    @Throws(IOException::class)
    override fun getBytes(): ByteArray {
        return this.content
    }

    @Throws(IOException::class)
    override fun getInputStream(): InputStream {
        return ByteArrayInputStream(this.content)
    }

    @Throws(IOException::class, IllegalStateException::class)
    override fun transferTo(dest: File) {
        FileCopyUtils.copy(this.content, dest)
    }
}
