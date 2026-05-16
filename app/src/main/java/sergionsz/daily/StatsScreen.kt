package sergionsz.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private const val ALL_TASKS_ID: Long = -1L

@Composable
fun StatsScreen(tasks: List<Task>, today: String) {
    val recurringTasks = remember(tasks) { tasks.filter { !it.isOneTime } }
    var selectedTaskId by remember { mutableStateOf(ALL_TASKS_ID) }
    val (year, month, _) = parseDate(today)
    var viewYear by remember { mutableStateOf(year) }
    var viewMonth by remember { mutableStateOf(month) }

    val datesForSelection: Set<String> = remember(recurringTasks, selectedTaskId) {
        if (selectedTaskId == ALL_TASKS_ID) {
            recurringTasks.flatMap { it.completionDates }.toSet()
        } else {
            recurringTasks.firstOrNull { it.id == selectedTaskId }?.completionDates ?: emptySet()
        }
    }

    val datesPerDayCount: Map<String, Int> = remember(recurringTasks, selectedTaskId) {
        if (selectedTaskId == ALL_TASKS_ID) {
            recurringTasks.flatMap { it.completionDates }
                .groupingBy { it }
                .eachCount()
        } else {
            datesForSelection.associateWith { 1 }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TaskFilterRow(
                tasks = recurringTasks,
                selectedTaskId = selectedTaskId,
                onSelect = { selectedTaskId = it }
            )
        }
        item {
            Spacer(Modifier.height(8.dp))
            CalendarCard(
                year = viewYear,
                month = viewMonth,
                today = today,
                datesPerDayCount = datesPerDayCount,
                onPrev = {
                    if (viewMonth == 1) {
                        viewMonth = 12; viewYear -= 1
                    } else viewMonth -= 1
                },
                onNext = {
                    if (viewMonth == 12) {
                        viewMonth = 1; viewYear += 1
                    } else viewMonth += 1
                }
            )
        }
        item {
            Spacer(Modifier.height(16.dp))
            StatsCard(
                dates = datesForSelection,
                today = today
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TaskFilterRow(
    tasks: List<Task>,
    selectedTaskId: Long,
    onSelect: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Filter",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        FlowChips(
            items = listOf(ALL_TASKS_ID to "All tasks") + tasks.map { it.id to it.name },
            selectedId = selectedTaskId,
            onSelect = onSelect
        )
    }
}

@Composable
private fun FlowChips(
    items: List<Pair<Long, String>>,
    selectedId: Long,
    onSelect: (Long) -> Unit
) {
    Column {
        items.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { (id, label) ->
                    Box(modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)) {
                        AssistChip(
                            onClick = { onSelect(id) },
                            label = { Text(label, maxLines = 1) },
                            colors = if (id == selectedId) {
                                AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                AssistChipDefaults.assistChipColors()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarCard(
    year: Int,
    month: Int,
    today: String,
    datesPerDayCount: Map<String, Int>,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrev) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                }
                Text(
                    text = monthLabel(year, month),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                }
            }
            Spacer(Modifier.height(8.dp))
            WeekdayHeader()
            Spacer(Modifier.height(4.dp))
            MonthGrid(
                year = year,
                month = month,
                today = today,
                datesPerDayCount = datesPerDayCount
            )
        }
    }
}

@Composable
private fun WeekdayHeader() {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEach { label ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(
    year: Int,
    month: Int,
    today: String,
    datesPerDayCount: Map<String, Int>
) {
    val daysInMonth = daysInMonth(year, month)
    val firstWeekday = firstWeekdayMondayBased(year, month)
    val maxCount = datesPerDayCount.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val totalCells = firstWeekday + daysInMonth
    val rows = (totalCells + 6) / 7

    Column {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - firstWeekday + 1
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNumber in 1..daysInMonth) {
                            val dateKey = formatDate(year, month, dayNumber)
                            val count = datesPerDayCount[dateKey] ?: 0
                            DayCell(
                                day = dayNumber,
                                isToday = dateKey == today,
                                completionCount = count,
                                maxCount = maxCount
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    completionCount: Int,
    maxCount: Int
) {
    val primary = MaterialTheme.colorScheme.primary
    val intensity = if (completionCount == 0) 0f else {
        0.3f + 0.7f * (completionCount.toFloat() / maxCount.toFloat())
    }
    val bg = if (completionCount > 0) primary.copy(alpha = intensity) else Color.Transparent
    val textColor = when {
        completionCount > 0 -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun StatsCard(dates: Set<String>, today: String) {
    val sortedDates = remember(dates) { dates.sorted() }
    val totalCompletions = sortedDates.size
    val currentStreak = remember(sortedDates, today) { currentStreak(sortedDates.toSet(), today) }
    val longestStreak = remember(sortedDates) { longestStreak(sortedDates) }
    val last30 = remember(sortedDates, today) { completionsInLastNDays(sortedDates.toSet(), today, 30) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatTile(label = "Total", value = totalCompletions.toString(), modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                StatTile(label = "Current streak", value = "$currentStreak d", modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatTile(label = "Longest streak", value = "$longestStreak d", modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                StatTile(label = "Last 30 days", value = "$last30 / 30", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun parseDate(date: String): Triple<Int, Int, Int> {
    val parts = date.split("-")
    return Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
}

private fun formatDate(year: Int, month: Int, day: Int): String =
    "%04d-%02d-%02d".format(year, month, day)

private fun daysInMonth(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.clear()
    cal.set(year, month - 1, 1)
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun firstWeekdayMondayBased(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.clear()
    cal.set(year, month - 1, 1)
    val sundayBased = cal.get(Calendar.DAY_OF_WEEK)
    return ((sundayBased + 5) % 7)
}

private fun monthLabel(year: Int, month: Int): String {
    val names = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return "${names[month - 1]} $year"
}

internal fun currentStreak(dates: Set<String>, today: String): Int {
    if (dates.isEmpty()) return 0
    val cal = Calendar.getInstance()
    val (y, m, d) = parseDate(today)
    cal.clear()
    cal.set(y, m - 1, d)
    var streak = 0
    if (today !in dates) {
        cal.add(Calendar.DAY_OF_MONTH, -1)
    }
    while (true) {
        val key = formatDate(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        if (key in dates) {
            streak += 1
            cal.add(Calendar.DAY_OF_MONTH, -1)
        } else break
    }
    return streak
}

internal fun longestStreak(sortedDates: List<String>): Int {
    if (sortedDates.isEmpty()) return 0
    var longest = 1
    var current = 1
    val cal = Calendar.getInstance()
    for (i in 1 until sortedDates.size) {
        val (py, pm, pd) = parseDate(sortedDates[i - 1])
        cal.clear()
        cal.set(py, pm - 1, pd)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val expected = formatDate(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        current = if (sortedDates[i] == expected) current + 1 else 1
        if (current > longest) longest = current
    }
    return longest
}

internal fun completionsInLastNDays(dates: Set<String>, today: String, n: Int): Int {
    val cal = Calendar.getInstance()
    val (y, m, d) = parseDate(today)
    cal.clear()
    cal.set(y, m - 1, d)
    var count = 0
    repeat(n) {
        val key = formatDate(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        if (key in dates) count += 1
        cal.add(Calendar.DAY_OF_MONTH, -1)
    }
    return count
}
