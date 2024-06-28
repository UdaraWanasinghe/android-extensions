package com.aureusapps.android.extensions.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import com.aureusapps.android.extensions.closeQuietly
import com.aureusapps.android.extensions.openOutputStream
import com.aureusapps.android.providerfile.ProviderFile
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.util.UUID

object TestHelpers {
    @JvmStatic
    fun generateFiles(root: File, nodes: List<Node>) {
        root.mkdir()
        for (node in nodes) {
            val file = File(root, node.name)
            if (node is DirectoryNode) {
                file.mkdirs()
                generateFiles(file, node.children)
            } else {
                file.createNewFile()
            }
        }
    }

    @JvmStatic
    fun getAndroidResourceUri(context: Context, resId: Int): Uri {
        // Accepted Uris
        // android.resource://{packageName}/{resId}
        // android.resource://{packageName}/{typeName}/{resId}
        // android.resource://{packageName}/{typeName}/{entryName}
        val scheme = ContentResolver.SCHEME_ANDROID_RESOURCE
        val packageName = context.resources.getResourcePackageName(resId)
        val typeName = context.resources.getResourceTypeName(resId)
        val entryName = context.resources.getResourceEntryName(resId)
        return Uri.parse("$scheme://$packageName/$typeName/$entryName")
    }

    @JvmStatic
    fun createTempDirectory(): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.createTempDirectory("tmp").toFile()
        } else {
            createTempDir("tmp")
        }
    }

    @JvmStatic
    fun generateTempFiles(targetParent: File): List<Pair<File, File>> {
        val root = createTempDirectory()
        TestHelpers.generateFiles(
            root,
            listOf(
                FileNode("file1"),
                FileNode("text1.txt"),
                FileNode("zip1.tar.gz"),
                DirectoryNode(
                    "dir1",
                    listOf(
                        FileNode("text2.txt")
                    )
                )
            )
        )
        val file1 = File(root, "file1")
        file1.writeText("file1")
        val text1 = File(root, "text1")
        text1.writeText("text1")
        val zip1 = File(root, "zip1.tar.gz")
        zip1.writeText("zip1")
        val dir1 = File(root, "dir1")
        val text2 = File(dir1, "text2.txt")
        val targetDir = File(targetParent, root.name)
        return listOf(
            root to targetDir,
            file1 to File(targetDir, "file1"),
            text1 to File(targetDir, "text1.txt"),
            zip1 to File(targetDir, "zip1.tar.gz"),
            dir1 to File(targetDir, "dir1"),
            text2 to File(targetDir, "dir1/text2.txt")
        )
    }

    @Suppress("SameParameterValue")
    fun genRandomName(extension: String? = null): String {
        return UUID.randomUUID().toString().let {
            if (extension != null) {
                "$it.$extension"
            } else {
                it
            }
        }
    }

    fun addFilesToProviderFile(
        context: Context,
        parentFile: ProviderFile,
        fileNodes: List<Node>,
    ) {
        for (fileNode in fileNodes) {
            when (fileNode) {
                is FileNode -> {
                    val displayName = fileNode.name.substringBeforeLast(".")
                    val extension = fileNode.name.substringAfterLast(".")
                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)!!
                    val providerFile = parentFile.createFile(mimeType, displayName)!!
                    if (fileNode.content != null) {
                        writeStringToProviderFile(context, providerFile, fileNode.content)
                    }
                }

                is DirectoryNode -> {
                    val providerDir = parentFile.createDirectory(fileNode.name)!!
                    addFilesToProviderFile(context, providerDir, fileNode.children)
                }
            }
        }
    }

    private fun writeStringToProviderFile(context: Context, providerFile: ProviderFile, content: String) {
        var output: OutputStream? = null
        try {
            output = providerFile.openOutputStream(context, "wt")!!
            output.write(content.toByteArray())
        } finally {
            output?.flush()
            output?.closeQuietly()
        }
    }

    sealed class Node {
        abstract val name: String
    }

    data class FileNode(
        override val name: String,
        val content: String? = null,
    ) : Node()

    data class DirectoryNode(
        override val name: String,
        val children: List<Node>,
    ) : Node()
}