package com.aureusapps.android.extensions.test

import android.content.ContentResolver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
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
import com.aureusapps.android.extensions.fileExists
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
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.io.path.writeText

@RunWith(AndroidJUnit4::class)
class UriExtensionsInstrumentedTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val testDocumentRoot: Uri

    private val fileName = "sample_text.txt"
    private val fileContent = "This is a sample text"
    private val textFile = File(Environment.getExternalStorageDirectory(), fileName)
    private val cacheFile = File(context.cacheDir, "cache_file.txt")

    init {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        val testTreeUri = preferences.getString(TEST_DIR_KEY, null)?.toUri()
            ?: throw NullPointerException("Test document root is not selected.")
        val documentId = DocumentsContract.getTreeDocumentId(testTreeUri)
        testDocumentRoot = DocumentsContract.buildDocumentUriUsingTree(testTreeUri, documentId)
    }

    @After
    @Before
    fun removeResiduals() {
        // delete text file from content provider and physically
        val textFileUri = findTextFileInContentProvider()
        if (textFileUri != null) {
            deleteTextFile(textFileUri)
        }
        // delete text file in external storage
        if (textFile.exists()) {
            textFile.delete()
        }
        // delete cache file
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }

    @Test
    fun test_getContentProviderUriFileName() {
        val textFileUri = createContentProviderTextFile()
        val resultFileName = textFileUri.fileName(context)
        assertEquals(fileName, resultFileName)
    }

    @Test
    fun test_getFileName() {
        // file uri
        createExternalStorageFile()
        var name = Uri.fromFile(textFile).fileName(context)
        assertEquals(fileName, name)

        // android resource uri
        val resourceUri = TestHelpers.getAndroidResourceUri(context, R.raw.sample_text)
        name = resourceUri.fileName(context)
        assertEquals("sample_text", name)

        // http uris
        val httpUri1 = Uri.parse("http://localhost:4648/$fileName")
        val resultFileName1 = httpUri1.fileName(context)
        assertEquals(fileName, resultFileName1)
        val httpUri2 = Uri.parse("http://localhost:4648/$fileName?param1=s&param2=q")
        val resultFileName2 = httpUri2.fileName(context)
        assertEquals(fileName, resultFileName2)
    }

    @Test
    fun test_listFiles() {
        createExternalStorageFile()
        val files = textFile
            .parentFile
            ?.toUri()
            ?.listFiles(context)
        val fileListed = files?.any { it.fileName(context) == fileName } ?: false
        assertTrue(fileListed)
    }

    @Test
    fun test_isEmpty() {
        createExternalStorageFile()
        val isEmpty = textFile
            .parentFile
            ?.toUri()
            ?.isEmpty(context)

        Assert.assertFalse(isEmpty!!)
    }

    @Test
    fun test_isDirectory() {
        val rootName = UUID.randomUUID().toString()
        TestHelpers.generateFiles(
            Environment.getExternalStorageDirectory(),
            listOf(
                DirectoryNode(
                    rootName,
                    listOf(
                        FileNode(
                            UUID.randomUUID().toString()
                        )
                    )
                )
            )
        )
        val file = File(Environment.getExternalStorageDirectory(), rootName)
        val result = file
            .toUri()
            .isDirectory(context)
        file.deleteRecursively()
        assertTrue(result)
    }

    @Test
    fun test_createFile() {
        val directory = Environment.getExternalStorageDirectory()
        val name = "new_file.txt"
        val uri = directory
            .toUri()
            .createFile(context, name, "text/plain")
        Assert.assertNotNull(uri)
        assertEquals(name, uri?.fileName(context))
    }

    @Test
    fun test_createDirectory() {
        val parentDir = Environment.getExternalStorageDirectory()
        val name = System.currentTimeMillis().toString()
        val dir = File(parentDir, name)
        val dirUri = parentDir
            .toUri()
            .createDirectory(context, name)
        assertTrue(dir.exists() && dir.isDirectory)
        assertEquals(
            dir.absolutePath,
            dirUri?.path
        )
        if (dir.exists()) {
            dir.delete()
        }
    }

    @Test
    fun test_deleteRecursively() {
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
        val deleted = root
            .toUri()
            .delete(context)
        assertTrue(deleted)
        if (root.exists()) {
            root.deleteRecursively()
            Assert.fail()
        }
    }

    @Test
    fun test_exists() {
        // check file uri
        val srcUri = createExternalStorageFile().toUri()
        var exists = srcUri.exists(context)
        assertTrue(exists)

        // check http uri
        val server = hostFileContent()
        val contentUrl = server.url("hosted_text.txt").toString()
        val httpUri = Uri.parse(contentUrl)
        exists = httpUri.exists(context)
        assertTrue(exists)
        server.shutdown()
    }

    @Test
    fun test_findFile() {
        createExternalStorageFile()
        val file = Environment
            .getExternalStorageDirectory()
            .toUri()
            .findFile(context, fileName)
        Assert.assertNotNull(file)
        assertEquals(fileName, file?.fileName(context))
    }

    @Test
    fun test_fileExists() {
        createExternalStorageFile()
        val result = textFile
            .parentFile
            ?.toUri()
            ?.fileExists(context, fileName) ?: false
        assertTrue(result)
    }

    @Test
    fun test_copyUri() {
        // file uri
        val srcFile = createTempTextFile()
        var srcUri = Uri.fromFile(srcFile)
        var targetParentFile = createTempDirectory()
        var targetParentUri = targetParentFile.toUri()
        try {
            val copied = srcUri.copyTo(context, targetParentUri)
            assertTrue(copied)
            val dstFile = File(targetParentFile, srcFile.name)
            verifyFileContent(dstFile)
        } finally {
            srcFile.delete()
            targetParentFile.deleteRecursively()
        }

        // content provider uri
        val srcName = genRandomName(extension = "txt")
        srcUri = createContentProviderTextFile(fileName = srcName)
        targetParentFile = createTempDirectory()
        targetParentUri = targetParentFile.toUri()
        try {
            val copied = srcUri.copyTo(context, targetParentUri)
            assertTrue(copied)
            val dstFile = File(targetParentFile, srcName)
            verifyFileContent(dstFile)
        } finally {
            deleteContentProviderFile(srcUri)
            targetParentFile.deleteRecursively()
        }

        // android resource uri
        srcUri = TestHelpers.getAndroidResourceUri(context, R.raw.sample_text)
        targetParentFile = createTempDirectory()
        targetParentUri = targetParentFile.toUri()
        try {
            val copied = srcUri.copyTo(context, targetParentUri)
            assertTrue(copied)
            val dstFile = File(targetParentFile, "sample_text")
            verifyFileContent(dstFile, "This is a sample text")
        } finally {
            targetParentFile.deleteRecursively()
        }

        // http uri
        val server = hostFileContent()
        try {
            val fileName = "hosted_text.txt"
            val contentUrl = server.url(fileName).toString()
            srcUri = Uri.parse(contentUrl)
            targetParentFile = createTempDirectory()
            targetParentUri = targetParentFile.toUri()
            val copied = srcUri.copyTo(context, targetParentUri)
            assertTrue(copied)
            val dstFile = File(targetParentFile, fileName)
            verifyFileContent(dstFile, "Sample text")
        } finally {
            server.shutdown()
            targetParentFile.deleteRecursively()
        }
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

    @Test
    fun test_readBytes() {
        createExternalStorageFile()
        val bytes = textFile
            .toUri()
            .readBytes(context)
        if (bytes == null) {
            Assert.fail()
        } else {
            Assert.assertNotNull(bytes)
            val string = String(bytes)
            assertEquals(fileContent, string)
        }
    }

    @Test
    fun test_writeBytes() {
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
    fun test_readToBuffer() {
        createExternalStorageFile()
        val buffer = textFile
            .toUri()
            .readToBuffer(context)
        if (buffer == null) {
            Assert.fail()
        } else {
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val string = String(bytes)
            assertEquals(fileContent, string)
        }
    }

    private fun genRandomName(extension: String? = null): String {
        return UUID.randomUUID().toString().let {
            if (extension != null) {
                "$it.$extension"
            } else {
                it
            }
        }
    }

    /**
     * Returns uri of the text file in the content provider or
     * null if no such file in the content provider.
     *
     * @return Uri of the text file or null if file not in the content provider.
     */
    private fun findTextFileInContentProvider(): Uri? {
        val textFile = File(Environment.getExternalStorageDirectory(), fileName)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA
            ),
            "${MediaStore.Files.FileColumns.DATA}=?",
            arrayOf(textFile.absolutePath),
            null
        ) ?: throw NullPointerException("Cursor cannot be null")
        if (cursor.count == 1) {
            // file exists
            cursor.moveToFirst()
            val idIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
            val fileId = cursor.getLong(idIndex)
            val fileUri = MediaStore.Files.getContentUri("external", fileId)
            cursor.close()
            return fileUri
        }
        return null
    }

    private fun createTempTextFile(text: String = "Sample text"): File {
        val file = Files.createTempFile("tmp", ".txt")
        file.writeText(text)
        return file.toFile()
    }

    private fun createTempDirectory(): File {
        return Files.createTempDirectory("tmp").toFile()
    }

    /**
     * Creates text file in the external storage and returns content uri of the file.
     * Additionally, this function will delete the existing file and written content will be verified.
     *
     * @return Content uri of the text file.
     */
    private fun createContentProviderTextFile(
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

    /**
     * Creates text file in the external storage.
     */
    private fun createExternalStorageFile(
        text: String = "Sample text"
    ): File {
        val textFile = Files.createTempFile("tmp", ".txt").toFile()
        val byteArray = text.toByteArray()
        val inputStream = ByteArrayInputStream(byteArray)
        val outputStream = FileOutputStream(textFile)
        val buffer = ByteArray(8192)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
        verifyFileContent(textFile)
        return textFile
    }

    private fun verifyFileContent(file: File, expected: String = "Sample text") {
        val text = file.readText()
        assertEquals(expected, text)
    }

    /**
     * Deletes file given by the uri.
     *
     * @param textFileUri uri of the file to delete.
     */
    private fun deleteTextFile(textFileUri: Uri) {
        when (textFileUri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                // delete from content provider
                context.contentResolver.delete(textFileUri, null, null)
                // sometimes deleting from content provider does not
                // delete physically from the device
                // delete physical file if it exists
                if (textFile.exists()) {
                    textFile.delete()
                }
            }

            ContentResolver.SCHEME_FILE -> {
                // if uri is from a file, delete it
                val file = File(textFileUri.toString().removePrefix("file://"))
                file.delete()
            }
        }
    }

    /**
     * Verify text file content given by the content provider uri.
     */
    private fun verifyContentProviderFile(fileUri: Uri, expectedText: String = "Sample text") {
        val input = context.contentResolver.openInputStream(fileUri)
            ?: throw AssertionError("Failed to open file uri.")
        val text = input.readText()
        assertEquals(expectedText, text)
    }

    private fun deleteContentProviderFile(documentUri: Uri) {
        val deleted = DocumentsContract.deleteDocument(context.contentResolver, documentUri)
        assertTrue(deleted)
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