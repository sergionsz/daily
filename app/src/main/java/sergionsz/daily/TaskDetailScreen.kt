package sergionsz.daily

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    initial: Task?,
    today: String,
    onClose: () -> Unit,
    onSave: (Task) -> Unit,
    onDelete: () -> Unit
) {
    val key = initial?.id
    var name by rememberSaveable(key) { mutableStateOf(initial?.name ?: "") }
    var emoji by rememberSaveable(key) { mutableStateOf(initial?.emoji ?: "") }
    var iconName by rememberSaveable(key) { mutableStateOf(initial?.iconName) }
    var isOneTime by rememberSaveable(key) { mutableStateOf(initial?.isOneTime ?: false) }
    var scheduledDate by rememberSaveable(key) {
        mutableStateOf(initial?.scheduledDate ?: if (initial?.isOneTime == true) today else null)
    }
    var pickerOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var datePickerOpen by remember { mutableStateOf(false) }

    fun commitAndClose() {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            val cleanedEmoji = emoji.trim().ifEmpty { null }
            val effectiveScheduled = if (isOneTime) (scheduledDate ?: today) else null
            val task = if (initial == null) {
                Task(
                    id = System.currentTimeMillis(),
                    name = trimmed,
                    emoji = cleanedEmoji,
                    iconName = iconName,
                    completionDates = emptySet(),
                    isOneTime = isOneTime,
                    scheduledDate = effectiveScheduled
                )
            } else {
                initial.copy(
                    name = trimmed,
                    emoji = cleanedEmoji,
                    iconName = iconName,
                    isOneTime = isOneTime,
                    scheduledDate = effectiveScheduled
                )
            }
            onSave(task)
        }
        onClose()
    }

    BackHandler { commitAndClose() }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text(if (initial == null) "New task" else "Task") },
                navigationIcon = {
                    IconButton(onClick = { commitAndClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                EditableTaskIcon(
                    emoji = emoji,
                    iconName = iconName,
                    onClick = { pickerOpen = true }
                )
                Spacer(Modifier.size(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Task name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !isOneTime,
                    onClick = { isOneTime = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Daily") }
                SegmentedButton(
                    selected = isOneTime,
                    onClick = {
                        isOneTime = true
                        if (scheduledDate == null) scheduledDate = today
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("One-time") }
            }

            if (isOneTime) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Scheduled for ${scheduledDate ?: today}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { datePickerOpen = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Move to another date")
                }
            }

            if (initial != null) {
                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Delete")
                }
            }
        }
    }

    if (pickerOpen) {
        IconPickerDialog(
            currentEmoji = emoji,
            currentIconName = iconName,
            onDismiss = { pickerOpen = false },
            onSelectEmoji = {
                emoji = it
                iconName = null
                pickerOpen = false
            },
            onSelectIcon = {
                iconName = it
                emoji = ""
                pickerOpen = false
            },
            onClear = {
                emoji = ""
                iconName = null
                pickerOpen = false
            }
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete task?") },
            text = {
                val displayName = name.trim().ifEmpty { initial?.name ?: "" }
                Text("\"$displayName\" will be removed.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }

    if (datePickerOpen) {
        val initialMillis = remember(scheduledDate, today) {
            dateStringToUtcMillis(scheduledDate ?: today)
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { datePickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        scheduledDate = utcMillisToDateString(millis)
                    }
                    datePickerOpen = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { datePickerOpen = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun EditableTaskIcon(
    emoji: String,
    iconName: String?,
    onClick: () -> Unit
) {
    val vector = if (emoji.isEmpty() && !iconName.isNullOrEmpty()) {
        IconCatalog.byName(iconName)
    } else null
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            emoji.isNotEmpty() -> Text(text = emoji, fontSize = 28.sp)
            vector != null -> Icon(
                imageVector = vector,
                contentDescription = "Change icon",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
            else -> Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = "Choose icon",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

private fun dateStringToUtcMillis(date: String): Long {
    val parts = date.split("-")
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.clear()
    cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
    return cal.timeInMillis
}

private fun utcMillisToDateString(millis: Long): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = millis
    return "%04d-%02d-%02d".format(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
    )
}
