package com.aureusapps.android.extensions.test

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aureusapps.android.extensions.copyTo
import com.aureusapps.android.extensions.createDirectory
import com.aureusapps.android.extensions.createFile
import com.aureusapps.android.extensions.delete
import com.aureusapps.android.extensions.exists
import com.aureusapps.android.extensions.fileName
import com.aureusapps.android.extensions.findFile
import com.aureusapps.android.extensions.isDirectory
import com.aureusapps.android.extensions.isEmpty
import com.aureusapps.android.extensions.listFiles
import com.aureusapps.android.extensions.readBytes
import com.aureusapps.android.extensions.readText
import com.aureusapps.android.extensions.readToBuffer
import com.aureusapps.android.extensions.test.utils.TestHelpers
import com.aureusapps.android.extensions.test.utils.TestHelpers.DirectoryNode
import com.aureusapps.android.extensions.test.utils.TestHelpers.FileNode
import com.aureusapps.android.extensions.writeBytes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.file.Files
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@RunWith(AndroidJUnit4::class)
class UriExtensionsInstrumentedTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val testDocumentRoot: Uri

    init {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        val testTreeUri = preferences.getString(TEST_DIR_KEY, null)?.toUri()
            ?: throw NullPointerException("Test document root is not selected.")
        val documentId = DocumentsContract.getTreeDocumentId(testTreeUri)
        testDocumentRoot = DocumentsContract.buildDocumentUriUsingTree(testTreeUri, documentId)
    }

    @Test
    fun getFileName_fileUri() {
        val textFile = createExternalStorageFile()
        val expectedName = textFile.name
        try {
            val textFileUri = textFile.toUri()
            val actualName = textFileUri.fileName(context)
            assertEquals(expectedName, actualName)
        } finally {
            textFile.deleteRecursively()
        }
    }

    @Test
    fun getFileName_contentProviderUri() {
        val expectedName = genRandomName(extension = "txt")
        val textFileUri = createContentProviderFile(fileName = expectedName)
        try {
            val actualName = textFileUri.fileName(context)
            assertEquals(expectedName, actualName)
        } finally {
            deleteContentProviderFile(textFileUri)
        }
    }

    @Test
    fun getFileName_androidResourceUri() {
        val expectedName = "sample_text"
        val resourceUri = TestHelpers.getAndroidResourceUri(context, R.raw.sample_text)
        val actualName = resourceUri.fileName(context)
        assertEquals(expectedName, actualName)
    }

    @Test
    fun getFileName_httpUri() {
        val expectedName = "hosted_text.txt"
        // pattern 1
        val httpUri1 = Uri.parse("http://localhost:4648/$expectedName")
        var actualName = httpUri1.fileName(context)
        assertEquals(expectedName, actualName)
        // pattern 2
        val httpUri2 = Uri.parse("http://localhost:4648/$expectedName?param1=s&param2=q")
        actualName = httpUri2.fileName(context)
        assertEquals(expectedName, actualName)
    }

    @Test
    fun checkIsDirectory_fileUri() {
        val rootDir = createExternalStorageDirectory()
        TestHelpers.generateFiles(
            rootDir,
            listOf(
                DirectoryNode(
                    "dir1",
                    listOf(
                        FileNode("file1")
                    )
                )
            )
        )
        val file = File(rootDir, "dir1")
        try {
            val isDirectory = file.toUri().isDirectory(context)
            assertTrue(isDirectory)
        } finally {
            file.deleteRecursively()
        }
    }

    @Test
    fun checkIsDirectory_contentProviderUri() {
        val rootUri = testDocumentRoot
        val isDirectory = rootUri.isDirectory(context)
        assertTrue(isDirectory)
    }

    @Test
    fun getFileList_fileUri() {
        val root = createExternalStorageDirectory()
        TestHelpers.generateFiles(
            root,
            listOf(
                FileNode("file1"),
                DirectoryNode(
                    "dir1",
                    listOf(
                        FileNode("file2")
                    )
                ),
                FileNode("file3")
            )
        )
        try {
            val rootUri = root.toUri()
            val fileList = rootUri.listFiles(context) ?: emptyList()
            val file1Uri = rootUri.buildUpon().appendPath("file1").build()
            val dir1Uri = rootUri.buildUpon().appendPath("dir1").build()
            val file2Uri = rootUri.buildUpon().appendPath("file3").build()
            assertArrayEquals(
                arrayOf(file1Uri, dir1Uri, file2Uri),
                fileList.toTypedArray()
            )
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun getFileList_contentProviderUri() {
        val parentUri = testDocumentRoot
        var childUri: Uri? = null
        try {
            val fileName = genRandomName(extension = "txt")
            childUri = createContentProviderFile(fileName = fileName)
            val actualUris = parentUri.listFiles(context)
                ?: throw AssertionError("File list is null.")
            assertTrue(actualUris.contains(childUri))
            val expectedUris = getContentProviderFileList(parentUri)
            assertTrue(expectedUris.containsAll(actualUris))
        } finally {
            childUri?.let { deleteContentProviderFile(it) }
        }
    }

    @Test
    fun checkIsEmpty_fileUri() {
        val tmpDir = createExternalStorageDirectory()
        try {
            File(tmpDir, "file1.txt").createNewFile()
            val isEmpty = tmpDir.toUri().isEmpty(context)
            assertFalse(isEmpty)
        } finally {
            tmpDir.deleteRecursively()
        }
    }

    @Test
    fun createFile_fileUri() {
        val tmpDir = createExternalStorageDirectory()
        try {
            val fileName = "new_file.txt"
            val fileUri = tmpDir.toUri().createFile(context, fileName)
            assertNotNull(fileUri)
            val newFile = File(tmpDir, fileName)
            assertTrue(newFile.exists() && newFile.isFile)
        } finally {
            tmpDir.deleteRecursively()
        }
    }

    @Test
    fun createFile_contentProviderUri() {
        val parentUri = testDocumentRoot
        var childUri: Uri? = null
        val fileName = genRandomName(extension = "txt")
        try {
            var error: String? = null
            childUri = parentUri.createFile(context, fileName) {
                error = it.message
            } ?: throw AssertionError("File uri is null: $error")
            val exists = checkContentProviderFileExist(childUri)
            assertTrue(exists)
        } finally {
            childUri?.let { deleteContentProviderFile(it) }
        }
    }

    @Test
    fun createDirectory_fileUri() {
        val tmpDir = createExternalStorageDirectory()
        try {
            val dirName = "new_dir"
            val dirUri = tmpDir.toUri().createDirectory(context, dirName)
            assertNotNull(dirUri)
            val childDir = File(tmpDir, dirName)
            assertTrue(childDir.exists() && childDir.isDirectory)
        } finally {
            tmpDir.deleteRecursively()
        }
    }

    @Test
    fun createDirectory_contentProviderUri() {
        val tmpDir = testDocumentRoot
        var dirUri: Uri? = null
        try {
            val dirName = "new_dir"
            var error: String? = null
            dirUri = tmpDir.createDirectory(context, dirName) {
                error = it.message
            } ?: throw AssertionError("Dir uri is null: $error")
            val exists = checkContentProviderFileExist(dirUri)
            assertTrue(exists)
        } finally {
            dirUri?.let { deleteContentProviderFile(it) }
        }
    }

    @Test
    fun checkExists_fileUri() {
        val tmpDir = createExternalStorageDirectory()
        try {
            val dirUri = tmpDir.toUri()
            val exists = dirUri.exists(context)
            assertTrue(exists)
        } finally {
            tmpDir.deleteRecursively()
        }
    }

    @Test
    fun checkExists_httpUri() {
        val server = hostFileContent()
        try {
            val contentUrl = server.url("hosted_text.txt").toString()
            val httpUri = Uri.parse(contentUrl)
            val exists = httpUri.exists(context)
            assertTrue(exists)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun findFile_fileUri() {
        val tmpDir = createExternalStorageDirectory()
        try {
            val fileName = "tmp.txt"
            val expectedFile = File(tmpDir, "tmp.txt")
            expectedFile.createNewFile()
            val fileUri = tmpDir.toUri()
            val foundUri = fileUri.findFile(context, fileName)
            assertNotNull(foundUri)
        } finally {
            tmpDir.deleteRecursively()
        }
    }

    @Test
    fun findFile_contentProviderUri() {
        val parentUri = testDocumentRoot
        val fileName = genRandomName(extension = "txt")
        val childUri = createContentProviderFile(fileName = fileName)
        try {
            val foundUri = parentUri.findFile(context, fileName)
            assertNotNull(foundUri)
        } finally {
            deleteContentProviderFile(childUri)
        }
    }

    @Test
    fun copyUri_fileUri() {
        val srcFile = createExternalStorageFile()
        val srcUri = Uri.fromFile(srcFile)
        val targetParentFile = createExternalStorageDirectory()
        val targetParentUri = targetParentFile.toUri()
        try {
            val copied = srcUri.copyTo(context, targetParentUri)
            assertTrue(copied)
            val dstFile = File(targetParentFile, srcFile.name)
            verifyFileContent(dstFile)
        } finally {
            srcFile.delete()
            targetParentFile.deleteRecursively()
        }
    }

    @Test
    fun copyUri_contentProviderUri() {
        val srcName = genRandomName(extension = "txt")
        val srcUri = createContentProviderFile(fileName = srcName)
        val targetParentFile = createExternalStorageDirectory()
        val targetParentUri = targetParentFile.toUri()
        try {
            val copied = srcUri.copyTo(context, targetParentUri)
            assertTrue(copied)
            val dstFile = File(targetParentFile, srcName)
            verifyFileContent(dstFile)
        } finally {
            deleteContentProviderFile(srcUri)
            targetParentFile.deleteRecursively()
        }
    }

    @Test
    fun copyUri_androidResourceUri() {
        val srcUri = TestHelpers.getAndroidResourceUri(context, R.raw.sample_text)
        val targetParentFile = createExternalStorageDirectory()
        val targetParentUri = targetParentFile.toUri()
        try {
            val copied = srcUri.copyTo(context, targetParentUri)
            assertTrue(copied)
            val dstFile = File(targetParentFile, "sample_text")
            verifyFileContent(dstFile, "This is a sample text")
        } finally {
            targetParentFile.deleteRecursively()
        }
    }

    @Test
    fun copyUri_httpUri() {
        val server = hostFileContent()
        val fileName = "hosted_text.txt"
        val contentUrl = server.url(fileName).toString()
        val srcUri = Uri.parse(contentUrl)
        val targetParentFile = createExternalStorageDirectory()
        val targetParentUri = targetParentFile.toUri()
        try {
            val copied = srcUri.copyTo(context, targetParentUri)
            assertTrue(copied)
            val dstFile = File(targetParentFile, fileName)
            verifyFileContent(dstFile, "Sample text")
        } finally {
            server.shutdown()
            targetParentFile.deleteRecursively()
        }
    }

    @Test
    fun delete_fileUri() {
        val rootDir = createExternalStorageDirectory()
        TestHelpers.generateFiles(
            rootDir,
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
        val rootUri = rootDir.toUri()
        try {
            val deleted = rootUri.delete(context)
            assertTrue(deleted)
            assertFalse(rootDir.exists())
        } finally {
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun readBytes_fileUri() {
        val textFile = createExternalStorageFile()
        try {
            val textFileUri = textFile.toUri()
            val bytes = textFileUri.readBytes(context)
            if (bytes == null) {
                Assert.fail()
            } else {
                val string = String(bytes)
                assertEquals("Sample text", string)
            }
        } finally {
            textFile.delete()
        }
    }

    @Test
    fun writeBytes_fileUri() {
        val tempFile = File.createTempFile("text", null)
        val sampleText = "Sample"
        val written = tempFile
            .toUri()
            .writeBytes(context, sampleText.toByteArray())
        assertTrue(written)
        val text = tempFile.readText()
        assertEquals(sampleText, text)
        tempFile.delete()
    }

    @Test
    fun readToBuffer_fileUri() {
        val textFile = createExternalStorageFile()
        try {
            val buffer = textFile.toUri().readToBuffer(context)
            if (buffer == null) {
                Assert.fail()
            } else {
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                val string = String(bytes)
                assertEquals("Sample text", string)
            }
        } finally {
            textFile.delete()
        }
    }

    @Suppress("SameParameterValue")
    private fun genRandomName(extension: String?): String {
        return UUID.randomUUID().toString().let {
            if (extension != null) {
                "$it.$extension"
            } else {
                it
            }
        }
    }

    private fun createExternalStorageDirectory(): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.createTempDirectory("tmp").toFile()
        } else {
            createTempDir("tmp")
        }
    }

    private fun createExternalStorageFile(text: String = "Sample text"): File {
        val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.createTempFile("tmp", ".txt").toFile()
        } else {
            createTempFile("tmp", ".txt")
        }
        file.writeText(text)
        return file
    }

    private fun verifyFileContent(file: File, expected: String = "Sample text") {
        val text = file.readText()
        assertEquals(expected, text)
    }

    private fun createContentProviderFile(
        fileName: String = genRandomName(extension = "txt"),
        text: String = "Sample text"
    ): Uri {
        val textFileUri = DocumentsContract.createDocument(
            context.contentResolver,
            testDocumentRoot,
            "text/plain",
            fileName
        ) ?: throw AssertionError("Couldn't create text file.")

        val outputStream = context.contentResolver.openOutputStream(textFileUri)
            ?: throw AssertionError("Could not open output stream.")
        val writer = outputStream.writer()
        writer.write(text)
        writer.flush()
        writer.close()
        verifyContentProviderFile(textFileUri, text)
        return textFileUri
    }

    private fun verifyContentProviderFile(fileUri: Uri, expectedText: String = "Sample text") {
        val input = context.contentResolver.openInputStream(fileUri)
            ?: throw AssertionError("Failed to open file uri.")
        val text = input.readText()
        assertEquals(expectedText, text)
    }

    private fun checkContentProviderFileExist(fileUri: Uri): Boolean {
        val cursor = context.contentResolver.query(
            fileUri,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
            null,
            null,
            null
        )
        return cursor?.use { it.count > 0 } ?: false
    }

    private fun getContentProviderFileList(dirUri: Uri): List<Uri> {
        val documentId = DocumentsContract.getDocumentId(dirUri)
        val documentsUri = DocumentsContract.buildChildDocumentsUriUsingTree(dirUri, documentId)
        val cursor = context.contentResolver.query(
            documentsUri,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
            null,
            null,
            null
        )
        return cursor?.use {
            val uris = mutableListOf<Uri>()
            while (it.moveToNext()) {
                val id = it.getString(0)
                uris.add(
                    DocumentsContract.buildDocumentUriUsingTree(dirUri, id)
                )
            }
            uris
        } ?: emptyList()
    }

    private fun deleteContentProviderFile(documentUri: Uri) {
        val deleted = DocumentsContract.deleteDocument(context.contentResolver, documentUri)
        assertTrue(deleted)
    }

    private fun hostFileContent(text: String = "Sample text"): MockWebServer {
        val server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "text/plain; charset=utf-8")
                    .setBody(text)
            }
        }
        server.start()
        return server
    }

    companion object {

        const val PREFERENCES_NAME = "URI_TEST"
        const val TEST_DIR_KEY = "TEST_DIR_KEY"

        @JvmStatic
        @OptIn(ExperimentalCoroutinesApi::class)
        @BeforeClass
        fun setUp(): Unit = runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val preferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
            val testDir = preferences.getString(TEST_DIR_KEY, null)
            if (testDir == null) {
                val uri = suspendCoroutine { cont ->
                    SelectTestDirectoryActivity.onDocumentSelected = { uri ->
                        cont.resume(uri)
                    }
                    val intent = Intent(context, SelectTestDirectoryActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                SelectTestDirectoryActivity.onDocumentSelected = null
                if (uri != null) {
                    val uriString = uri.toString()
                    preferences
                        .edit()
                        .putString(TEST_DIR_KEY, uriString)
                        .commit()
                }
            }
        }

    }

    class SelectTestDirectoryActivity : ComponentActivity() {
        companion object {
            var onDocumentSelected: ((Uri?) -> Unit)? = null
        }

        private val openDocumentTreeContract = ActivityResultContracts.OpenDocumentTree()
        private val documentSelector = registerForActivityResult(openDocumentTreeContract) { uri ->
            onDocumentSelected?.invoke(uri)
            finish()
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            documentSelector.launch(null)
        }
    }

}