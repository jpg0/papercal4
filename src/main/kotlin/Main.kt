package com.trillica

//import androidx.compose.ui.ImageComposeScene
//import androidx.compose.ui.use
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import biweekly.Biweekly
import com.trillica.ui.CalendarImage
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File
import java.net.URL

fun main() {

    renderScreenshot(1304, 984)
}

@OptIn(ExperimentalTestApi::class)
fun renderScreenshot(width: Int, height: Int) = runDesktopComposeUiTest(width, height) {

    val icals = Config.icalUrl_Test. map {
        URL(it).readBytes().let {
            val input = it.inputStream()
            Biweekly.parse(input).first()
        }
    }

    setContent {
        CalendarImage(icals, modifier = Modifier.fillMaxSize())
    }
    val img: ImageBitmap = captureToImage()
    val file = File("build/output.png")
    val encodedImage = img.encodeToPngBytes() ?: error("Could not encode image as png")
    file.writeBytes(encodedImage)
}

fun ImageBitmap.encodeToPngBytes(): ByteArray? {
    return Image.makeFromBitmap(this.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes
}