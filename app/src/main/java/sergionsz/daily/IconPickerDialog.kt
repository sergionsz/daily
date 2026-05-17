package sergionsz.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerDialog(
    currentEmoji: String,
    currentIconName: String?,
    onDismiss: () -> Unit,
    onSelectEmoji: (String) -> Unit,
    onSelectIcon: (String) -> Unit,
    onClear: () -> Unit
) {
    var tab by rememberSaveable {
        mutableIntStateOf(if (currentIconName != null) 1 else 0)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose icon") },
        text = {
            Column {
                PrimaryTabRow(selectedTabIndex = tab) {
                    Tab(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        text = { Text("Suggested") }
                    )
                    Tab(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        text = { Text("Icon") }
                    )
                    Tab(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        text = { Text("Emoji") }
                    )
                }
                Spacer(Modifier.height(8.dp))
                when (tab) {
                    0 -> SuggestedPicker(
                        selectedEmoji = currentEmoji,
                        selectedIcon = currentIconName,
                        onEmojiSelect = onSelectEmoji,
                        onIconSelect = onSelectIcon
                    )
                    1 -> IconGridPicker(
                        selected = currentIconName,
                        onSelect = onSelectIcon
                    )
                    else -> SystemEmojiPicker(onSelect = onSelectEmoji)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onClear) { Text("None") }
        }
    )
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
private fun IconGridPicker(
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
