package me.neon.libs.io

import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest


fun File.forFile(end: String): List<File> {
    return mutableListOf<File>().run {
        if (isDirectory) {
            listFiles()?.forEach {
                addAll(it.forFile(end))
            }
        } else if (exists() && absolutePath.endsWith(end)) {
            add(this@forFile)
        }
        this
    }
}

