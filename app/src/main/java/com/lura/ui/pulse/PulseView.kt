package com.lura.ui.pulse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.geometry.Offset
import com.lura.ui.theme.LuraGhostWhite
import com.lura.ui.theme.LuraIndigo
import com.lura.domain.engine.PulseWord

@Composable
fun PulseView(
    pulseWord: PulseWord
) {
    val leftPart = pulseWord.word.substring(0, pulseWord.pivotIndex)
    val pivotChar = pulseWord.word[pulseWord.pivotIndex]
    val rightPart = pulseWord.word.substring(pulseWord.pivotIndex + 1)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. The Reticle (Eye Anchor)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val markerLength = 12.dp.toPx()
            val markerThickness = 2.dp.toPx()
            val markerGap = 32.dp.toPx()

            // Top Marker
            drawLine(
                color = LuraIndigo.copy(alpha = 0.4f),
                start = Offset(centerX, centerY - markerGap),
                end = Offset(centerX, centerY - markerGap - markerLength),
                strokeWidth = markerThickness
            )

            // Bottom Marker
            drawLine(
                color = LuraIndigo.copy(alpha = 0.4f),
                start = Offset(centerX, centerY + markerGap),
                end = Offset(centerX, centerY + markerGap + markerLength),
                strokeWidth = markerThickness
            )
        }

        // 2. The Word (Precise Centering)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Part
            Text(
                text = leftPart,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 0.sp
                ),
                color = LuraGhostWhite.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )

            // Pivot Character (ORP)
            Text(
                text = pivotChar.toString(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                ),
                color = LuraIndigo,
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            // Right Part
            Text(
                text = rightPart,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 0.sp
                ),
                color = LuraGhostWhite.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
        }
    }
}
