package sergionsz.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class CalendarFilter { All, Daily, OneTime }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(tasks: List<Task>, today: String) {
    val (todayYear, todayMonth, _) = parseDate(today)
    var viewYear by remember { mutableStateOf(todayYear) }
    var viewMonth by remember { mutableStateOf(todayMonth) }
    var selectedDate by remember { mutableStateOf(today) }
    var filter by remember { mutableStateOf(CalendarFilter.All) }

    val filteredTasks = remember(tasks, filter) {
        when (filter) {
            CalendarFilter.All -> tasks
            CalendarFilter.Daily -> tasks.filter { !it.isOneTime }
            CalendarFilter.OneTime -> tasks.filter { it.isOneTime }
        }
    }

    val activeDates: Set<String> = remember(filteredTasks) {
        buildSet {
            filteredTasks.forEach { task ->
                addAll(task.completionDates)
                if (task.isOneTime) task.scheduledDate?.let { add(it) }
            }
        }
    }

    val tasksForSelected = remember(filteredTasks, selectedDate) {
        filteredTasks.filter { task ->
            if (task.isOneTime) {
                selectedDate in task.completionDates || task.scheduledDate == selectedDate
            } else {
                true
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(Modifier.height(8.dp))
            MonthCard(
                year = viewYear,
                month = viewMonth,
                today = today,
                selectedDate = selectedDate,
                activeDates = activeDates,
                onSelectDate = { selectedDate = it },
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
            Spacer(Modifier.height(12.dp))
            FilterRow(
                selected = filter,
                onSelect = { filter = it }
            )
        }
        item {
            Spacer(Modifier.height(20.dp))
            Text(
                text = friendlyDateLabel(selectedDate, today),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
        if (tasksForSelected.isEmpty()) {
            item {
                Text(
                    text = "No tasks for this day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        } else {
            items(tasksForSelected, key = { it.id }) { task ->
                CalendarTaskRow(task = task, date = selectedDate)
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    selected: CalendarFilter,
    onSelect: (CalendarFilter) -> Unit
) {
    val options = listOf(
        CalendarFilter.All to "All",
        CalendarFilter.Daily to "Daily",
        CalendarFilter.OneTime to "One-time"
    )
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = selected == value,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) { Text(label) }
        }
    }
}

@Composable
private fun MonthCard(
    year: Int,
    month: Int,
    today: String,
    selectedDate: String,
    activeDates: Set<String>,
    onSelectDate: (String) -> Unit,
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
            WeekdayHeaderCal()
            Spacer(Modifier.height(4.dp))
            SelectableMonthGrid(
                year = year,
                month = month,
                today = today,
                selectedDate = selectedDate,
                activeDates = activeDates,
                onSelectDate = onSelectDate
            )
        }
    }
}

@Composable
private fun WeekdayHeaderCal() {
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
private fun SelectableMonthGrid(
    year: Int,
    month: Int,
    today: String,
    selectedDate: String,
    activeDates: Set<String>,
    onSelectDate: (String) -> Unit
) {
    val daysInMonth = daysInMonth(year, month)
    val firstWeekday = firstWeekdayMondayBased(year, month)
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
                            SelectableDayCell(
                                day = dayNumber,
                                isToday = dateKey == today,
                                isSelected = dateKey == selectedDate,
                                hasActivity = dateKey in activeDates,
                                onClick = { onSelectDate(dateKey) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableDayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    hasActivity: Boolean,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val bg = if (isSelected) primary else Color.Transparent
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderMod = if (isToday && !isSelected) {
        Modifier.border(1.dp, primary, CircleShape)
    } else Modifier
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .then(borderMod)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (hasActivity) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else primary)
                )
            }
        }
    }
}

@Composable
private fun CalendarTaskRow(task: Task, date: String) {
    val isCompleted = task.isCompletedOn(date)
    val alpha = if (isCompleted) 0.4f else 1f
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        color = if (isCompleted) {
            MaterialTheme.colorScheme.surfaceContainerLow
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskLeadingIconCal(task = task, alpha = alpha)
            Text(
                text = task.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                textDecoration = if (isCompleted) TextDecoration.LineThrough else null
            )
            if (!task.isOneTime) {
                Spacer(Modifier.size(6.dp))
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repeats daily",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha * 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.size(8.dp))
            }
            Icon(
                imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = if (isCompleted) "Done" else "Not done",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = if (isCompleted) 0.55f else 1f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun TaskLeadingIconCal(task: Task, alpha: Float) {
    val hasEmoji = !task.emoji.isNullOrEmpty()
    val vector = if (!hasEmoji && !task.iconName.isNullOrEmpty()) {
        IconCatalog.byName(task.iconName)
    } else null
    Box(
        modifier = Modifier
            .padding(end = 12.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (hasEmoji) {
            Text(text = task.emoji!!, style = MaterialTheme.typography.titleMedium)
        } else if (vector != null) {
            Icon(
                imageVector = vector,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun friendlyDateLabel(date: String, today: String): String {
    val (ty, tm, td) = parseDate(today)
    val (y, m, d) = parseDate(date)
    val cal = Calendar.getInstance()
    cal.clear()
    cal.set(ty, tm - 1, td)
    val todayMillis = cal.timeInMillis
    cal.set(y, m - 1, d)
    val pickedMillis = cal.timeInMillis
    val diffDays = ((pickedMillis - todayMillis) / (24L * 60L * 60L * 1000L)).toInt()
    return when (diffDays) {
        0 -> "Today"
        -1 -> "Yesterday"
        1 -> "Tomorrow"
        else -> SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(cal.time)
    }
}
