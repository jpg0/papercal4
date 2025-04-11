package com.trillica

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import biweekly.ICalendar
import org.jetbrains.skia.*


import java.time.LocalDate

@Composable
fun CalendarImage(ical: ICalendar, date: LocalDate = LocalDate.now(), modifier: Modifier = Modifier) {



    //val pixelFont = FontFamily(Font(File("VCR_OSD_MONO_1.001.ttf")))
    //val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier.background(Color.White)) {
        Column {
            CustomText("Header")
            Canvas(modifier = modifier.height(20.dp)) {

            }
            Row {
                Text("Header", color = Color.Black, fontSize = 20.sp, fontFamily = FontFamily.SansSerif)
            }

            CalendarWeek(ical, date)
            CalendarWeek(ical, date.plusWeeks(1))

            Row {
                Text(
                    modifier = modifier.padding(12.dp),
                    text = "Bottom.",
                    fontSize = 20.sp,
                    color = Color.Black,
                )
            }
        }
    }
}

@Composable
fun CalendarWeek (ical: ICalendar, date: LocalDate, modifier: Modifier = Modifier) {
    Row {
        for (day in 0..6) {
            val dayOfWeek = date.plusDays(day.toLong()).dayOfWeek
            val dayOfMonth = date.plusDays(day.toLong()).dayOfMonth
            val events = ical.events
            Column {
                Text(
                    modifier = modifier.padding(12.dp),
                    text = dayOfWeek.toString(),
                    fontSize = 20.sp,
                    color = Color.Black,
                )
                Text(
                    modifier = modifier.padding(12.dp),
                    text = dayOfMonth.toString(),
                    fontSize = 20.sp,
                    color = Color.Black,
                )
            }
        }
    }
}

@Composable
fun CustomText(text: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {


        val skijaCanvas = drawContext.canvas.nativeCanvas //as org.jetbrains.skia.Canvas
        val paint = Paint().apply {
            color = org.jetbrains.skia.Color.BLACK
            mode = org.jetbrains.skia.PaintMode.FILL
            isAntiAlias = false // Disable anti-aliasing
            isDither = false

        }

        val typeface = FontMgr.default.makeFromFile("VCR_OSD_MONO_1.001 2.ttf")
//        val typeface = FontMgr.default.matchFamilyStyle("Menlo", FontStyle.NORMAL)!!

        val font = org.jetbrains.skia.Font(typeface, 21f) .apply {
            this.hinting = FontHinting.NONE
            this.edging = FontEdging.ALIAS
            this.isSubpixel = false
            this.setBitmapsEmbedded(true)
        }


        skijaCanvas.drawString(text, 100f, 100f, font, paint)
    }
}