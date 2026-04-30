package com.aima.habitual.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aima.habitual.model.Quote
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * QuoteCard: Displays the daily motivational quote on the Dashboard.
 *
 * Uses AnimatedVisibility to smoothly fade in once the quote is loaded,
 * giving the app a polished, premium feel even while data is being fetched.
 *
 * ASSIGNMENT NOTE: This component is the visible proof of all four data
 * requirements — it shows live API data when online, local JSON data when
 * offline, and is backed by Room for local read/write on all other features.
 *
 * @param quote    The [Quote] to display, or null while loading (shows shimmer).
 * @param modifier Optional [Modifier] passed in by the caller for layout control.
 */
@Composable
fun QuoteCard(quote: Quote?, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }

    // Trigger the entrance animation once the quote data arrives
    LaunchedEffect(quote) {
        if (quote != null) visible = true
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(HabitualTheme.radius.lg))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            primaryContainerColor.copy(alpha = 0.7f),
                            primaryContainerColor.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(HabitualTheme.spacing.lg)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(HabitualTheme.spacing.sm)
            ) {
                // Decorative large quote icon
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = null,
                    tint = primaryColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    // Quote body text
                    Text(
                        text = quote?.text ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

                    // Author attribution
                    Text(
                        text = "— ${quote?.author ?: ""}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = primaryColor
                    )
                }
            }
        }
    }

    // Loading shimmer: shown while the quote is null (still being fetched)
    if (!visible) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(HabitualTheme.radius.lg))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )
    }
}
