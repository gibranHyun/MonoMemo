package com.monomemo.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.monomemo.app.ui.theme.AccentBlue
import com.monomemo.app.ui.theme.AccentLime
import com.monomemo.app.ui.theme.AccentOrange
import com.monomemo.app.ui.theme.AccentPink

@Composable
fun BrandStripe(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(AccentOrange, AccentBlue, AccentLime, AccentOrange),
                ),
            ),
    )
}

@Composable
fun PencilIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(80.dp, 100.dp)) {
        val w = size.width
        val h = size.height
        val bodyLeft = w * 0.38f
        val bodyRight = w * 0.62f
        val bodyTop = h * 0.08f
        val bodyBottom = h * 0.65f

        // Eraser (pink)
        drawRect(
            color = AccentPink,
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyRight - bodyLeft, h * 0.08f),
        )

        // Body (orange)
        drawRect(
            color = AccentOrange,
            topLeft = Offset(bodyLeft, bodyTop + h * 0.08f),
            size = Size(bodyRight - bodyLeft, bodyBottom - bodyTop - h * 0.08f),
        )

        // Tip (triangle)
        val tipPath = Path().apply {
            moveTo(bodyLeft, bodyBottom)
            lineTo(bodyRight, bodyBottom)
            lineTo(w * 0.5f, h * 0.82f)
            close()
        }
        drawPath(tipPath, Color(0xFFFFDC96))

        // Tip point
        drawCircle(
            color = Color(0xFF444444),
            radius = 2.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.82f),
        )

        // Color lines from pencil
        val lineStartX = w * 0.7f
        val lineEndX = w * 0.95f
        val strokeW = 2.5f.dp.toPx()
        drawLine(AccentOrange, Offset(lineStartX, h * 0.3f), Offset(lineEndX, h * 0.3f), strokeWidth = strokeW, cap = StrokeCap.Round)
        drawLine(AccentBlue, Offset(lineStartX, h * 0.4f), Offset(lineEndX, h * 0.4f), strokeWidth = strokeW, cap = StrokeCap.Round)
        drawLine(AccentLime, Offset(lineStartX, h * 0.5f), Offset(lineEndX, h * 0.5f), strokeWidth = strokeW, cap = StrokeCap.Round)
    }
}

@Composable
fun PencilEmptyState(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PencilIllustration()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun EmptyBoxIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(64.dp, 64.dp)) {
        val w = size.width
        val h = size.height
        val strokeW = 2.dp.toPx()
        val color = Color(0xFF9E9E9E)

        // Box body
        drawRect(
            color = color,
            topLeft = Offset(w * 0.2f, h * 0.4f),
            size = Size(w * 0.6f, h * 0.5f),
            style = Stroke(strokeW),
        )

        // Left flap
        val flapPath = Path().apply {
            moveTo(w * 0.2f, h * 0.4f)
            lineTo(w * 0.1f, h * 0.28f)
            lineTo(w * 0.5f, h * 0.28f)
            lineTo(w * 0.5f, h * 0.4f)
        }
        drawPath(flapPath, color, style = Stroke(strokeW))

        // Right flap
        val flapPath2 = Path().apply {
            moveTo(w * 0.8f, h * 0.4f)
            lineTo(w * 0.9f, h * 0.28f)
            lineTo(w * 0.5f, h * 0.28f)
        }
        drawPath(flapPath2, color, style = Stroke(strokeW))

        // Dashed lines inside box (emptiness)
        for (i in 0..2) {
            val y = h * 0.55f + i * h * 0.1f
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = Offset(w * 0.3f, y),
                end = Offset(w * 0.7f, y),
                strokeWidth = 1.dp.toPx(),
            )
        }
    }
}

@Composable
fun EmptyTrashState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyBoxIllustration()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "비어있음",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun MiniPencilIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp, 28.dp)) {
        val w = size.width
        val h = size.height
        val left = w * 0.25f
        val right = w * 0.75f

        // Eraser
        drawRect(AccentPink, Offset(left, 0f), Size(right - left, h * 0.12f))
        // Body
        drawRect(AccentOrange, Offset(left, h * 0.12f), Size(right - left, h * 0.6f))
        // Tip
        val tipPath = Path().apply {
            moveTo(left, h * 0.72f)
            lineTo(right, h * 0.72f)
            lineTo(w * 0.5f, h * 0.92f)
            close()
        }
        drawPath(tipPath, Color(0xFFFFDC96))
        drawCircle(Color(0xFF444444), 1.5f.dp.toPx(), Offset(w * 0.5f, h * 0.92f))
    }
}
