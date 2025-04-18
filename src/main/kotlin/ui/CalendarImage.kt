package com.trillica.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
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
fun CalendarImage(icals: Collection<ICalendar>, date: LocalDate = LocalDate.now(), modifier: Modifier = Modifier) {

    Box(modifier = modifier.background(Color.White)) {
        Column {
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                AliasedText(date.month.name.titlecase() + " " + date.year, style = TextStyle(fontSize = 36.sp))
            }

            Calendar2Week(icals, date)

            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                AliasedText(text = "Footer")
            }
        }
    }
}

@Composable
fun Calendar2Week(icals: Collection<ICalendar>, date: LocalDate, modifier: Modifier = Modifier) {

    val start = date.minusDays(date.dayOfWeek.value.toLong() - 1) //start Monday

    Column(verticalArrangement = Arrangement.spacedBy(-2.dp)) {
        DayNameRow(start)
        EventRow(icals, start, date, true)
        EventRow(icals, start.plusWeeks(1), date, false)
    }
}

fun ICalendar.eventsOn(date: LocalDate): List<DayEvent> =
    events.filter { it.summary != null }.mapNotNull { it.occursOn(date) }


fun VEvent.occursOn(date: LocalDate): DayEvent? {
    val iterator = getDateIterator(TimeZone.getDefault())

    val eventDuration: Duration = if (duration == null) {
        Duration.between(dateStart.value.toLocalDateTime(), dateEnd.value.toLocalDateTime())
    } else {
        Duration.ofMillis(duration.value.toMillis())
    }

    while (iterator.hasNext()) {
        val instanceStart = iterator.next().toLocalDateTime()

        val instanceStartDate = instanceStart.toLocalDate()

        if (instanceStartDate <= date) {
            val instanceEnd = instanceStart.plus(eventDuration)

            if (instanceEnd.toLocalDate() >= date) {
                return if (dateStart.value.hasTime()) {
                    WithinDayEvent(this)
                } else {
                    AllDayEvent(this, instanceStartDate, instanceEnd.toLocalDate())
                }
            }
        } else break
    }

    return null
}


fun LocalDate.toDate(): Date = Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())
fun Date.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()

sealed class DayEvent(val event: VEvent) {
    abstract val showIndex: Int
    val summary get() = event.summary.value.trim()
}

class AllDayEvent(event: VEvent, val instanceStart: LocalDate, val instanceEnd: LocalDate) : DayEvent(event) {
    override val showIndex = 10
    fun startsOn(date: LocalDate): Boolean = date == instanceStart
    fun endsOn(date: LocalDate): Boolean = date == instanceEnd
}

class WithinDayEvent(event: VEvent) : DayEvent(event) {
    val start: LocalTime
        get() = LocalDateTime.ofInstant(
            event.dateStart.value.toInstant(),
            ZoneId.systemDefault()
        ).toLocalTime()
    override val showIndex = 20
}

@Composable
fun EventRow(
    icals: Collection<ICalendar>,
    start: LocalDate,
    today: LocalDate,
    firstWeek: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(-2.dp),
        modifier = Modifier.height(IntrinsicSize.Max).fillMaxWidth()
    ) {

        val border = 2.dp

        for (dayIdx in 0..6) {
            val day = start.plusDays(dayIdx.toLong())
            Column(Modifier.fillMaxHeight().weight(1f)) {
                val maybeBorder = if (day.equals(today)) {
                    modifier.border(border, Color.Red, shape = CircleShape)
                } else modifier

                AliasedText(
                    modifier = maybeBorder.padding(2.dp).align(Alignment.CenterHorizontally),
                    text = day.dayOfMonth.toString()
                )

                val eventsOnDay = icals.flatMap { it.eventsOn(day) }

                eventsOnDay.sortedBy { it.showIndex }.forEach { dayEvent ->
                    dayEvent.compose(day,  dayIdx == 0, )
                }
            }
        }
    }
}

@Composable
fun DayEvent.compose(forDay: LocalDate, isFirstWeekday: Boolean) = when (this) {
    is AllDayEvent -> compose(forDay, isFirstWeekday)
    is WithinDayEvent -> compose(forDay)
}

@Composable
fun AllDayEvent.compose(forDay: LocalDate, isFirstWeekday: Boolean) {
    val summary = if(startsOn(forDay) || isFirstWeekday) summary else " "

    Row(
        Modifier.fillMaxWidth().multiBorder(
            drawTop = true,
            drawBottom = true,
            drawLeft = startsOn(forDay),
            drawRight = endsOn(forDay),
            Color.Black, 2f
        )
    ) {
        AliasedText(
            modifier = Modifier.padding(start = 2.dp, top = 2.dp, bottom = 2.dp),
            text = summary
        )
    }
}

@Composable
fun WithinDayEvent.compose(forDay: LocalDate) {
    val ref = this

    Row(Modifier.fillMaxWidth().border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))) {
        AliasedText(
            modifier = Modifier.padding(start = 2.dp, top = 2.dp, bottom = 2.dp),
            text = ref.start.format(DateTimeFormatter.ofPattern("HH:mm"))
        )
        AliasedText(
            modifier = Modifier.weight(2f).padding(2.dp),
            text = ref.summary,
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

fun Modifier.multiBorder(
    drawTop: Boolean,
    drawBottom: Boolean,
    drawLeft: Boolean,
    drawRight: Boolean,
    color: Color,
    width: Float,
) = this.drawWithContent {
    drawContent()
    if (drawBottom) drawLine(
        color = color,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = width,
    )
    if (drawTop) drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = width,
    )
    if (drawRight) drawLine(
        color = color,
        start = Offset(size.width, 0f),
        end = Offset(size.width, size.height),
        strokeWidth = width,
    )
    if (drawLeft) drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(0f, size.height),
        strokeWidth = width,
    )
}