package sergionsz.daily

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class TaskRepository(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(): List<Task> {
        val json = prefs.getString(KEY_TASKS, null) ?: return emptyList()
        val arr = JSONArray(json)
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            val dates = mutableSetOf<String>()
            if (o.has("completionDates") && !o.isNull("completionDates")) {
                val datesArr = o.getJSONArray("completionDates")
                for (j in 0 until datesArr.length()) {
                    dates.add(datesArr.getString(j))
                }
            } else if (o.has("lastCompletedDate") && !o.isNull("lastCompletedDate")) {
                dates.add(o.getString("lastCompletedDate"))
            }
            Task(
                id = o.getLong("id"),
                name = o.getString("name"),
                emoji = if (o.has("emoji") && !o.isNull("emoji")) o.getString("emoji") else null,
                iconName = if (o.has("iconName") && !o.isNull("iconName")) o.getString("iconName") else null,
                completionDates = dates,
                isOneTime = o.optBoolean("isOneTime", false),
                scheduledDate = if (o.has("scheduledDate") && !o.isNull("scheduledDate")) o.getString("scheduledDate") else null
            )
        }
    }

    fun save(tasks: List<Task>) {
        val arr = JSONArray()
        tasks.forEach { t ->
            val o = JSONObject()
            o.put("id", t.id)
            o.put("name", t.name)
            o.put("emoji", t.emoji ?: JSONObject.NULL)
            o.put("iconName", t.iconName ?: JSONObject.NULL)
            val datesArr = JSONArray()
            t.completionDates.sorted().forEach { datesArr.put(it) }
            o.put("completionDates", datesArr)
            o.put("lastCompletedDate", t.lastCompletedDate ?: JSONObject.NULL)
            o.put("isOneTime", t.isOneTime)
            o.put("scheduledDate", t.scheduledDate ?: JSONObject.NULL)
            arr.put(o)
        }
        prefs.edit().putString(KEY_TASKS, arr.toString()).apply()
    }

    companion object {
        private const val PREFS = "daily_tasks"
        private const val KEY_TASKS = "tasks"

        fun todayKey(): String {
            val cal = Calendar.getInstance()
            return "%04d-%02d-%02d".format(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}
