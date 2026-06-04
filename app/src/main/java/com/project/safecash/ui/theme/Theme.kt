package com.project.safecash.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryBlue,
    tertiary = SuccessGreen,
    background = TextPrimary,
    surface = TextPrimary,
    onPrimary = SurfaceWhite,
    onSecondary = SurfaceWhite,
    onTertiary = SurfaceWhite,
    onBackground = SurfaceWhite,
    onSurface = SurfaceWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryBlue,
    tertiary = SuccessGreen,
    background = BackgroundLight,
    surface = SurfaceWhite,
    onPrimary = SurfaceWhite,
    onSecondary = SurfaceWhite,
    onTertiary = SurfaceWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun SafeCashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Solución Senior: Eliminamos statusBarColor (deprecated).
            // Ya habilitamos enableEdgeToEdge() en MainActivity, por lo que el sistema maneja el fondo.
            // Gestionamos la visibilidad de los iconos para garantizar legibilidad absoluta.
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // Los iconos de la barra (reloj, batería) deben contrastar con el fondo:
            // Si el tema es oscuro, los iconos deben ser blancos.
            // Si el tema es claro, los iconos deben ser negros.
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
