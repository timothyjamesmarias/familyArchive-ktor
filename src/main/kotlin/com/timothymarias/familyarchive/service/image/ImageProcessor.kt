package com.timothymarias.familyarchive.service.image

import java.io.InputStream

data class ImageDimensions(val width: Int, val height: Int)

interface ImageProcessor {
    fun resize(input: InputStream, width: Int, height: Int): ByteArray
    fun resize(input: InputStream, width: Int, height: Int, outputFormat: String): ByteArray
    fun thumbnail(input: InputStream, size: Int): ByteArray
    fun thumbnail(input: InputStream, size: Int, outputFormat: String): ByteArray
    fun scaleToWidth(input: InputStream, width: Int): ByteArray
    fun scaleToHeight(input: InputStream, height: Int): ByteArray
    fun getDimensions(input: InputStream): ImageDimensions
    fun convert(input: InputStream, outputFormat: String): ByteArray
}
