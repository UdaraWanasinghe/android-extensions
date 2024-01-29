package com.aureusapps.android.extensions.test

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aureusapps.android.extensions.copyTo
import com.aureusapps.android.extensions.test.utils.TestHelpers
import com.aureusapps.android.extensions.test.utils.TestHelpers.DirectoryNode
import com.aureusapps.android.extensions.test.utils.TestHelpers.FileNode
import com.aureusapps.android.extensions.walkBottomUp
import com.aureusapps.android.extensions.walkTopDown
import com.aureusapps.android.providerfile.ProviderFile
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.file.Files
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ProviderFileExtensionsInstrumentedTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun test_walkThroughFiles() {
        val rootName = UUID.randomUUID().toString()
        val root = File(context.cacheDir, rootName)
        TestHelpers.generateFiles(
            root,
            listOf(
                FileNode("1"),
                DirectoryNode(
                    "2",
                    listOf(
                        FileNode("4"),
                        DirectoryNode(
                            "5",
                            listOf(
                                FileNode("7"),
                                FileNode("8")
                            )
                        ),
                        FileNode("6")
                    )
                ),
                FileNode("3")
            )
        )
        val document = ProviderFile.fromFile(root)
        // top-down
        val topDownList = mutableListOf<String>()
        document.walkTopDown {
            topDownList.add(it.name ?: "")
            true
        }
        Assert.assertArrayEquals(
            arrayOf(rootName, "1", "2", "3", "4", "5", "6", "7", "8"),
            topDownList.toTypedArray()
        )

        // bottom-up
        val bottomUpList = mutableListOf<String>()
        document.walkBottomUp {
            bottomUpList.add(it.name ?: "")
            true
        }
        Assert.assertArrayEquals(
            arrayOf("1", "4", "7", "8", "5", "6", "2", "3", rootName),
            bottomUpList.toTypedArray()
        )

        // cleanup
        root.deleteRecursively()
    }

    @Test
    fun test_copyTo() {
        val targetParent = createTempDirectory()
        targetParent.mkdirs()
        val files = generateTempFiles(targetParent)
        try {
            val srcDocumentFile = ProviderFile.fromFile(files.first().first)
            val targetDocumentFile = ProviderFile.fromFile(targetParent)
            val copyResult = srcDocumentFile.copyTo(context, targetDocumentFile)
            assertTrue(copyResult)
            for (file in files) {
                val (_, dst) = file
                assertTrue("Destination does not exists: ${dst.absolutePath}", dst.exists())
            }
        } finally {
            targetParent.deleteRecursively()
            val (root, _) = files.first()
            root.deleteRecursively()
        }
    }

    private fun createTempDirectory(): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.createTempDirectory("tmp").toFile()
        } else {
            createTempDir("tmp")
        }
    }


    private fun generateTempFiles(targetParent: File): List<Pair<File, File>> {
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

}