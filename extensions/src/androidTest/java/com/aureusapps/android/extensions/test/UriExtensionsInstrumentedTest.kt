package com.aureusapps.android.extensions.test

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
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
import com.aureusapps.android.extensions.readToBuffer
import com.aureusapps.android.extensions.test.utils.TestHelpers
import com.aureusapps.android.extensions.test.utils.TestHelpers.DirectoryNode
import com.aureusapps.android.extensions.test.utils.TestHelpers.FileNode
import com.aureusapps.android.extensions.writeBytes
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class UriExtensionsInstrumentedTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val fileName = "sample_text.txt"
    private val fileContent = "This is a sample text"
    private val textFile = File(Environment.getExternalStorageDirectory(), fileName)
    private val cacheFile = File(context.cacheDir, "cache_file.txt")

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
        val textFileUri = createTextFileInContentProvider()
        val resultFileName = textFileUri.fileName(context)
        Assert.assertEquals(fileName, resultFileName)
    }

    @Test
    fun test_getFileName() {
        // file uri
        createTextFileInExternalStorage()
        var name = Uri.fromFile(textFile).fileName(context)
        Assert.assertEquals(fileName, name)

        // android resource uri
        val resourceUri = TestHelpers.getAndroidResourceUri(context, R.raw.sample_text)
        name = resourceUri.fileName(context)
        Assert.assertEquals("sample_text", name)

        // http uris
        val httpUri1 = Uri.parse("http://localhost:4648/$fileName")
        val resultFileName1 = httpUri1.fileName(context)
        Assert.assertEquals(fileName, resultFileName1)
        val httpUri2 = Uri.parse("http://localhost:4648/$fileName?param1=s&param2=q")
        val resultFileName2 = httpUri2.fileName(context)
        Assert.assertEquals(fileName, resultFileName2)
    }

    @Test
    fun test_listFiles() {
        createTextFileInExternalStorage()
        val files = textFile
            .parentFile
            ?.toUri()
            ?.listFiles(context)
        val fileListed = files?.any { it.fileName(context) == fileName } ?: false
        Assert.assertTrue(fileListed)
    }

    @Test
    fun test_isEmpty() {
        createTextFileInExternalStorage()
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
        Assert.assertTrue(result)
    }

    @Test
    fun test_createFile() {
        val directory = Environment.getExternalStorageDirectory()
        val name = "new_file.txt"
        val uri = directory
            .toUri()
            .createFile(context, name, "text/plain")
        Assert.assertNotNull(uri)
        Assert.assertEquals(name, uri?.fileName(context))
    }

    @Test
    fun test_createDirectory() {
        val parentDir = Environment.getExternalStorageDirectory()
        val name = System.currentTimeMillis().toString()
        val dir = File(parentDir, name)
        val dirUri = parentDir
            .toUri()
            .createDirectory(context, name)
        Assert.assertTrue(dir.exists() && dir.isDirectory)
        Assert.assertEquals(
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
        Assert.assertTrue(deleted)
        if (root.exists()) {
            root.deleteRecursively()
            Assert.fail()
        }
    }

    @Test
    fun test_exists() {
        // check file uri
        createTextFileInExternalStorage()
        var exists = textFile.toUri().exists(context)
        Assert.assertTrue(exists)

        // check http uri
        val server = hostFileContent()
        val contentUrl = server.url("/$fileName").toString()
        val httpUri = Uri.parse(contentUrl)
        exists = httpUri.exists(context)
        server.shutdown()
        Assert.assertTrue(exists)
    }

    @Test
    fun test_findFile() {
        createTextFileInExternalStorage()
        val file = Environment
            .getExternalStorageDirectory()
            .toUri()
            .findFile(context, fileName)
        Assert.assertNotNull(file)
        Assert.assertEquals(fileName, file?.fileName(context))
    }

    @Test
    fun test_fileExists() {
        createTextFileInExternalStorage()
        val result = textFile
            .parentFile
            ?.toUri()
            ?.fileExists(context, fileName) ?: false
        Assert.assertTrue(result)
    }

    @Test
    fun test_copyUri() {
        // file uri
        createTextFileInExternalStorage()
        var uri = Uri.fromFile(textFile)
        uri.copyTo(context, cacheFile.toUri())
        verifyCacheFileContent()
        deleteTextFile(uri)

        // content provider uri
        uri = createTextFileInContentProvider()
        uri.copyTo(context, cacheFile.toUri())
        verifyCacheFileContent()
        deleteTextFile(uri)

        // android resource uri
        uri = TestHelpers.getAndroidResourceUri(context, R.raw.sample_text)
        uri.copyTo(context, cacheFile.toUri())
        verifyCacheFileContent()
        deleteTextFile(uri)

        // http uri
        val server = hostFileContent()
        val contentUrl = server.url("/$fileName").toString()
        uri = Uri.parse(contentUrl)
        uri.copyTo(context, cacheFile.toUri())
        verifyCacheFileContent()
        server.shutdown()
    }

    private fun hostFileContent(): MockWebServer {
        val server = MockWebServer()
        server.start()
        val buffer = Buffer()
        buffer.write(fileContent.toByteArray())
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(buffer)
        )
        return server
    }

    @Test
    fun test_readBytes() {
        createTextFileInExternalStorage()
        val bytes = textFile
            .toUri()
            .readBytes(context)
        if (bytes == null) {
            Assert.fail()
        } else {
            Assert.assertNotNull(bytes)
            val string = String(bytes)
            Assert.assertEquals(fileContent, string)
        }
    }

    @Test
    fun test_writeBytes() {
        val tempFile = File.createTempFile("text", null)
        val sampleText = "Sample"
        val written = tempFile
            .toUri()
            .writeBytes(context, sampleText.toByteArray())
        Assert.assertTrue(written)
        val text = tempFile.readText()
        Assert.assertEquals(sampleText, text)
        tempFile.delete()
    }

    @Test
    fun test_readToBuffer() {
        createTextFileInExternalStorage()
        val buffer = textFile
            .toUri()
            .readToBuffer(context)
        if (buffer == null) {
            Assert.fail()
        } else {
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val string = String(bytes)
            Assert.assertEquals(fileContent, string)
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

    /**
     * Creates text file in the external storage and returns content uri of the file.
     * Additionally, this function will delete the existing file and written content will be verified.
     *
     * @return Content uri of the text file.
     */
    private fun createTextFileInContentProvider(): Uri {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val textFile = File(Environment.getExternalStorageDirectory(), fileName)
        val fileContentValues = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain")
            put(MediaStore.Files.FileColumns.DATA, textFile.absolutePath)
        }
        val filesExternalUri = MediaStore.Files.getContentUri("external")
        val textFileUri = context.contentResolver.insert(filesExternalUri, fileContentValues)
            ?: throw NullPointerException("Content uri cannot be null")
        val outputStream = context.contentResolver.openOutputStream(textFileUri)
            ?: throw NullPointerException("Output stream cannot be null")
        val inputStream = ByteArrayInputStream(fileContent.toByteArray()) as InputStream
        val buffer = ByteArray(8192)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
        verifyTextFileInContentProvider(textFileUri)
        return textFileUri
    }

    /**
     * Creates text file in the external storage.
     */
    private fun createTextFileInExternalStorage() {
        val inputStream = ByteArrayInputStream(fileContent.toByteArray()) as InputStream
        val outputStream = FileOutputStream(textFile)
        val buffer = ByteArray(8192)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
        verifyTextFileContentInExternalStorage()
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
    private fun verifyTextFileInContentProvider(textFileUri: Uri) {
        val inputStream = context.contentResolver.openInputStream(textFileUri)
        val inputStreamReader = InputStreamReader(inputStream)
        val text = inputStreamReader.readText()
        inputStreamReader.close()
        Assert.assertEquals(fileContent, text)
    }

    /**
     * Verify text file content in the external storage.
     */
    private fun verifyTextFileContentInExternalStorage() {
        val inputStream = FileInputStream(textFile)
        val inputStreamReader = InputStreamReader(inputStream)
        val text = inputStreamReader.readText()
        inputStreamReader.close()
        Assert.assertEquals(fileContent, text)
    }

    /**
     * Verify text file content in the cache file.
     */
    private fun verifyCacheFileContent() {
        val inputStream = FileInputStream(cacheFile)
        val inputStreamReader = InputStreamReader(inputStream)
        val text = inputStreamReader.readText()
        inputStreamReader.close()
        Assert.assertEquals(fileContent, text)
    }

}