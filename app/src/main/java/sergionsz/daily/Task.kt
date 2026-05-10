package sergionsz.daily

data class Task(
    val id: Long,
    val name: String,
    val emoji: String? = null,
    val iconName: String? = null,
    val lastCompletedDate: String? = null
)
