package sergionsz.daily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sergionsz.daily.ui.theme.DailyTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SuggestedEmojis = listOf(
    "⭐", "🎯", "✅", "💡", "❤️", "🔥", "✨", "🎉",
    "🏃", "💪", "🧘", "📚", "💧", "💤", "☕", "🌱"
)

private val SuggestedIconNames = listOf(
    "Star", "Favorite", "EmojiEvents", "Bolt",
    "FitnessCenter", "DirectionsRun", "SelfImprovement", "MenuBook",
    "LocalDrink", "Bedtime", "Computer", "MusicNote",
    "Brush", "Spa", "AutoAwesome", "Flag"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = TaskRepository(applicationContext)
        setContent {
            DailyTheme {
                DailyApp(repo)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyApp(repo: TaskRepository) {
    var tasks by remember { mutableStateOf(repo.load()) }
    var dialogOpen by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var detailTaskId by remember { mutableStateOf<Long?>(null) }
    var reorderMode by remember { mutableStateOf(false) }
    var today by remember { mutableStateOf(TaskRepository.todayKey()) }

    LaunchedEffect(Unit) {
        val startupToday = TaskRepository.todayKey()
        if (startupToday != today) today = startupToday
        val pruned = tasks.filter { it.shouldBePersisted(startupToday) }
        if (pruned.size != tasks.size) {
            tasks = pruned
            repo.save(pruned)
        }
        while (true) {
            delay(60_000L)
            val current = TaskRepository.todayKey()
            if (current != today) {
                today = current
                val rolled = tasks.filter { it.shouldBePersisted(current) }
                if (rolled.size != tasks.size) {
                    tasks = rolled
                    repo.save(rolled)
                }
            }
        }
    }

    val detailTask = detailTaskId?.let { id -> tasks.firstOrNull { it.id == id } }
    if (detailTask != null) {
        TaskDetailScreen(
            task = detailTask,
            today = today,
            onBack = { detailTaskId = null },
            onEdit = {
                editingTask = detailTask
                dialogOpen = true
            },
            onDelete = {
                tasks = tasks.filter { it.id != detailTask.id }
                repo.save(tasks)
                detailTaskId = null
            },
            onReschedule = { newDate ->
                tasks = tasks.map {
                    if (it.id == detailTask.id) it.copy(scheduledDate = newDate) else it
                }
                repo.save(tasks)
                detailTaskId = null
            }
        )
    } else {
        val pagerState = rememberPagerState(pageCount = { 2 })
        val scope = rememberCoroutineScope()
        Scaffold(
            topBar = {
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("Daily") }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text("Stats") }
                    )
                }
            }
        ) { padding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().padding(padding)
            ) { page ->
                when (page) {
                    0 -> DailyTab(
                        tasks = tasks,
                        today = today,
                        reorderMode = reorderMode,
                        onTasksChange = { newTasks ->
                            tasks = newTasks
                            repo.save(newTasks)
                        },
                        onTasksReorder = { newTasks -> tasks = newTasks },
                        onTasksReorderEnd = { repo.save(tasks) },
                        onToggleReorderMode = { reorderMode = !reorderMode },
                        onTaskClick = { task -> detailTaskId = task.id },
                        onNewTask = {
                            editingTask = null
                            dialogOpen = true
                        }
                    )
                    1 -> StatsScreen(tasks = tasks, today = today)
                }
            }
        }
    }

    if (dialogOpen) {
        TaskDialog(
            initial = editingTask,
            onDismiss = {
                dialogOpen = false
                editingTask = null
            },
            onConfirm = { name, emoji, iconName, isOneTime ->
                val trimmedName = name.trim()
                val trimmedEmoji = emoji.trim().ifEmpty { null }
                if (trimmedName.isNotEmpty()) {
                    val current = editingTask
                    val updated = if (current == null) {
                        tasks + Task(
                            id = System.currentTimeMillis(),
                            name = trimmedName,
                            emoji = trimmedEmoji,
                            iconName = iconName,
                            completionDates = emptySet(),
                            isOneTime = isOneTime,
                            scheduledDate = if (isOneTime) today else null
                        )
                    } else {
                        tasks.map {
                            if (it.id == current.id) {
                                val newScheduled = when {
                                    !isOneTime -> null
                                    it.isOneTime -> it.scheduledDate ?: today
                                    else -> today
                                }
                                it.copy(
                                    name = trimmedName,
                                    emoji = trimmedEmoji,
                                    iconName = iconName,
                                    isOneTime = isOneTime,
                                    scheduledDate = newScheduled
                                )
                            } else it
                        }
                    }
                    tasks = updated
                    repo.save(updated)
                }
                dialogOpen = false
                editingTask = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyTab(
    tasks: List<Task>,
    today: String,
    reorderMode: Boolean,
    onTasksChange: (List<Task>) -> Unit,
    onTasksReorder: (List<Task>) -> Unit,
    onTasksReorderEnd: () -> Unit,
    onToggleReorderMode: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onNewTask: () -> Unit
) {
    val visibleTasks = tasks.filter { it.isVisibleOn(today) }
    val sorted = visibleTasks.sortedBy { if (it.isCompletedOn(today)) 1 else 0 }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromKey = from.key as? Long ?: return@rememberReorderableLazyListState
        val toKey = to.key as? Long ?: return@rememberReorderableLazyListState
        val fromIdx = tasks.indexOfFirst { it.id == fromKey }
        val toIdx = tasks.indexOfFirst { it.id == toKey }
        if (fromIdx < 0 || toIdx < 0 || fromIdx == toIdx) return@rememberReorderableLazyListState
        val fromCompleted = tasks[fromIdx].isCompletedOn(today)
        val toCompleted = tasks[toIdx].isCompletedOn(today)
        if (fromCompleted != toCompleted) return@rememberReorderableLazyListState
        onTasksReorder(tasks.toMutableList().apply { add(toIdx, removeAt(fromIdx)) })
    }

    val completedCount = visibleTasks.count { it.isCompletedOn(today) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                HeroHeader(completed = completedCount, total = visibleTasks.size)
                Spacer(Modifier.height(8.dp))
            }
            items(sorted, key = { it.id }) { task ->
                val isCompleted = task.isCompletedOn(today)
                ReorderableItem(
                    state = reorderState,
                    key = task.id
                ) { isDragging ->
                    val elevation = if (isDragging) 8.dp else 1.dp
                    val cardColor = if (isCompleted) {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .longPressDraggableHandle(
                                enabled = reorderMode,
                                onDragStopped = { onTasksReorderEnd() }
                            ),
                        shape = RoundedCornerShape(20.dp),
                        tonalElevation = elevation,
                        shadowElevation = elevation,
                        color = cardColor
                    ) {
                        TaskRow(
                            task = task,
                            isCompleted = isCompleted,
                            reorderMode = reorderMode,
                            onToggle = { checked ->
                                onTasksChange(tasks.map {
                                    if (it.id == task.id) {
                                        val newDates = if (checked) {
                                            it.completionDates + today
                                        } else {
                                            it.completionDates - today
                                        }
                                        it.copy(completionDates = newDates)
                                    } else it
                                })
                            },
                            onClick = { onTaskClick(task) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onToggleReorderMode,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            containerColor = if (reorderMode) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Icon(
                imageVector = if (reorderMode) Icons.Default.Done else Icons.Default.SwapVert,
                contentDescription = if (reorderMode) "Done reordering" else "Reorder tasks"
            )
        }

        FloatingActionButton(
            onClick = onNewTask,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "New task")
        }
    }
}

@Composable
fun TaskRow(
    task: Task,
    isCompleted: Boolean,
    reorderMode: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val alpha = if (isCompleted) 0.4f else 1f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (reorderMode) Modifier else Modifier.clickable(onClick = onClick)
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskLeadingIcon(task = task, alpha = alpha)
            Text(
                text = task.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                textDecoration = if (isCompleted) TextDecoration.LineThrough else null
            )
        }
        Spacer(Modifier.width(8.dp))
        if (reorderMode) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(12.dp)
            )
        } else {
            CircleCheckbox(
                checked = isCompleted,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
fun CircleCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val tint = if (checked) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.primary
    }
    IconButton(onClick = { onCheckedChange(!checked) }) {
        Icon(
            imageVector = if (checked) {
                Icons.Filled.CheckCircle
            } else {
                Icons.Filled.RadioButtonUnchecked
            },
            contentDescription = if (checked) "Mark not done" else "Mark done",
            tint = tint
        )
    }
}

@Composable
private fun TaskLeadingIcon(task: Task, alpha: Float) {
    val hasEmoji = !task.emoji.isNullOrEmpty()
    val vector = if (!hasEmoji && !task.iconName.isNullOrEmpty()) {
        IconCatalog.byName(task.iconName)
    } else null
    Box(
        modifier = Modifier
            .padding(end = 12.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        if (hasEmoji) {
            Text(text = task.emoji!!, fontSize = 20.sp)
        } else if (vector != null) {
            Icon(
                imageVector = vector,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun HeroHeader(completed: Int, total: Int) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.background
        )
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Text(
            text = todayLabel(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Daily",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.height(20.dp))
        ProgressSection(completed = completed, total = total)
    }
}

@Composable
private fun ProgressSection(completed: Int, total: Int) {
    val pct = if (total == 0) 0f else completed / total.toFloat()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (total == 0) "No tasks yet" else "$completed of $total done today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        if (total > 0) {
            Text(
                text = "${(pct * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    LinearProgressIndicator(
        progress = { pct },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}

private fun todayLabel(): String =
    SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

@Composable
fun TaskDialog(
    initial: Task?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String, iconName: String?, isOneTime: Boolean) -> Unit
) {
    val key = initial?.id
    var name by rememberSaveable(key) { mutableStateOf(initial?.name ?: "") }
    var emoji by rememberSaveable(key) { mutableStateOf(initial?.emoji ?: "") }
    var iconName by rememberSaveable(key) { mutableStateOf(initial?.iconName) }
    var isOneTime by rememberSaveable(key) { mutableStateOf(initial?.isOneTime ?: false) }
    var pickerOpen by rememberSaveable(key) { mutableStateOf(false) }
    var pickerTab by rememberSaveable(key) {
        mutableIntStateOf(if (initial?.iconName != null) 1 else 0)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New task" else "Edit task") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconPickerButton(
                        emoji = emoji,
                        iconName = iconName,
                        onClick = { pickerOpen = !pickerOpen }
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Task name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !isOneTime,
                        onClick = { isOneTime = false },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("Daily") }
                    SegmentedButton(
                        selected = isOneTime,
                        onClick = { isOneTime = true },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("One-time") }
                }
                if (pickerOpen) {
                    Spacer(Modifier.height(12.dp))
                    PrimaryTabRow(selectedTabIndex = pickerTab) {
                        Tab(
                            selected = pickerTab == 0,
                            onClick = { pickerTab = 0 },
                            text = { Text("Suggested") }
                        )
                        Tab(
                            selected = pickerTab == 1,
                            onClick = { pickerTab = 1 },
                            text = { Text("Icon") }
                        )
                        Tab(
                            selected = pickerTab == 2,
                            onClick = { pickerTab = 2 },
                            text = { Text("Emoji") }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    when (pickerTab) {
                        0 -> SuggestedPicker(
                            selectedEmoji = emoji,
                            selectedIcon = iconName,
                            onEmojiSelect = {
                                emoji = it
                                iconName = null
                                pickerOpen = false
                            },
                            onIconSelect = {
                                iconName = it
                                emoji = ""
                                pickerOpen = false
                            }
                        )
                        1 -> IconPicker(
                            selected = iconName,
                            onSelect = {
                                iconName = it
                                emoji = ""
                                pickerOpen = false
                            }
                        )
                        else -> SystemEmojiPicker(
                            onSelect = {
                                emoji = it
                                iconName = null
                                pickerOpen = false
                            }
                        )
                    }
                    TextButton(onClick = {
                        emoji = ""
                        iconName = null
                        pickerOpen = false
                    }) {
                        Text("None")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, emoji, iconName, isOneTime) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun IconPickerButton(emoji: String, iconName: String?, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.height(56.dp)
    ) {
        val vector = IconCatalog.byName(iconName)
        when {
            emoji.isNotEmpty() -> Text(emoji, fontSize = 22.sp)
            vector != null -> Icon(
                imageVector = vector,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            else -> Text("Icon", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SuggestedPicker(
    selectedEmoji: String,
    selectedIcon: String?,
    onEmojiSelect: (String) -> Unit,
    onIconSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SuggestedEmojis.chunked(8).forEach { rowEmojis ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowEmojis.forEach { e ->
                    EmojiChip(
                        emoji = e,
                        selected = e == selectedEmoji,
                        onClick = { onEmojiSelect(e) }
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        SuggestedIconNames.chunked(8).forEach { rowNames ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowNames.forEach { name ->
                    val vector = IconCatalog.byName(name)
                    if (vector != null) {
                        IconChip(
                            vector = vector,
                            description = name,
                            selected = name == selectedIcon,
                            onClick = { onIconSelect(name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IconPicker(
    selected: String?,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        IconCatalog.items.chunked(8).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowItems.forEach { (name, vector) ->
                    IconChip(
                        vector = vector,
                        description = name,
                        selected = name == selected,
                        onClick = { onSelect(name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemEmojiPicker(onSelect: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search emojis") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        if (query.isBlank()) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                factory = { context ->
                    EmojiPickerView(context).apply {
                        setOnEmojiPickedListener { picked ->
                            onSelect(picked.emoji)
                        }
                    }
                }
            )
        } else {
            val results = remember(query) { EmojiSearchData.search(query) }
            if (results.isEmpty()) {
                Text(
                    text = "No matches",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    results.take(48).chunked(8).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            row.forEach { e ->
                                EmojiChip(
                                    emoji = e,
                                    selected = false,
                                    onClick = { onSelect(e) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmojiChip(emoji: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    Surface(
        color = background,
        shape = CircleShape,
        onClick = onClick
    ) {
        Text(
            text = emoji,
            fontSize = 22.sp,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun IconChip(
    vector: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    Surface(
        color = background,
        shape = CircleShape,
        onClick = onClick
    ) {
        Icon(
            imageVector = vector,
            contentDescription = description,
            modifier = Modifier
                .padding(8.dp)
                .size(22.dp)
        )
    }
}
