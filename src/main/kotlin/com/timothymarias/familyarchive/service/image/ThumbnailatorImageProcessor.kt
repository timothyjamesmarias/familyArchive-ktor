package com.timothymarias.familyarchive.service.image

import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.geometry.Positions
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

class ThumbnailatorImageProcessor : ImageProcessor {
    override fun resize(input: InputStream, width: Int, height: Int): ByteArray =
        resize(input, width, height, "jpg")

    override fun resize(input: InputStream, width: Int, height: Int, outputFormat: String): ByteArray {
        val output = ByteArrayOutputStream()
        Thumbnails.of(input)
            .size(width, height)
            .outputFormat(outputFormat)
            .toOutputStream(output)
        return output.toByteArray()
    }

    override fun thumbnail(input: InputStream, size: Int): ByteArray =
        thumbnail(input, size, "jpg")

    override fun thumbnail(input: InputStream, size: Int, outputFormat: String): ByteArray {
        val output = ByteArrayOutputStream()
        Thumbnails.of(input)
            .size(size, size)
            .crop(Positions.CENTER)
            .outputFormat(outputFormat)
            .toOutputStream(output)
        return output.toByteArray()
    }

    override fun scaleToWidth(input: InputStream, width: Int): ByteArray {
        val output = ByteArrayOutputStream()
        Thumbnails.of(input)
            .width(width)
            .outputFormat("jpg")
            .toOutputStream(output)
        return output.toByteArray()
    }

    override fun scaleToHeight(input: InputStream, height: Int): ByteArray {
        val output = ByteArrayOutputStream()
        Thumbnails.of(input)
            .height(height)
            .outputFormat("jpg")
            .toOutputStream(output)
        return output.toByteArray()
    }

    override fun getDimensions(input: InputStream): ImageDimensions {
        val image = ImageIO.read(input)
        return ImageDimensions(image.width, image.height)
    }

    override fun convert(input: InputStream, outputFormat: String): ByteArray {
        val output = ByteArrayOutputStream()
        Thumbnails.of(input)
            .scale(1.0)
            .outputFormat(outputFormat)
            .toOutputStream(output)
        return output.toByteArray()
    }
}
