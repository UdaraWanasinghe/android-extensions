package com.aureusapps.android.extensions.test.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File

object TestHelpers {

    @JvmStatic
    fun generateFiles(root: File, nodes: List<Node>) {
        root.mkdir()
        for (node in nodes) {
            val file = File(root, node.name)
            if (node is DirectoryNode) {
                generateFiles(file, node.children)
            } else {
                file.createNewFile()
            }
        }
    }

    @JvmStatic
    fun getAndroidResourceUri(context: Context, resId: Int): Uri {
        val scheme = ContentResolver.SCHEME_ANDROID_RESOURCE
        val packageName = context.resources.getResourcePackageName(resId)
        val typeName = context.resources.getResourceTypeName(resId)
        val entryName = context.resources.getResourceEntryName(resId)
        // Accepted Uris
        // android.resource://{packageName}/{resId}
        // android.resource://{packageName}/{typeName}/{resId}
        // android.resource://{packageName}/{typeName}/{entryName}
        return Uri.parse("$scheme://$packageName/$typeName/$entryName")
    }

    abstract class Node {
        abstract val name: String
    }

    data class FileNode(
        override val name: String
    ) : Node()

    data class DirectoryNode(
        override val name: String,
        val children: List<Node>
    ) : Node()

}