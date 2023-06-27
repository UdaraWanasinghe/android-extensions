package com.aureusapps.android.extensions.test.utils

import java.io.File

object FileGenerator {

    @JvmStatic
    fun generate(root: File, nodes: List<Node>) {
        root.mkdir()
        for (node in nodes) {
            val file = File(root, node.name)
            if (node is DirectoryNode) {
                generate(file, node.children)
            } else {
                file.createNewFile()
            }
        }
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