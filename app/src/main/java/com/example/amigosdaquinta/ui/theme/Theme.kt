package com.example.amigosdaquinta.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Esquema de cores Escuro (Dark Mode).
 */
private val DarkColorScheme = darkColorScheme(
    primary = IndigoPadrão,
    secondary = LavandaClaro,
    tertiary = VerdeSucesso,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

/**
 * Esquema de cores Claro (Light Mode) - Padrão oficial do App.
 */
private val LightColorScheme = lightColorScheme(
    primary = IndigoPadrão,
    secondary = LavandaClaro,
    tertiary = VerdeSucesso,
    background = CinzaFundo,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

/**
 * Tema principal da aplicação Amigos da Quinta.
 */
@Composable
fun AmigosDaQuintaTheme(
    darkTheme: Boolean = false, // feito para corrigir erros de UI no programa devido ao tema escuro
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Faz com que a barra de status combine com a cor primária (Indigo)
            window.statusBarColor = colorScheme.primary.toArgb()
            // Configura os ícones da barra de status para branco (já que Indigo é escuro)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
