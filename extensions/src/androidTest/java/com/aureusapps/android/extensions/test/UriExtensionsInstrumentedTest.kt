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
import com.aureusapps.android.extensions.createFile
import com.aureusapps.android.extensions.fileExists
import com.aureusapps.android.extensions.fileName
import com.aureusapps.android.extensions.generateMD5
import com.aureusapps.android.extensions.generateSHA1
import com.aureusapps.android.extensions.listFiles
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import kotlinx.coroutines.runBlocking
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
    fun test_getFileUriFileName() {
        createTextFileInExternalStorage()
        val resultFileName = Uri.fromFile(textFile).fileName(context)
        Assert.assertEquals(fileName, resultFileName)
    }

    @Test
    fun test_getAndroidUriFileName() {
        val resourceUri = getAndroidResourceUri()
        val resultFileName = resourceUri.fileName(context)
        Assert.assertEquals("sample_text", resultFileName)
    }

    @Test
    fun test_getHttpUriFileName() {
        val httpUri1 = Uri.parse("http://localhost:4648/sample_text.txt")
        val resultFileName1 = httpUri1.fileName(context)
        Assert.assertEquals(fileName, resultFileName1)
        val httpUri2 = Uri.parse("http://localhost:4648/sample_text.txt?param1=s&param2=q")
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
    fun test_fileExists() {
        createTextFileInExternalStorage()
        val result = textFile
            .parentFile
            ?.toUri()
            ?.fileExists(context, fileName) ?: -1
        Assert.assertEquals(1, result)
    }

    @Test
    fun test_copyContentUri() {
        runBlocking {
            val textFileUri = createTextFileInContentProvider()
            textFileUri.copyTo(context, cacheFile.toUri())
            verifyCacheFileContent()
        }
    }

    @Test
    fun test_copyFileUri() {
        runBlocking {
            createTextFileInExternalStorage()
            val fileUri = Uri.fromFile(textFile)
            fileUri.copyTo(context, cacheFile.toUri())
            verifyCacheFileContent()
        }
    }

    @Test
    fun test_copyAndroidResourceUri() {
        runBlocking {
            val resourceUri = getAndroidResourceUri()
            resourceUri.copyTo(context, cacheFile.toUri())
            verifyCacheFileContent()
        }
    }

    @Test
    fun test_copyHttpUri() {
        runBlocking {
            val server = MockWebServer()
            server.start()
            val buffer = Buffer()
            buffer.write(fileContent.toByteArray())
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(buffer)
            )
            val contentUrl = server.url("/$fileName").toString()
            val httpUri = Uri.parse(contentUrl)
            httpUri.copyTo(context, cacheFile.toUri())
            verifyCacheFileContent()
            server.shutdown()
        }
    }

    @Test
    fun test_generateHash() {
        val resUri = getAndroidResourceUri()
        val expectedSHA1 = "e666e67f66f4038e0c1d2f7c3a2abeaf27b3123f"
        val expectedMD5 = "6029f28561014cd2fccef51253be6dbb"
        val actualSHA1 = resUri.generateSHA1(context)
        val actualMD5 = resUri.generateMD5(context)
        Assert.assertEquals(expectedMD5, actualMD5)
        Assert.assertEquals(expectedSHA1, actualSHA1)
    }

    private fun getAndroidResourceUri(): Uri {
        val scheme = ContentResolver.SCHEME_ANDROID_RESOURCE
        val resId = R.raw.sample_text
        val packageName = context.resources.getResourcePackageName(resId)
        val typeName = context.resources.getResourceTypeName(resId)
        val entryName = context.resources.getResourceEntryName(resId)
        return Uri.parse("$scheme://$packageName/$typeName/$entryName")
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