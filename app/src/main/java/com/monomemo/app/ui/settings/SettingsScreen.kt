package com.monomemo.app.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monomemo.app.R
import com.monomemo.app.data.settings.SettingsDataStore
import com.monomemo.app.ui.theme.AccentOrange
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val PRIVACY_POLICY_URL = "https://gibranhyun.github.io/MonoMemo/privacy-policy.html"
private const val LICENSE_URL = "https://scripts.sil.org/OFL"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsDataStore: SettingsDataStore,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val themeMode by settingsDataStore.themeMode.collectAsState(initial = "system")
    val fontSizeSp by settingsDataStore.fontSizeSp.collectAsState(initial = 16)
    val wrapEnabled by settingsDataStore.wrapEnabled.collectAsState(initial = true)
    val lineNumbersEnabled by settingsDataStore.lineNumbersEnabled.collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.trash_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // APPEARANCE section
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.settings_appearance),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange,
                    letterSpacing = 1.sp,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Theme Mode
                Text(
                    text = stringResource(R.string.settings_theme_mode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.settings_theme_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3-button segment selector
                ThemeSegmentSelector(
                    selected = themeMode,
                    onSelect = { scope.launch { settingsDataStore.setThemeMode(it) } },
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Font Size
                Text(
                    text = stringResource(R.string.settings_font_size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.settings_font_size_desc, fontSizeSp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // A slider (small A ... big A)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "A",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                    Slider(
                        value = fontSizeSp.toFloat(),
                        onValueChange = { scope.launch { settingsDataStore.setFontSizeSp(it.roundToInt()) } },
                        valueRange = 10f..30f,
                        steps = 19,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentOrange,
                            activeTrackColor = AccentOrange,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                    Text(
                        "A",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // EDITOR PREFERENCES section
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.settings_editor_preferences),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange,
                    letterSpacing = 1.sp,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Word Wrap
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_word_wrap),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.settings_word_wrap_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = wrapEnabled,
                        onCheckedChange = { scope.launch { settingsDataStore.setWrapEnabled(it) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = AccentOrange,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Show Line Numbers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_line_numbers),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.settings_line_numbers_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = lineNumbersEnabled,
                        onCheckedChange = { scope.launch { settingsDataStore.setLineNumbersEnabled(it) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = AccentOrange,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // About section (integrated)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Orange rounded square "M" logo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentOrange),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "M",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "MonoMemo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    stringResource(R.string.settings_version, com.monomemo.app.BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    stringResource(R.string.settings_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // License and Privacy Policy links
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.settings_license),
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentOrange,
                        modifier = Modifier.clickable { uriHandler.openUri(LICENSE_URL) },
                    )
                    Text(
                        "  \u00B7  ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.settings_privacy_policy),
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentOrange,
                        modifier = Modifier.clickable { uriHandler.openUri(PRIVACY_POLICY_URL) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ThemeSegmentSelector(
    selected: String,
    onSelect: (String) -> Unit,
) {
    val options = listOf("system", "light", "dark")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
    ) {
        options.forEach { mode ->
            val isSelected = selected == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh
                        else Color.Transparent,
                    )
                    .then(
                        if (isSelected) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        else Modifier,
                    )
                    .clickable { onSelect(mode) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ThemeIcon(mode = mode, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (mode) {
                            "system" -> stringResource(R.string.settings_theme_system)
                            "light" -> stringResource(R.string.settings_theme_light)
                            else -> stringResource(R.string.settings_theme_dark)
                        }.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeIcon(mode: String, color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val strokeW = 2.dp.toPx()

        when (mode) {
            "system" -> {
                // Monitor icon
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.15f, h * 0.15f),
                    size = Size(w * 0.7f, h * 0.5f),
                    cornerRadius = CornerRadius(2.dp.toPx()),
                    style = Stroke(strokeW),
                )
                // Stand
                drawLine(color, Offset(cx, h * 0.65f), Offset(cx, h * 0.78f), strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.3f, h * 0.78f), Offset(w * 0.7f, h * 0.78f), strokeWidth = strokeW, cap = StrokeCap.Round)
            }
            "light" -> {
                // Sun icon
                val sunR = w * 0.15f
                drawCircle(color = color, radius = sunR, center = Offset(cx, cy), style = Stroke(strokeW))
                // Rays
                val rayLen = w * 0.12f
                val rayStart = sunR + w * 0.06f
                for (i in 0 until 8) {
                    val angle = Math.toRadians(i * 45.0)
                    val sx = cx + (rayStart * Math.cos(angle)).toFloat()
                    val sy = cy + (rayStart * Math.sin(angle)).toFloat()
                    val ex = cx + ((rayStart + rayLen) * Math.cos(angle)).toFloat()
                    val ey = cy + ((rayStart + rayLen) * Math.sin(angle)).toFloat()
                    drawLine(color, Offset(sx, sy), Offset(ex, ey), strokeWidth = strokeW, cap = StrokeCap.Round)
                }
            }
            "dark" -> {
                // Moon (crescent)
                val moonPath = Path().apply {
                    // Outer circle arc
                    val r = w * 0.3f
                    val ocx = cx - w * 0.05f
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            ocx - r, cy - r, ocx + r, cy + r,
                        ),
                    )
                }
                drawPath(moonPath, color = color, style = Stroke(strokeW))
                // Inner cutout (just draw a circle with background color to simulate crescent)
                drawCircle(
                    color = color,
                    radius = w * 0.2f,
                    center = Offset(cx + w * 0.12f, cy - h * 0.1f),
                    style = Stroke(strokeW * 0.8f),
                )
            }
        }
    }
}
