package sergionsz.daily.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import sergionsz.daily.MainActivity
import sergionsz.daily.R
import sergionsz.daily.TaskRepository
import sergionsz.daily.isCompletedOn
import sergionsz.daily.isVisibleOn

class TodayTasksWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = TaskRepository(context.applicationContext)
        val today = TaskRepository.todayKey()
        val visible = repo.load()
            .filter { it.isVisibleOn(today) }
            .sortedBy { if (it.isCompletedOn(today)) 1 else 0 }

        provideContent {
            GlanceTheme(colors = SunsetWidgetColors) {
                TodayWidgetContent(context, today, visible)
            }
        }
    }
}

@Composable
private fun TodayWidgetContent(
    context: Context,
    today: String,
    tasks: List<sergionsz.daily.Task>
) {
    val openApp = actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    )
    val completed = tasks.count { it.isCompletedOn(today) }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(GlanceTheme.colors.background)
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.primary)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable(openApp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "Today",
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = if (tasks.isEmpty()) "No tasks" else "$completed of ${tasks.size} done",
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimary,
                        fontSize = 12.sp
                    )
                )
            }
            Image(
                provider = ImageProvider(R.drawable.ic_widget_add),
                contentDescription = "Add task",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimary),
                modifier = GlanceModifier
                    .size(28.dp)
                    .clickable(
                        actionStartActivity(
                            Intent(context, MainActivity::class.java).apply {
                                action = MainActivity.ACTION_QUICK_ADD
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                        )
                    )
            )
        }

        if (tasks.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize().padding(12.dp).clickable(openApp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tap to add tasks",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(tasks, itemId = { it.id }) { task ->
                    TaskRow(task = task, isCompleted = task.isCompletedOn(today), openApp = openApp)
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: sergionsz.daily.Task,
    isCompleted: Boolean,
    openApp: androidx.glance.action.Action
) {
    val toggle = actionRunCallback<ToggleTaskAction>(
        actionParametersOf(ToggleTaskAction.taskIdKey to task.id)
    )
    val labelPrefix = when {
        !task.emoji.isNullOrEmpty() -> "${task.emoji}  "
        else -> ""
    }
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(
                if (isCompleted) R.drawable.ic_widget_check_on else R.drawable.ic_widget_check_off
            ),
            contentDescription = if (isCompleted) "Mark not done" else "Mark done",
            colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
            modifier = GlanceModifier.size(28.dp).clickable(toggle)
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = labelPrefix + task.name,
            modifier = GlanceModifier.defaultWeight().clickable(openApp),
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = 14.sp,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            maxLines = 2
        )
    }
}

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val id = parameters[taskIdKey] ?: return
        val repo = TaskRepository(context.applicationContext)
        val today = TaskRepository.todayKey()
        val updated = repo.load().map { t ->
            if (t.id == id) {
                val newDates = if (today in t.completionDates) {
                    t.completionDates - today
                } else {
                    t.completionDates + today
                }
                t.copy(completionDates = newDates)
            } else t
        }
        repo.save(updated)
        TodayTasksWidget().updateAll(context)
    }

    companion object {
        val taskIdKey = ActionParameters.Key<Long>("task_id")
    }
}

class TodayTasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayTasksWidget()
}

