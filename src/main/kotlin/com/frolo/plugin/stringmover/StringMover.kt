package com.frolo.plugin.stringmover

import org.jdom.Document
import org.jdom.Element
import org.jdom.JDOMException
import org.jdom.input.SAXBuilder
import org.jdom.output.Format
import org.jdom.output.XMLOutputter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*

internal class StringMover {

    @JvmOverloads
    fun moveStrings(params: Params, errorDispatcher: ErrorDispatcher = ErrorDispatcher.Rethrow) {
        val srcResFolder = params.srcModule.ensureResDir()
        if (!srcResFolder.exists() || !srcResFolder.isDirectory) {
            errorDispatcher.dispatchError(IllegalArgumentException("Src res folder not found"))
        }
        val dstResFolder = params.dstModule.ensureResDir()
        if (!dstResFolder.exists()) {
            dstResFolder.mkdirs()
        }
        if (!dstResFolder.isDirectory) {
            errorDispatcher.dispatchError(IllegalArgumentException("Dst res folder not found"))
        }
        srcResFolder.listFiles().orEmpty().forEach { child ->
            if (!child.isDirectory) {
                return@forEach
            }
            val nameParts = child.name.orEmpty().split("-")
            if (nameParts.isEmpty()) {
                return@forEach
            }
            if (nameParts[0] != DEFAULT_VALUES_DIR) {
                return@forEach
            }
            val srcFile = child.listFiles()?.find { it.name == DEFAULT_STRINGS_FILE }
            if (srcFile == null || !srcFile.exists()) {
                return@forEach
            }
//            val lang = nameParts.find { it.length == 2 }
//            val dstDirName = if (lang.isNullOrBlank()) {
//                DEFAULT_VALUES_DIR
//            } else {
//                "$DEFAULT_VALUES_DIR-$lang"
//            }
            // Preserve all the prefixes
            val dstDirName = child.name
            try {
                val dstDir = File(dstResFolder, dstDirName).apply { mkdirs() }
                val dstFile = File(dstDir, DEFAULT_STRINGS_FILE).also { checkXmlResourceFile(it) }
                moveTranslationsInXml(
                    fromXmlFile = srcFile,
                    toXmlFile = dstFile,
                    keys = params.stringKeys,
                    sourceRetentionStrategy = params.sourceRetentionStrategy,
                    conflictStrategy = params.conflictStrategy
                )
            } catch (e: Throwable) {
                errorDispatcher.dispatchError(e)
            }
        }
    }

    private fun GradleModule.ensureResDir(): File {
        val srcDir = File(this.dirPath, "src")
        val mainDir = File(srcDir, "main")
        val resDir = File(mainDir, "res")
        resDir.mkdirs()
        return resDir
    }

    @Throws(JDOMException::class, IOException::class)
    private fun moveTranslationsInXml(
        fromXmlFile: File,
        toXmlFile: File,
        keys: Set<String>,
        sourceRetentionStrategy: SourceRetentionStrategy,
        conflictStrategy: ConflictStrategy
    ) {
        val translations: MutableCollection<Element> = LinkedList<Element>()
        val builder: SAXBuilder = SAXBuilder()
        val srcXml: Document = builder.build(fromXmlFile)
        val dstXml: Document = builder.build(toXmlFile)
        // Collect translations from the src xml
        for (el in srcXml.rootElement.children) {
            if (el.name != XML_TAG_STRING) {
                continue
            }
            val name: String = el.getAttributeValue(XML_ATTR_NAME)
            if (keys.contains(name)) {
                translations.add(el)
            }
        }
        if (sourceRetentionStrategy == SourceRetentionStrategy.REMOVE) {
            // Remove translations in the src xml
            for (el in translations) {
                srcXml.rootElement.removeContent(el)
            }
        }
        // Add translations to the dst xml
        for (el in translations) {
            val name = el.getAttributeValue(XML_ATTR_NAME)
            val existingContent = dstXml.rootElement.children.find { it.getAttributeValue(XML_ATTR_NAME) == name }
            if (existingContent != null) {
                when (conflictStrategy) {
                    ConflictStrategy.IGNORE -> {
                        continue
                    }
                    ConflictStrategy.REPLACE -> {
                        dstXml.rootElement.removeContent(existingContent)
                    }
                }
            }
            val newChild: Element = Element(XML_TAG_STRING)
            newChild.setAttribute(XML_ATTR_NAME, name)
            newChild.text = el.text
            dstXml.rootElement.addContent(newChild)
        }
        // Write changes
        writeChanges(fromXmlFile, srcXml)
        writeChanges(toXmlFile, dstXml)
    }

    @Throws(IOException::class)
    private fun writeChanges(xmlFile: File, xml: Document) {
        val writer = FileWriter(xmlFile)
        val outputter: XMLOutputter = XMLOutputter()
        outputter.format = Format.getPrettyFormat().apply {
            indent = "    " // 4 spaces
        }
        outputter.output(xml, writer)
        writer.close()
    }

    private fun resolveModulePath(projectPath: String, module: String): String {
        val gradleSettingsFile = File(projectPath, "settings.gradle")
        check(gradleSettingsFile.exists()) { "settings.gradle not found" }
        return File(projectPath, module).absolutePath
    }

    @Throws(IOException::class)
    private fun checkXmlResourceFile(file: File): File {
        if (!file.exists()) {
            val created = file.createNewFile()
            check(created) { "Failed to create a new file: $file" }
            val xml: Document = Document()
            val rootElement = Element(XML_TAG_RESOURCES)
            xml.rootElement = rootElement
            writeChanges(file, xml)
        }
        return file
    }

    data class Params(
        val srcModule: GradleModule,
        val dstModule: GradleModule,
        val stringKeys: Set<String>,
        val sourceRetentionStrategy: SourceRetentionStrategy,
        val conflictStrategy: ConflictStrategy
    )

    enum class SourceRetentionStrategy {
        KEEP, REMOVE
    }

    enum class ConflictStrategy {
        IGNORE, REPLACE
    }

    companion object {
        private const val DEFAULT_VALUES_DIR = "values"
        private const val DEFAULT_STRINGS_FILE = "strings.xml"

        private const val XML_TAG_RESOURCES = "resources"
        private const val XML_TAG_STRING = "string"
        private const val XML_ATTR_NAME = "name"
    }
}