package com.trillica


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import biweekly.ICalendar
import biweekly.component.VEvent
import org.jetbrains.skia.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


fun String.titlecase() = this[0].titlecase() + substring(1).lowercase()

@Composable
fun CalendarImage(ical: ICalendar, date: LocalDate = LocalDate.now(), modifier: Modifier = Modifier) {

    Box(modifier = modifier.background(Color.White)) {
        Column {
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                AliasedText(date.month.name.titlecase() + " " + date.year)
            }

            Calendar2Week(ical, date)
            Row {
                AliasedText(
                    text = "Footer",
                )
            }

        }
    }
}

@Composable
fun Calendar2Week(ical: ICalendar, date: LocalDate, modifier: Modifier = Modifier) {

    val start = date.minusDays(date.dayOfWeek.value.toLong() - 1) //start Monday

    Column(verticalArrangement = Arrangement.spacedBy(-2.dp)) {
        DayNameRow(start)
        EventRow(ical, start, date)
        EventRow(ical, start.plusWeeks(1), date)
    }
}

fun ICalendar.eventsOn(date: LocalDate): List<VEvent> =
    events.filter { it.occursOn(date) }


fun VEvent.occursOn(date: LocalDate): Boolean {
    val iterator = getDateIterator(TimeZone.getDefault())
    iterator.advanceTo(date.toDate())

    if (iterator.hasNext()) {
        return date.equals(iterator.next().toLocalDate())
    } else return false
}


fun LocalDate.toDate(): Date = Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())
fun Date.toLocalDate(): LocalDate = Instant.ofEpochMilli(getTime()).atZone(ZoneId.systemDefault()).toLocalDate()


@Composable
fun EventRow(ical: ICalendar, start: LocalDate, today: LocalDate, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(-2.dp),
        modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max).fillMaxWidth()
    ) {

        val border = 2.dp

        for (dayIdx in 0..6) {
            val day = start.plusDays(dayIdx.toLong())
            Column(
                modifier
                    .border(2.dp, Color.Black)
//                    .width((1304.dp / 7))
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                val maybeBorder = if(day.equals(today)) {
                    modifier.border(border, Color.Red, shape = CircleShape)
                } else modifier

                AliasedText(
                    modifier = maybeBorder.padding(2.dp),
                    text = day.dayOfMonth.toString()
                )

                for (event in ical.eventsOn(day)) {
                    AliasedText(
                        modifier = modifier.padding(2.dp),
                        text = event.summary.value.trim(),
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}

@Composable
fun DayNameRow(start: LocalDate, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.spacedBy(-2.dp), modifier = Modifier.fillMaxWidth()) {
        for (day in 0..6) {
            val dayOfWeek = start.plusDays(day.toLong()).dayOfWeek
            Column(
                modifier
                    .border(2.dp, Color.Black)
                    .weight(1f)
            ) {
                Row {
                    AliasedText(modifier = modifier.padding(2.dp), text = dayOfWeek.name.substring(0, 3).titlecase())
                }
            }
        }
    }
}


val cfont by lazy {
    val typeface = FontMgr.default.matchFamilyStyle("Arial", FontStyle.NORMAL)
    Font(typeface, 21f).apply {
        this.hinting = FontHinting.NONE
        this.edging = FontEdging.ALIAS
        this.isSubpixel = false
        this.setBitmapsEmbedded(true)
    }
}

@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class)
@Composable
fun CText1(text: String, ringText: Boolean = false, maxWidth: Dp = Int.MAX_VALUE.dp, modifier: Modifier = Modifier) {

    val textMeasurer = rememberTextMeasurer()

    val layoutResult = textMeasurer.measure(
        text,
        style = TextStyle(
            fontFamily = FontFamily("Arial"),
            fontSize = TextUnit(21f, TextUnitType.Sp)
        )
    )

    val border = if (ringText) 2 else 0

    val width = min(
        layoutResult.size.width.dp, maxWidth
    ) + 2 * border.dp
    val height = layoutResult.size.height.dp + 2 * border.dp

    val canvasModifier = modifier
        .size(DpSize(width, height)).let {
            if (ringText) {
                it.border(border.dp, Color.Red, shape = CircleShape)
            } else it
        }.padding(1.dp)


    Canvas(modifier = canvasModifier) {

        val skijaCanvas = drawContext.canvas.nativeCanvas //as org.jetbrains.skia.Canvas
        val paint = Paint().apply {
            color = org.jetbrains.skia.Color.BLACK
            mode = PaintMode.FILL
            isAntiAlias = false // Disable anti-aliasing
            isDither = false
        }

        skijaCanvas.clipRect(Rect(0f, 0f, width.toPx(), height.toPx()), antiAlias = false, mode = ClipMode.INTERSECT)
        //skijaCanvas.clipRect(Rect(0f, 0f, 10f, 10f), antiAlias = false, mode = ClipMode.INTERSECT)
        skijaCanvas.drawString(
            text,
            border.toFloat(),
            layoutResult.size.height.toFloat() - 2 * border - 4,
            cfont,
            paint
        )
    }
}