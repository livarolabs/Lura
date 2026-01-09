package com.lura.data.engine

import android.content.Context
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class CoverExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Extracts cover image from EPUB file and saves it to app storage.
     * Returns the absolute path to the saved cover image, or null if not found.
     */
    suspend fun extractCover(epubFilePath: String, bookId: String): String? {
        val file = File(epubFilePath)
        if (!file.exists()) {
            android.util.Log.e("CoverExtractor", "EPUB file not found: $epubFilePath")
            return null
        }

        android.util.Log.d("CoverExtractor", "Starting cover extraction for: $epubFilePath")
        
        return try {
            ZipFile(file).use { zipFile ->
                // 1. Find OPF path
                val opfPath = findOpfPath(zipFile)
                if (opfPath == null) {
                    android.util.Log.e("CoverExtractor", "Could not find OPF path")
                    return null
                }
                android.util.Log.d("CoverExtractor", "Found OPF path: $opfPath")
                
                // 2. Parse OPF to find cover image reference
                val coverImagePath = findCoverImageInOpf(zipFile, opfPath)
                if (coverImagePath == null) {
                    android.util.Log.e("CoverExtractor", "Could not find cover image reference in OPF")
                    return null
                }
                android.util.Log.d("CoverExtractor", "Found cover image path: $coverImagePath")
                
                // 3. Extract cover image from EPUB
                val imageBytes = extractImageFromZip(zipFile, coverImagePath, opfPath)
                if (imageBytes == null) {
                    android.util.Log.e("CoverExtractor", "Could not extract image from ZIP")
                    return null
                }
                android.util.Log.d("CoverExtractor", "Extracted image: ${imageBytes.size} bytes")
                
                // 4. Save to app storage
                val savedPath = saveCoverToStorage(imageBytes, bookId, coverImagePath)
                android.util.Log.d("CoverExtractor", "Saved cover to: $savedPath")
                savedPath
            }
        } catch (e: Exception) {
            android.util.Log.e("CoverExtractor", "Error extracting cover", e)
            e.printStackTrace()
            null
        }
    }

    private fun findOpfPath(zipFile: ZipFile): String? {
        return try {
            val containerEntry = zipFile.getEntry("META-INF/container.xml") ?: return null
            val containerXml = zipFile.getInputStream(containerEntry).bufferedReader().use { it.readText() }
            val doc = Jsoup.parse(containerXml, "", Parser.xmlParser())
            doc.select("rootfile").first()?.attr("full-path")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun findCoverImageInOpf(zipFile: ZipFile, opfPath: String): String? {
        return try {
            val opfEntry = getEntryCaseInsensitive(zipFile, opfPath) ?: return null
            val opfXml = zipFile.getInputStream(opfEntry).bufferedReader().use { it.readText() }
            val doc = Jsoup.parse(opfXml, "", Parser.xmlParser())

            // Strategy 1: Look for <meta name="cover" content="cover-image-id"/>
            val coverMetaId = doc.select("meta[name=cover]").attr("content")
            if (coverMetaId.isNotEmpty()) {
                val coverItem = doc.select("item[id=$coverMetaId]").first()
                if (coverItem != null) {
                    return coverItem.attr("href")
                }
            }

            // Strategy 2: Look for item with properties="cover-image"
            val coverImageItem = doc.select("item[properties~=cover-image]").first()
            if (coverImageItem != null) {
                return coverImageItem.attr("href")
            }

            // Strategy 3: Look for common cover file names in manifest
            val commonCoverNames = listOf(
                "cover.jpg", "cover.jpeg", "cover.png", "cover.gif",
                "Cover.jpg", "Cover.jpeg", "Cover.png",
                "cover-image.jpg", "cover-image.jpeg", "cover-image.png"
            )
            
            doc.select("item").forEach { item ->
                val href = item.attr("href")
                val fileName = href.substringAfterLast("/")
                if (fileName in commonCoverNames) {
                    return href
                }
            }

            // Strategy 4: Find first image in manifest (fallback)
            val firstImage = doc.select("item[media-type^=image/]").first()
            firstImage?.attr("href")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun extractImageFromZip(zipFile: ZipFile, imagePath: String, opfPath: String): ByteArray? {
        return try {
            // Resolve relative path (image path is relative to OPF location)
            val opfDir = File(opfPath).parent ?: ""
            val fullImagePath = if (opfDir.isNotEmpty()) {
                val cleanPath = imagePath.removePrefix("/")
                if (cleanPath.startsWith(opfDir)) cleanPath else "$opfDir/$cleanPath"
            } else {
                imagePath.removePrefix("/")
            }.replace("//", "/")

            // Try to find the image entry
            val imageEntry = getEntryCaseInsensitive(zipFile, fullImagePath) ?: return null
            zipFile.getInputStream(imageEntry).readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveCoverToStorage(imageBytes: ByteArray, bookId: String, originalPath: String): String {
        // Determine file extension
        val extension = originalPath.substringAfterLast(".", "jpg")
        
        // Create covers directory
        val coversDir = File(context.filesDir, "covers")
        if (!coversDir.exists()) coversDir.mkdirs()
        
        // Save with book ID as filename
        val coverFile = File(coversDir, "$bookId.$extension")
        coverFile.writeBytes(imageBytes)
        
        return coverFile.absolutePath
    }

    private fun getEntryCaseInsensitive(zipFile: ZipFile, path: String): java.util.zip.ZipEntry? {
        // Try exact match first
        val exact = zipFile.getEntry(path)
        if (exact != null) return exact

        // Try case-insensitive search
        val target = path.replace("\\", "/")
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val entryName = entry.name.replace("\\", "/")
            if (entryName.equals(target, ignoreCase = true)) {
                return entry
            }
            if (entryName.endsWith("/$target", ignoreCase = true)) {
                return entry
            }
        }
        return null
    }
}
