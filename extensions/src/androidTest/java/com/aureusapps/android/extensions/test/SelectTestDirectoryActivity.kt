package com.aureusapps.android.extensions.test

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

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