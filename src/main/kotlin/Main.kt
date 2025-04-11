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
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.net.URL

fun main() {

    renderScreenshot(1304, 984)
}

//    val coroutineScope = rememberCoroutineScope()
//    val graphicsLayer = rememberGraphicsLayer()
//    Box(
//        modifier = Modifier
//            .drawWithContent {
//                // call record to capture the content in the graphics layer
//                graphicsLayer.record {
//                    // draw the contents of the composable into the graphics layer
//                    this@drawWithContent.drawContent()
//                }
//                // draw the graphics layer on the visible canvas
//                drawLayer(graphicsLayer)
//            }
//            .clickable {
//                coroutineScope.launch {
//                    val bitmap = graphicsLayer.toImageBitmap()
//                    // do something with the newly acquired bitmap
//                }
//            }
//            .background(Color.White)
//    ) {
//        Text("Hello Android", fontSize = 26.sp)
//    }

//    val scene =
//        ImageComposeScene(
//            width = 1304,
//            height = 984,
//        ) {
//            CalendarImage(ical, modifier = Modifier.fillMaxSize())
//        }
//
//    val image =
//        scene.use {
//            it
//                .render(nanoTime = 0)
//                .encodeToData(format = EncodedImageFormat.PNG) ?: error("encoding failed")
//        }
//
//    FileOutputStream("test.png").use {
//        it.write(image.bytes)
//    }
//}



@OptIn(ExperimentalTestApi::class)
fun renderScreenshot(width: Int, height: Int) = runDesktopComposeUiTest(width, height) {

    //val ical = FileInputStream("/Users/jpg/calendar1.ics").use { input ->
    val ical = URL("http://192.168.11.190/calendar1.ics").readBytes().let {
        val input = it.inputStream()
        Biweekly.parse(input).first()
    }

    setContent {
        CalendarImage(ical, modifier = Modifier.fillMaxSize())
    }
    val img: ImageBitmap = captureToImage()
    val file = File("test-screenshot.png")
    val encodedImage = img.encodeToPngBytes() ?: error("Could not encode image as png")
    file.writeBytes(encodedImage)
}

fun ImageBitmap.encodeToPngBytes(): ByteArray? {
    return Image.makeFromBitmap(this.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes
}