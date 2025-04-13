package com.trillica


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import biweekly.ICalendar
import biweekly.component.VEvent
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


fun String.titlecase() = this[0].titlecase() + substring(1).lowercase()

@Composable
fun CalendarImage(ical: ICalendar, date: LocalDate = LocalDate.now(), modifier: Modifier = Modifier) {

    Box(modifier = modifier.background(Color.White)) {
        Column {
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                AliasedText(date.month.name.titlecase() + " " + date.year, style = TextStyle(fontSize = 36.sp))
            }

            Calendar2Week(ical, date)

            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                AliasedText(text = "Footer")
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
                    if(event.dateStart.value.hasTime()) { //normal event
                        eventWithinDay(
                            LocalDateTime.ofInstant(
                                event.dateStart.value.toInstant(),
                                ZoneId.systemDefault()
                            ).toLocalTime(), event.summary.value)
                    } else { //all day event

                    }
                }
            }
        }
    }
}

@Composable
fun eventWithinDay(start: LocalTime, summary: String) {
    Row(Modifier.fillMaxWidth()) {
           AliasedText(
            modifier = Modifier.padding(start = 2.dp, top = 2.dp, bottom = 2.dp).border(2.dp, Color.Green),
            text = start.format(DateTimeFormatter.ofPattern("HH:mm"))
        )
            AliasedText(
                modifier = Modifier.weight(2f).padding(2.dp).border(2.dp, Color.Green),
                text = summary.trim(),
                overflow = TextOverflow.Clip
            )

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
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    AliasedText(modifier = modifier.padding(2.dp), text = dayOfWeek.name.substring(0, 3).titlecase())
                }
            }
        }
    }
}