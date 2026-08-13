package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun QrCodeCanvas(
    qrContent: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 180.dp,
    qrColor: Color = Color(0xFF0F172A),
    backgroundColor: Color = Color(0xFFFFFFFF)
) {
    // Generate deterministic 21x21 QR grid based on content hash
    val matrix = remember(qrContent) {
        generateDeterministicQrGrid(qrContent)
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(12.dp)
            .semantics {
                testTag = "qr_code_canvas"
                contentDescription = "Código QR de acceso generado para $qrContent"
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp - 24.dp)) {
            val gridCount = 21
            val cellSize = this.size.width / gridCount

            // Background fill
            drawRect(
                color = backgroundColor,
                size = Size(this.size.width, this.size.height)
            )

            // Draw modules
            for (r in 0 until gridCount) {
                for (c in 0 until gridCount) {
                    if (matrix[r][c]) {
                        drawRect(
                            color = qrColor,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize + 0.5f, cellSize + 0.5f)
                        )
                    }
                }
            }
        }
    }
}

private fun generateDeterministicQrGrid(content: String): Array<BooleanArray> {
    val size = 21
    val grid = Array(size) { BooleanArray(size) }
    val hash = content.hashCode()

    // 1. Finder patterns at 3 corners (7x7 top-left, top-right, bottom-left)
    fun drawFinderPattern(startR: Int, startC: Int) {
        for (r in 0..6) {
            for (c in 0..6) {
                val isOuter = r == 0 || r == 6 || c == 0 || c == 6
                val isInner = r in 2..4 && c in 2..4
                grid[startR + r][startC + c] = isOuter || isInner
            }
        }
    }

    drawFinderPattern(0, 0)
    drawFinderPattern(0, size - 7)
    drawFinderPattern(size - 7, 0)

    // 2. Timing patterns (Row 6, Col 6)
    for (i in 7 until size - 7) {
        grid[6][i] = i % 2 == 0
        grid[i][6] = i % 2 == 0
    }

    // 3. Fill data modules deterministically using content string bytes
    val bytes = content.toByteArray()
    var byteIdx = 0
    for (r in 0 until size) {
        for (c in 0 until size) {
            // Skip finder patterns
            if ((r < 7 && c < 7) || (r < 7 && c >= size - 7) || (r >= size - 7 && c < 7)) continue
            // Skip timing patterns
            if (r == 6 || c == 6) continue

            val b = bytes[byteIdx % bytes.size].toInt()
            val seed = abs(hash xor (r * 31 + c * 17) xor b)
            grid[r][c] = (seed % 2 == 0)
            byteIdx++
        }
    }

    return grid
}
