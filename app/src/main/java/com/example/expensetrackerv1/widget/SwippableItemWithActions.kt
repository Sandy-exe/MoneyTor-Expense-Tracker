package com.example.expensetrackerv1.widget

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwippableItemWithActions(
    isRevealedLeft: Boolean,
    isRevealedRight: Boolean,
    beforeContentActions: @Composable RowScope.() -> Unit = {}, // Actions for left swipe
    afterContentActions: @Composable RowScope.() -> Unit = {},  // Actions for right swipe
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var beforeActionsWidth by remember { mutableFloatStateOf(0f) }
    var afterActionsWidth by remember { mutableFloatStateOf(0f) }
    val offset = remember { Animatable(0f) } // 0: content, +ve: right swipe, -ve: left swipe
    val scope = rememberCoroutineScope()

    // Sync offset with the revealed state
    LaunchedEffect(key1 = isRevealedLeft, key2 = isRevealedRight) {
        if (isRevealedLeft) {
            offset.animateTo(-beforeActionsWidth)
        } else if (isRevealedRight) {
            offset.animateTo(afterActionsWidth)
        } else {
            offset.animateTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Actions behind the content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    beforeActionsWidth = it.width.toFloat() / 5 // Left swipe area width
                    afterActionsWidth = it.width.toFloat() / 5 // Right swipe area width
                },
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Left swipe actions (before content)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .height(IntrinsicSize.Min),

                contentAlignment = Alignment.Center
            ) {

                Row(){beforeContentActions()}
            }

            Spacer(modifier = Modifier.weight(1f))

            // Right swipe actions (after content)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .height(IntrinsicSize.Min),
                contentAlignment = Alignment.Center
            ) {

                Row(){afterContentActions()}

            }
        }

        // Foreground content
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newOffset = (offset.value + dragAmount)
                                    .coerceIn(-beforeActionsWidth, afterActionsWidth)
                                offset.snapTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offset.value <= -beforeActionsWidth / 4 -> {
                                        offset.animateTo(-beforeActionsWidth)
                                    }
                                    offset.value >= afterActionsWidth / 4 -> {
                                        offset.animateTo(afterActionsWidth)
                                    }
                                    else -> {
                                        offset.animateTo(0f)
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}


@Composable
fun ActionIcon(
    onClick: () -> Unit,
    backgroundColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Color.White,
    padding: Dp = 16.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10)) // Rounded button
            .background(backgroundColor)
            .clickable(
                onClick = onClick,
            )
            .padding(padding)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}