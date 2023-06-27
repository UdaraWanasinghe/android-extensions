package com.aureusapps.android.extensions.test

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aureusapps.android.extensions.test.utils.FileGenerator
import com.aureusapps.android.extensions.walkBottomUp
import com.aureusapps.android.extensions.walkTopDown
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DocumentFileExtensionsInstrumentedTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun test_walkThroughFiles() {
        val rootName = UUID.randomUUID().toString()
        val root = File(context.cacheDir, rootName)
        FileGenerator.generate(
            root,
            listOf(
                FileGenerator.FileNode("1"),
                FileGenerator.DirectoryNode(
                    "2",
                    listOf(
                        FileGenerator.FileNode("4"),
                        FileGenerator.DirectoryNode(
                            "5",
                            listOf(
                                FileGenerator.FileNode("7"),
                                FileGenerator.FileNode("8")
                            )
                        ),
                        FileGenerator.FileNode("6")
                    )
                ),
                FileGenerator.FileNode("3")
            )
        )
        val document = DocumentFile.fromFile(root)
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

}