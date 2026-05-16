package sergionsz.daily

data class Task(
    val id: Long,
    val name: String,
    val emoji: String? = null,
    val iconName: String? = null,
    val completionDates: Set<String> = emptySet(),
    val isOneTime: Boolean = false,
    val scheduledDate: String? = null
) {
    val lastCompletedDate: String? get() = completionDates.maxOrNull()
}

fun Task.isCompletedOn(date: String): Boolean = date in completionDates

fun Task.isVisibleOn(today: String): Boolean {
    if (!isOneTime) return true
    if (today in completionDates) return true
    if (completionDates.isNotEmpty()) return false
    val scheduled = scheduledDate ?: return true
    return scheduled <= today
}

fun Task.shouldBePersisted(today: String): Boolean {
    if (!isOneTime) return true
    if (completionDates.isEmpty()) return true
    return today in completionDates
}
