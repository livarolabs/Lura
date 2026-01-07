package com.lura.data.engine

import com.lura.domain.engine.BookContent
import com.lura.domain.engine.Chapter
import com.lura.domain.engine.EpubParser
import com.lura.domain.engine.ReaderElement
import com.lura.domain.engine.ReaderTextStyle
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.File
import java.io.InputStream
import java.net.URLDecoder
import java.util.zip.ZipFile
import javax.inject.Inject

class RealEpubParser @Inject constructor() : EpubParser {

    override suspend fun parseBook(filePath: String): BookContent {
        val file = File(filePath)
        if (!file.exists()) throw Exception("File not found: $filePath")

        val zipFile = ZipFile(file)
        
        try {
            // 1. Find OPF path from container.xml
            val containerEntry = zipFile.getEntry("META-INF/container.xml") 
                ?: throw Exception("Invalid EPUB: Missing container.xml")
            
            val containerXml = zipFile.getInputStream(containerEntry).bufferedReader().use { it.readText() }
            val opfPath = parseContainerXml(containerXml)
            
            // 2. Parse OPF to get metadata and spine
            val opfEntry = getEntryCaseInsensitive(zipFile, opfPath) 
                ?: throw Exception("OPF file not found: $opfPath")
                
            val opfXml = zipFile.getInputStream(opfEntry).bufferedReader().use { it.readText() }
            val (manifest, spine, metadata) = parseOpf(opfXml)
            
            // 3. Resolve base path for chapters (OPF might be in a subdir like "OEBPS/")
            val opfDir = File(opfPath).parent ?: ""

            // 3a. Parse NCX (TOC) if available for better titles
            // Look for .ncx file in manifest
            val ncxId = manifest.entries.find { it.value.endsWith(".ncx", ignoreCase = true) }?.key
            val ncxHref = ncxId?.let { manifest[it] }
            val tocMap = if (ncxHref != null) {
                val ncxPath = if (opfDir.isNotEmpty()) "$opfDir/$ncxHref" else ncxHref
                val entry = getEntryCaseInsensitive(zipFile, ncxPath)
                if (entry != null) {
                    val ncxXml = zipFile.getInputStream(entry).bufferedReader().use { it.readText() }
                    parseNcx(ncxXml, ncxHref)
                } else emptyMap()
            } else emptyMap()
            
            // 4. Extract content from spine items
            val chapters = spine.mapIndexedNotNull { index, itemId ->
                val rawHref = manifest[itemId] ?: return@mapIndexedNotNull null
                val href = URLDecoder.decode(rawHref, "UTF-8")
                
                // Handle relative paths correctly
                // Handle relative paths correctly
                val chapterPath = if (opfDir.isNotEmpty()) {
                   // If href is absolute or starts with opfDir, respect it. Otherwise prepend.
                   val cleanHref = href.removePrefix("/")
                   if (cleanHref.startsWith(opfDir)) cleanHref else "$opfDir/$cleanHref"
                } else {
                    href.removePrefix("/")
                }.replace("//", "/")

                // Try to find the file
                val entry = getEntryCaseInsensitive(zipFile, chapterPath)
                if (entry == null) {
                    println("EpubParser: Chapter file not found: $chapterPath (href: $href)")
                    return@mapIndexedNotNull null
                }
                
                val parsed = zipFile.getInputStream(entry).use { stream ->
                    parseChapterHtml(stream, chapterPath)
                }
                
                // Title Strategy:
                // 1. Check TOC (NCX) map for official title
                // 2. Fallback to extracting H1/H2 from content body (parsed.elements)
                // 3. Fallback to HTML <title> tag (parsed.headTitle)
                // 4. Fallback to empty string (Hide from TOC)
                val cleanHref = href.removePrefix("/")
                val officialTitle = tocMap[cleanHref]
                
                val contentTitle = parsed.elements
                        .filterIsInstance<ReaderElement.Text>()
                        .filter { it.style == ReaderTextStyle.Title || it.style == ReaderTextStyle.Heading }
                        .firstOrNull()?.content?.take(50)

                val title = officialTitle 
                    ?: contentTitle 
                    ?: parsed.headTitle.takeIf { it.isNotBlank() } 
                    ?: ""
                
                Chapter(
                    title = title.trim(),
                    elements = parsed.elements
                )
            }

            // 5. Build final content
            val title = metadata["title"]?.ifBlank { null } ?: "Untitled Book"
            val author = metadata["creator"]?.ifBlank { null } ?: "Unknown Author"
            
            return BookContent(
                title = title,
                author = author,
                chapters = chapters
            )

        } finally {
            zipFile.close()
        }
    }

