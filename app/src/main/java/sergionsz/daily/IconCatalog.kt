package sergionsz.daily

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object IconCatalog {
    val items: List<Pair<String, ImageVector>> = listOf(
        "Star" to Icons.Filled.Star,
        "Favorite" to Icons.Filled.Favorite,
        "EmojiEvents" to Icons.Filled.EmojiEvents,
        "Bolt" to Icons.Filled.Bolt,
        "AutoAwesome" to Icons.Filled.AutoAwesome,
        "Flag" to Icons.Filled.Flag,
        "CheckCircle" to Icons.Filled.CheckCircle,
        "Whatshot" to Icons.Filled.Whatshot,
        "FitnessCenter" to Icons.Filled.FitnessCenter,
        "DirectionsRun" to Icons.AutoMirrored.Filled.DirectionsRun,
        "DirectionsBike" to Icons.AutoMirrored.Filled.DirectionsBike,
        "DirectionsWalk" to Icons.AutoMirrored.Filled.DirectionsWalk,
        "Pool" to Icons.Filled.Pool,
        "SelfImprovement" to Icons.Filled.SelfImprovement,
        "Spa" to Icons.Filled.Spa,
        "Bedtime" to Icons.Filled.Bedtime,
        "LocalDrink" to Icons.Filled.LocalDrink,
        "LocalCafe" to Icons.Filled.LocalCafe,
        "Restaurant" to Icons.Filled.Restaurant,
        "MenuBook" to Icons.AutoMirrored.Filled.MenuBook,
        "School" to Icons.Filled.School,
        "Edit" to Icons.Filled.Edit,
        "Computer" to Icons.Filled.Computer,
        "MusicNote" to Icons.Filled.MusicNote,
        "Headphones" to Icons.Filled.Headphones,
        "Brush" to Icons.Filled.Brush,
        "LocalFlorist" to Icons.Filled.LocalFlorist,
        "Shower" to Icons.Filled.Shower,
        "CleaningServices" to Icons.Filled.CleaningServices,
        "AccessTime" to Icons.Filled.AccessTime,
        "CalendarMonth" to Icons.Filled.CalendarMonth,
        "Home" to Icons.Filled.Home,
        "Work" to Icons.Filled.Work
    )

    private val byNameMap: Map<String, ImageVector> = items.toMap()

    fun byName(name: String?): ImageVector? = name?.let { byNameMap[it] }
}
