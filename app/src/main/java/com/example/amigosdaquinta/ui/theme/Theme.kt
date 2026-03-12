package com.example.amigosdaquinta.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