    private fun parseNcx(xml: String, ncxHref: String): Map<String, String> {
        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        val map = mutableMapOf<String, String>()
        val ncxDir = if (ncxHref.contains("/")) ncxHref.substringBeforeLast("/") else ""

        doc.select("navPoint").forEach { navPoint ->
            val label = navPoint.selectFirst("navLabel > text")?.text()?.trim()
            val src = navPoint.selectFirst("content")?.attr("src")?.let { URLDecoder.decode(it, "UTF-8") }
            
            if (!label.isNullOrEmpty() && !src.isNullOrEmpty()) {
                val resolved = if (ncxDir.isEmpty()) src else resolveRelativePath(ncxDir, src)
                val path = resolved.substringBefore("#")
                
                // Use putIfAbsent to prefer the first occurrence (e.g. Chapter title vs Subsection)
                if (!map.containsKey(path)) {
                    map[path] = label
                }
            }
        }
        return map
    }

    private fun getEntryCaseInsensitive(zipFile: ZipFile, path: String): java.util.zip.ZipEntry? {
        val exact = zipFile.getEntry(path)
        if (exact != null) return exact

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

    private data class ParsedChapter(val headTitle: String, val elements: List<ReaderElement>)

    private fun parseChapterHtml(stream: InputStream, href: String): ParsedChapter {
        val doc = Jsoup.parse(stream, "UTF-8", "")
        
        // Determine the base directory of *this* file relative to the OPF root/zip root.
        // href is like "e978.../xhtml/ch01.xhtml"
        // baseDir should be "e978.../xhtml"
        val baseDir = if (href.contains("/")) href.substringBeforeLast("/") else ""

        val elements = mutableListOf<ReaderElement>()
        val headTitle = doc.title() ?: ""
        
        val body = doc.body()
        
        // Recursively extract elements
        elements.addAll(extractNode(body, baseDir))
        
        return ParsedChapter(headTitle, elements)
    }

    private fun extractNode(node: org.jsoup.nodes.Node, baseDir: String): List<ReaderElement> {
        val results = mutableListOf<ReaderElement>()
        
        if (node is org.jsoup.nodes.TextNode) {
            val text = cleanText(node.text())
            if (text.isNotBlank()) {
                results.add(ReaderElement.Text(text, ReaderTextStyle.Body))
            }
            return results
        }
        
        if (node is org.jsoup.nodes.Element) {
            // Ignore hidden elements and page markers
            if (node.hasAttr("hidden") || node.attr("role") == "doc-pagebreak" || node.attr("aria-hidden") == "true") {
                return emptyList()
            }
            
            when (node.tagName()) {
            "img", "svg" -> {
                     // Standalone images at block level are likely chapter covers or section dividers
                     val src = node.attr("src").takeIf { it.isNotEmpty() } ?: node.attr("xlink:href")
                     if (!src.isNullOrEmpty()) {
                         val fullPath = resolveRelativePath(baseDir, src)
                         val alt = node.attr("alt")
                         results.add(ReaderElement.Image(fullPath, alt, isFullPage = true))
                     }
                }
                "h1", "h2", "h3", "h4", "h5", "h6" -> {
                    val text = cleanText(node.text())
                    if (text.isNotBlank()) {
                        results.add(ReaderElement.Text(text, ReaderTextStyle.Title))
                    }
                }
                "p", "div", "section", "blockquote", "li", "span" -> { // Added span to block logic for mixed content? 
                    // Actually, span is usually inline. But if we want to catch images inside span, we must recurse.
                    
                    // Check if this container ONLY has an image (no significant text) - treat as full-page
                    val hasOnlyImage = node.children().size == 1 && 
                                       node.children().first()?.let { it.tagName() == "img" || 
                                           (it.tagName() in listOf("a", "span", "div") && it.select("img").isNotEmpty() && it.text().isBlank()) } == true &&
                                       node.ownText().isBlank()
                    
                    if (hasOnlyImage) {
                        // This is a full-page image container
                        val imgElement = node.selectFirst("img")
                        if (imgElement != null) {
                            val src = imgElement.attr("src").takeIf { it.isNotEmpty() } ?: imgElement.attr("xlink:href")
                            if (!src.isNullOrEmpty()) {
                                val fullPath = resolveRelativePath(baseDir, src)
                                val alt = imgElement.attr("alt")
                                results.add(ReaderElement.Image(fullPath, alt, isFullPage = true))
                                return results
                            }
                        }
                    }
                    
                    val bufferedText = StringBuilder()
                    
                    node.childNodes().forEach { child ->
                        if (child is org.jsoup.nodes.TextNode) {
                            bufferedText.append(child.text())
                        } else if (child is org.jsoup.nodes.Element) {
                            if (isBlockTag(child.tagName())) {
                                // Flush buffer
                                if (bufferedText.isNotBlank()) {
                                    val cleaned = cleanText(bufferedText.toString())
                                    if (cleaned.isNotEmpty()) results.add(ReaderElement.Text(cleaned, getStyleForTag(node.tagName())))
                                    bufferedText.clear()
                                }
                                // Recurse into block child
                                results.addAll(extractNode(child, baseDir))
                            } else if (child.tagName() == "img") {
                                // Flush buffer
                                if (bufferedText.isNotBlank()) {
                                    val cleaned = cleanText(bufferedText.toString())
                                    if (cleaned.isNotEmpty()) results.add(ReaderElement.Text(cleaned, getStyleForTag(node.tagName())))
                                    bufferedText.clear()
                                }
                                // Add Image
                                val src = child.attr("src")
                                if (src.isNotEmpty()) {
                                    val fullPath = resolveRelativePath(baseDir, src)
                                    results.add(ReaderElement.Image(fullPath, child.attr("alt")))
                                }
                            } else if (child.tagName() == "br") {
                                bufferedText.append("\n")
                            } else {
                                // Inline formatting (span, b, i, etc) - simple append text
                                // IMPORTANT: Recurse into span to find images nested deep!
                                // But extractNode returns List<ReaderElement>. We can't easily append to buffer.
                                // If a span contains ONLY text/inline, we want text.
                                // If it contains an IMG, we need to break.
                                
                                // Simplified approach: Recursively extract elements from the child.
                                // If the result is ONLY text, append it.
                                // If it has images, flush buffer, add results, continue.
                                
                                val childElements = extractNode(child, baseDir)
                                childElements.forEach { el ->
                                    if (el is ReaderElement.Text && el.style == ReaderTextStyle.Body) {
                                        // Merge text into buffer to keep inline flow
                                        bufferedText.append(el.content)
                                    } else {
                                        // It's a non-body text (Title?) or Image.
                                        // Flush buffer first.
                                        if (bufferedText.isNotBlank()) {
                                            val cleaned = cleanText(bufferedText.toString())
                                            if (cleaned.isNotEmpty()) results.add(ReaderElement.Text(cleaned, getStyleForTag(node.tagName())))
                                            bufferedText.clear()
                                        }
                                        results.add(el)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Final flush
                    if (bufferedText.isNotBlank()) {
                        val cleaned = cleanText(bufferedText.toString())
                        // Use Caption style if parent was figure/figcaption? Not handled yet.
                        if (cleaned.isNotEmpty()) results.add(ReaderElement.Text(cleaned, getStyleForTag(node.tagName())))
                    }
                }
                "table" -> {
                    val rows = mutableListOf<com.lura.domain.engine.TableRow>()
                    node.select("tr").forEach { tr ->
                        val cells = mutableListOf<com.lura.domain.engine.TableCell>()
                        tr.children().forEach { cell ->
                            if (cell.tagName() == "td" || cell.tagName() == "th") {
                                val cellElements = mutableListOf<com.lura.domain.engine.ReaderElement>()
                                cell.childNodes().forEach { child ->
                                    cellElements.addAll(extractNode(child, baseDir))
                                }
                                cells.add(com.lura.domain.engine.TableCell(cellElements, cell.tagName() == "th"))
                            }
                        }
                        if (cells.isNotEmpty()) {
                            rows.add(com.lura.domain.engine.TableRow(cells))
                        }
                    }
                    if (rows.isNotEmpty()) {
                        results.add(com.lura.domain.engine.ReaderElement.Table(rows))
                    }
                }
                else -> {
                    // Generic container or unknown tag -> Recurse blindly
                    node.childNodes().forEach { child ->
                        results.addAll(extractNode(child, baseDir))
                    }
                }
            }
        }
        
        return results
    }
    
    private fun isBlockTag(tag: String): Boolean {
        // Treat div/section as block to force flush
        return tag in setOf("p", "div", "section", "blockquote", "h1", "h2", "h3", "h4", "h5", "h6", "li", "ul", "ol", "figure", "table")
    }
    
    private fun getStyleForTag(tag: String): ReaderTextStyle {
        return when(tag) {
            "h1", "h2", "h3" -> ReaderTextStyle.Title
            "blockquote" -> ReaderTextStyle.Quote
            else -> ReaderTextStyle.Body
        }
    }
    
    private fun cleanText(text: String): String {
        return text.replace("\uFFFC", "")
                   .replace("\uFFFD", "")
                   // Remove zero width space
                   .replace("\u200B", "")
                   .trim()
    }
    
    private fun resolveRelativePath(baseDir: String, relativePath: String): String {
        if (relativePath.startsWith("/")) return relativePath.removePrefix("/")
        
        val parts = baseDir.split("/").filter { it.isNotEmpty() }.toMutableList()
        val segments = relativePath.split("/")
        
        for (segment in segments) {
            if (segment == "..") {
                if (parts.isNotEmpty()) parts.removeAt(parts.lastIndex)
            } else if (segment != ".") {
                parts.add(segment)
            }
        }
        
        return parts.joinToString("/")
    }

    private fun parseContainerXml(xml: String): String {
        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        val rootfile = doc.select("rootfile").first()
        return rootfile?.attr("full-path") 
            ?: throw Exception("Could not find OPF path in container.xml")
    }

    private fun parseOpf(xml: String): Triple<Map<String, String>, List<String>, Map<String, String>> {
        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        
        val metadata = mutableMapOf<String, String>()
        // Robust selector for title (namespaced and non-namespaced)
        var title = doc.select("dc|title").text()
        if (title.isEmpty()) title = doc.select("title").text()
        if (title.isEmpty()) title = doc.getElementsByTag("dc:title").text()
        
        // Robust selector for creator/author
        var creator = doc.select("dc|creator").text()
        if (creator.isEmpty()) creator = doc.select("creator").text()
        if (creator.isEmpty()) creator = doc.getElementsByTag("dc:creator").text()

        metadata["title"] = title
        metadata["creator"] = creator

        val manifest = mutableMapOf<String, String>()
        doc.select("item").forEach { item ->
            val id = item.attr("id")
            val href = item.attr("href")
            manifest[id] = href
        }

        val spine = mutableListOf<String>()
        doc.select("itemref").forEach { itemref ->
            spine.add(itemref.attr("idref"))
        }

        return Triple(manifest, spine, metadata)
    }
}
