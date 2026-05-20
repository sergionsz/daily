package sergionsz.daily.widget

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.glance.material3.ColorProviders
import sergionsz.daily.ui.theme.BackgroundDark
import sergionsz.daily.ui.theme.BackgroundLight
import sergionsz.daily.ui.theme.OnBackgroundDark
import sergionsz.daily.ui.theme.OnBackgroundLight
import sergionsz.daily.ui.theme.OnSurfaceDark
import sergionsz.daily.ui.theme.OnSurfaceLight
import sergionsz.daily.ui.theme.OnSurfaceVariantDark
import sergionsz.daily.ui.theme.OnSurfaceVariantLight
import sergionsz.daily.ui.theme.SunsetOnPrimaryDark
import sergionsz.daily.ui.theme.SunsetOnPrimaryLight
import sergionsz.daily.ui.theme.SunsetPrimaryContainerDark
import sergionsz.daily.ui.theme.SunsetPrimaryContainerLight
import sergionsz.daily.ui.theme.SunsetPrimaryDark
import sergionsz.daily.ui.theme.SunsetPrimaryLight
import sergionsz.daily.ui.theme.SurfaceDark
import sergionsz.daily.ui.theme.SurfaceLight
import sergionsz.daily.ui.theme.SurfaceVariantDark
import sergionsz.daily.ui.theme.SurfaceVariantLight

val SunsetWidgetColors = ColorProviders(
    light = lightColorScheme(
        primary = SunsetPrimaryLight,
        onPrimary = SunsetOnPrimaryLight,
        primaryContainer = SunsetPrimaryContainerLight,
        background = BackgroundLight,
        onBackground = OnBackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = OnSurfaceVariantLight
    ),
    dark = darkColorScheme(
        primary = SunsetPrimaryDark,
        onPrimary = SunsetOnPrimaryDark,
        primaryContainer = SunsetPrimaryContainerDark,
        background = BackgroundDark,
        onBackground = OnBackgroundDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = OnSurfaceVariantDark
    )
)
