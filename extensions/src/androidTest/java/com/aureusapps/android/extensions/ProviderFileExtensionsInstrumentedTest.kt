package com.aureusapps.android.extensions

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aureusapps.android.extensions.utils.TestHelpers
import com.aureusapps.android.extensions.utils.TestHelpers.DirectoryNode
import com.aureusapps.android.extensions.utils.TestHelpers.FileNode
import com.aureusapps.android.providerfile.ProviderFile
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
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
        val targetParent = TestHelpers.createTempDirectory()
        targetParent.mkdirs()
        val files = TestHelpers.generateTempFiles(targetParent)
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

    @Test
    fun test_moveTo() {
        val targetFile = TestHelpers.createTempDirectory()
        targetFile.mkdirs()
        val filePairs = TestHelpers.generateTempFiles(targetFile)
        try {
            val srcProviderFile = ProviderFile.fromFile(filePairs.first().first)
            val dstProviderFile = ProviderFile.fromFile(filePairs.first().second)
            val moved = srcProviderFile.moveTo(context, dstProviderFile, true)
            assertTrue(moved)
            for (filePair in filePairs) {
                val (srcFile, dstFile) = filePair
                assertFalse("Source file exists: ${srcFile.absolutePath}", srcFile.exists())
                assertTrue("Destination file does not exists: ${dstFile.absolutePath}", dstFile.exists())
            }
        } finally {
            targetFile.deleteRecursively()
            val (root, _) = filePairs.first()
            root.deleteRecursively()
        }
    }
}