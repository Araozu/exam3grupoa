package org.danp.exam3grupoa

import android.annotation.SuppressLint
import android.graphics.*
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

// Convierte formato yuv a rgb
fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer // Y
    val vuBuffer = planes[2].buffer // VU

    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()

    val nv21 = ByteArray(ySize + vuSize)

    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

// Extrae los pixeles rojos del bitmap, los cuenta y almacena en un array.
// La posicion 0 del array tiene el numero de pixeles con rojo=0,
// la posicion 1 el nro. de pixeles con rojo=1, etc.
fun bitmapToRedArray(bitmap: Bitmap): Array<Int> {
    val result = Array(256) { 0 }

    // Iterar en todos los pixeles de la img
    for (x in 0 until bitmap.width) {
        for (y in 0 until bitmap.height) {
            val pixel = bitmap.getPixel(x, y)

            // Extraer rojo del pixel usando operaciones binarias
            val redChannel = pixel and 0x00FF0000
            val value = redChannel shr 16

            // Log.d("DEBUG", pixel.toString(2) + " -> ${redChannel.toString(2)} -> ${value.toString(2)} = " + value)

            // Incrementar el contador
            result[value] += 1
        }
    }

    return result
}

class CamaraAnalyzer(val listener: (Array<Int>) -> Unit) : ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.format == ImageFormat.YUV_420_888) {
            val image = imageProxy.image ?: return
            val bitmap = image.toBitmap()
            val count = bitmapToRedArray(bitmap)
            listener(count)
        }

        imageProxy.close()
    }
}