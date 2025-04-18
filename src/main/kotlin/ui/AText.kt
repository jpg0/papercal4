package com.trillica.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import org.jetbrains.skia.*

// Helper to convert Compose TextStyle to Skia Font (basic version)
// Note: Handling font families/weights/styles robustly requires more complex mapping.
@Composable
private fun skiaFont(style: TextStyle): Font {
    return Font(
        typeface = FontMgr.default.matchFamilyStyle("Arial", FontStyle.NORMAL),
        size = style.fontSize.value
    ).apply {
        this.hinting = FontHinting.NONE
        this.edging = FontEdging.ALIAS
        this.isSubpixel = false
    }
}


/**
 * A Composable that draws text without anti-aliasing using Skia directly.
 * It respects Compose layout measurement and placement.
 *
 * Note: This implementation assumes a Compose for Desktop (Skiko) environment.
 * Font family/weight/style mapping is basic.
 */
@Composable
fun AliasedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
    overflow: TextOverflow = TextOverflow.Clip
) {
    val textMeasurer = rememberTextMeasurer()
    val skiaFont = skiaFont(style)

    // Remember the Skia Paint
    val skiaPaint = remember {
        Paint().apply {
            isAntiAlias = false
            isDither = false
            mode = PaintMode.FILL
            // Set other properties like style, stroke etc. if needed
        }
    }

    // State to hold the measurement result for the drawing phase
    // Needs to be mutable state to be updated in measure pass and read in draw pass
    var textLayoutResultState by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Update paint color whenever style.color changes
    LaunchedEffect(style.color) {
        skiaPaint.color = if (style.color != Color.Unspecified) {
            style.color.toArgb()
        } else {
            Color.Black.toArgb() // Default to black
        }
    }

    // Use Layout to measure the text and size the Canvas accordingly
    Layout(
        // The Canvas composable will be the single child measured ('content')
        content = {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Drawing happens here, within the DrawScope of the Canvas
                val layoutResult = textLayoutResultState // Read the measured result

                if (layoutResult != null) {
                    // Access the native canvas from the DrawScope's drawContext
                    drawContext.canvas.nativeCanvas.let { nativeCanvas ->

                        nativeCanvas.save() // Save canvas state
                        try {
                            // Clip to the bounds of this Canvas/DrawScope
                            nativeCanvas.clipRect(Rect.makeWH(size.width, size.height))

                            // Now draw - will be clipped by the rect above
                            nativeCanvas.drawString(
                                s = text,
                                x = 0f,
                                y = layoutResult.firstBaseline,
                                font = skiaFont,
                                paint = skiaPaint
                            )
                        } finally {
                            nativeCanvas.restore() // Restore to previous state (removes clip)
                        }
                    }
                }
            }
        },
        modifier = modifier // Apply the modifier passed by the user to the Layout
    ) { measurables, constraints ->
        // This lambda is the measure policy

        // 1. Measure the text intrinsically to determine desired size
        val measuredResult = textMeasurer.measure(
            AnnotatedString(text),
            softWrap = false,
            style = style,
            constraints = constraints, // Pass constraints for potential wrapping
            overflow = overflow
        )

        // Store the result in state to be used during the drawing phase
        textLayoutResultState = measuredResult

        // 2. Determine the size for the Layout based on text measurement
        val layoutWidth = measuredResult.size.width
        val layoutHeight = measuredResult.size.height

        // 3. Measure the actual child (the Canvas) - make it match the text size exactly
        // There should be exactly one measurable: the Canvas defined in 'content'
        require(measurables.size == 1)
        val canvasPlaceable = measurables.first().measure(
            Constraints.fixed(layoutWidth, layoutHeight)
        )

        // 4. Place the child (Canvas) within the Layout
        layout(layoutWidth, layoutHeight) {
            canvasPlaceable.placeRelative(0, 0)
        }
    }
}
// --- Example Usage ---
/*
@Composable
fun MyScreen() {
    Column {
        Text("Default Anti-Aliased Text:")
        Text("Hello Compose!", style = TextStyle(fontSize = 30.sp))

        Spacer(Modifier.height(20.dp))

        Text("Custom Aliased Text:")
        AliasedText(
            text = "Hello Compose!",
            style = TextStyle(fontSize = 30.sp, color = Color.Blue)
        )

        Spacer(Modifier.height(20.dp))

        Text("Small Aliased Text (more noticeable):")
        AliasedText(
            text = "Jagged edges?",
            style = TextStyle(fontSize = 14.sp, color = Color.Red)
        )
    }
}
*/