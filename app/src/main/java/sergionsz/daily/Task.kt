package sergionsz.daily

data class Task(
    val id: Long,
    val name: String,
    val emoji: String? = null,
    val iconName: String? = null,
    val lastCompletedDate: String? = null,
    val isOneTime: Boolean = false
)

fun Task.isCompletedOn(today: String): Boolean =
    if (isOneTime) lastCompletedDate != null else lastCompletedDate == today

fun Task.isVisibleOn(today: String): Boolean {
    if (!isOneTime) return true
    return lastCompletedDate == null || lastCompletedDate == today
}
