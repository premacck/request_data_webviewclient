package com.doi.garasi.postinterceptor

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

object Utils {
  @JvmStatic @Throws(IOException::class) fun consumeInputStream(inputStream: InputStream?): ByteArray {
    if (inputStream == null) return ByteArray(0)
    val byteArrayOutputStream = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var count: Int
    while (inputStream.read(buffer).also { count = it } != -1) {
      byteArrayOutputStream.write(buffer, 0, count)
    }
    return byteArrayOutputStream.toByteArray()
  }
}