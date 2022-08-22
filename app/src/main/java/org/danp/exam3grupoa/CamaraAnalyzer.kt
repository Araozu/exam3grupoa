package org.danp.exam3grupoa

import android.annotation.SuppressLint
import android.graphics.*
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

// ------------------------------------------------------------
// Convierte una imagen de formato YUV a RGB
// ------------------------------------------------------------
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

// ------------------------------------------------------------
//
// Extrae los pixeles rojos, los cuenta, y almacena la cantidad
// en un array de tamaño 256.
// Ejm. Si hay 500 pixeles con rojo=20, la posicion 20 del array = 500
//
// ------------------------------------------------------------
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

// ------------------------------------------------------------
//
// Analisis de imagen
//
// ------------------------------------------------------------
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