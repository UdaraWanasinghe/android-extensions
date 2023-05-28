package com.aureusapps.android.extensions.test

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aureusapps.android.extensions.saveTo
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
        // delete cache file
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }

    @Test
    fun test_saveContentUriToPath() {
        runBlocking {
            val textFileUri = createTextFileInContentProvider()
            textFileUri.saveTo(context, cacheFile.absolutePath)
            verifyCacheFileContent()
        }
    }

    @Test
    fun test_saveFileUriToPath() {
        runBlocking {
            createTextFileInExternalStorage()
            val fileUri = Uri.fromFile(textFile)
            fileUri.saveTo(context, cacheFile.absolutePath)
            verifyCacheFileContent()
        }
    }

    @Test
    fun test_saveAndroidResourceUriToPath() {
        runBlocking {
            val scheme = ContentResolver.SCHEME_ANDROID_RESOURCE
            val resId = R.raw.sample_text
            val packageName = context.resources.getResourcePackageName(resId)
            val typeName = context.resources.getResourceTypeName(resId)
            val entryName = context.resources.getResourceEntryName(resId)
            val resourceUri = Uri.parse("$scheme://$packageName/$typeName/$entryName")
            resourceUri.saveTo(context, cacheFile.absolutePath)
            verifyCacheFileContent()
        }
    }

    @Test
    fun test_saveHttpUriToPath() {
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
            httpUri.saveTo(context, cacheFile.absolutePath)
            verifyCacheFileContent()
            server.shutdown()
        }
    }

    @Test
    fun test_getFileName() {

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
     *
     * @return File instance of the text file.
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