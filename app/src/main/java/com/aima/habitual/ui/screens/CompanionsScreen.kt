package com.aima.habitual.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aima.habitual.model.VirtualCompanion
import com.aima.habitual.ui.components.ScreenHeader
import com.aima.habitual.ui.components.rememberAssetPainter
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.CompanionViewModel
import com.aima.habitual.viewmodel.HabitViewModel

/**
 * CompanionsScreen (MASTER VIEW):
 * 2-column grid of companions. Locked entries (userLevel < requiredLevel) render with
 * a grayscale color filter and are non-clickable. Tapping an unlocked entry navigates
 * to [CompanionDetailScreen] where the user can set it as the active companion.
 */
@Composable
fun CompanionsScreen(
    habitViewModel: HabitViewModel,
    companionViewModel: CompanionViewModel,
    onCompanionClick: (String) -> Unit
) {
    // ── Tokens ─────────────────────────────────
    val outerPad = HabitualTheme.spacing.lg
    val gutter = HabitualTheme.spacing.md
    val gridColumns = 2
    val bottomContentPad = HabitualTheme.spacing.navBarPad
    // ────────────────────────────────────────────

    val companions by companionViewModel.companions.collectAsState()
    val activeName by companionViewModel.activeCompanionName.collectAsState()

    // Subscribe to records to force recomposition when gamification stats change
    val records by habitViewModel.records.collectAsState()
    val userLevel = habitViewModel.currentLevel

    LaunchedEffect(userLevel, companions.size) {
        companionViewModel.onUserLevelChanged(userLevel)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = outerPad)
    ) {
        ScreenHeader(title = "Virtual Companions")

        Text(
            text = "Complete habits to level up and unlock new companions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = HabitualTheme.spacing.md)
        )

        if (companions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                verticalArrangement = Arrangement.spacedBy(gutter),
                horizontalArrangement = Arrangement.spacedBy(gutter),
                contentPadding = PaddingValues(bottom = bottomContentPad)
            ) {
                items(companions, key = { it.name }) { companion ->
                    CompanionTile(
                        companion = companion,
                        isActive = companion.name == activeName,
                        onClick = {
                            if (companion.unlockStatus) onCompanionClick(companion.name)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompanionTile(
    companion: VirtualCompanion,
    isActive: Boolean,
    onClick: () -> Unit
) {
    // ── Tokens ─────────────────────────────────
    val cardRadius = HabitualTheme.radius.lg
    val cardElev = if (isActive) HabitualTheme.elevation.medium else HabitualTheme.elevation.low
    val padding = HabitualTheme.spacing.md
    val spriteSize = 96.dp
    val lockedAlpha = HabitualTheme.alpha.muted
    val activeBorderColor = MaterialTheme.colorScheme.primary
    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val grayscaleFilter = remember {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    }
    // ────────────────────────────────────────────

    val painter = rememberAssetPainter("companions/${companion.spriteAsset}/idle_0.png")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = companion.unlockStatus, onClick = onClick),
        shape = RoundedCornerShape(cardRadius),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElev)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(spriteSize)
                    .clip(RoundedCornerShape(HabitualTheme.radius.md))
                    .alpha(if (companion.unlockStatus) 1f else lockedAlpha),
                contentAlignment = Alignment.Center
            ) {
                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = "${companion.name} sprite",
                        contentScale = ContentScale.Fit,
                        colorFilter = if (!companion.unlockStatus) grayscaleFilter else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "?",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))

            Text(
                text = companion.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (companion.unlockStatus) {
                    if (isActive) "Active" else companion.species
                } else {
                    "Unlock at Lvl ${companion.requiredLevel}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) activeBorderColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
