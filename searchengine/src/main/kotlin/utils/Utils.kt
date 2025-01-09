package utils

import com.github.luben.zstd.ZstdInputStream
import java.io.*
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream

internal fun extractZst(zstFilePath: String, outputDirectory: String) {
    val zstFile = File(zstFilePath)
    val outputDir = File(outputDirectory)

    if (!outputDir.exists()) {
        if (!outputDir.mkdirs()) {
            throw IOException("Failed to create output directory: $outputDirectory")
        }
    }

    val decompressedTarFile = File(outputDirectory, "decompressed_temp.tar")

    ZstdInputStream(FileInputStream(zstFile)).use { zstdInput ->
        FileOutputStream(decompressedTarFile).use { output ->
            zstdInput.copyTo(output)
        }
    }

    extractTarHtmlAndEn(decompressedTarFile.absolutePath, outputDirectory)

    if (!decompressedTarFile.delete()) {
        println("Warning: Failed to delete temp tar file: ${decompressedTarFile.absolutePath}")
    }
}
internal fun extractTarHtmlAndEn(tarFilePath: String, outputDirectory: String) {
    val tarFile = File(tarFilePath)
    val bufferSize = 4096

    val rootOutputDir = File(outputDirectory)
    if (!rootOutputDir.exists()) {
        if (!rootOutputDir.mkdirs()) {
            throw IOException("Failed to create output directory: ${rootOutputDir.absolutePath}")
        }
    }

    FileInputStream(tarFile).use { fis ->
        BufferedInputStream(fis).use { bis ->
            TarArchiveInputStream(bis).use { tais ->
                var entry = tais.nextTarEntry
                while (entry != null) {
                    val entryPath = entry.name
                    val normalizedPath = entryPath.trim('/')

                    if (normalizedPath.startsWith("usr/share/doc/arch-wiki/html/")) {
                        val relativePath = normalizedPath.substringAfter("usr/share/doc/arch-wiki/html/")

                        val shouldExtract = when {
                            !entry.isDirectory && !relativePath.contains('/') -> true
                            relativePath.startsWith("en/") -> true
                            else -> false
                        }

                        if (shouldExtract) {
                            val outputFile = File(rootOutputDir, sanitizeFileName(relativePath))

                            if (entry.isDirectory) {
                                if (!outputFile.exists() && !outputFile.mkdirs()) {
                                    throw IOException("Failed to create directory: ${outputFile.absolutePath}")
                                }
                            } else {
                                outputFile.parentFile?.let { parent ->
                                    if (!parent.exists() && !parent.mkdirs()) {
                                        throw IOException("Failed to create directory: ${parent.absolutePath}")
                                    }
                                }

                                FileOutputStream(outputFile).use { fos ->
                                    val buffer = ByteArray(bufferSize)
                                    var bytesRead: Int
                                    while (tais.read(buffer).also { bytesRead = it } != -1) {
                                        fos.write(buffer, 0, bytesRead)
                                    }
                                }
                            }
                        }
                    }

                    entry = tais.nextTarEntry
                }
            }
        }
    }
}

private fun sanitizeFileName(fileName: String): String {
    val illegalChars = Regex("[<>:\"/\\\\|?*]")
    return fileName.split('/').joinToString("/") { part ->
        part.replace(illegalChars, "_")
    }
}

internal fun endsWithPlusOrMinus(input: String): Boolean {
    return !input.trim().endsWith('-') && !input.trim().endsWith('+')
}

internal fun escapeDashOutsideQuotes(queryString: String): String {
    val regex = Regex("\".*?\"")
    val escapedBuilder = StringBuilder()
    var lastEnd = 0

    regex.findAll(queryString).forEach { matchResult ->
        val beforeQuoted = queryString.substring(lastEnd, matchResult.range.first)
        escapedBuilder.append(beforeQuoted.replace("-", "\\-"))

        escapedBuilder.append(matchResult.value)

        lastEnd = matchResult.range.last + 1
    }

    val remaining = queryString.substring(lastEnd)
    escapedBuilder.append(remaining.replace("-", "\\-"))

    return escapedBuilder.toString()
}