package com.aima.habitual.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aima.habitual.ui.components.rememberAssetPainter
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.viewmodel.CompanionViewModel

/**
 * CompanionDetailScreen (DETAIL VIEW):
 * Shows the full sprite and an "Active" toggle. Toggling on sets this companion as
 * the single active companion (any other active is cleared via the ViewModel).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionDetailScreen(
    companionName: String,
    companionViewModel: CompanionViewModel,
    onBack: () -> Unit
) {
    // ── Tokens ─────────────────────────────────
    val horizontalPad = HabitualTheme.spacing.xl
    val sectionGap = HabitualTheme.spacing.xl
    val itemGap = HabitualTheme.spacing.md
    val heroRadius = HabitualTheme.radius.xl
    val heroImageSize = 140.dp
    val heroBoxSize = 160.dp
    // ────────────────────────────────────────────

    val companions by companionViewModel.companions.collectAsState()
    val activeName by companionViewModel.activeCompanionName.collectAsState()
    // Renamed from 'companion' — that's a Kotlin keyword and can't be a variable name.
    val entry = companions.firstOrNull { it.name == companionName }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Companion Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        if (entry == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Companion not found.", color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }

        val isActive = entry.name == activeName
        val painter = rememberAssetPainter("companions/${entry.spriteAsset}/idle_0.png")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = horizontalPad)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
            Box(
                modifier = Modifier
                    .size(heroBoxSize)
                    .clip(RoundedCornerShape(heroRadius)),
                contentAlignment = Alignment.Center
            ) {
                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = "${entry.name} sprite",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(heroImageSize)
                    )
                } else {
                    Text(
                        text = entry.name.first().toString(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } // inner sized Box
            } // outer centering Box

            Spacer(modifier = Modifier.height(sectionGap))

            Text(
                text = entry.name,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = entry.species,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = HabitualTheme.spacing.lg)
            )

            HorizontalDivider()
            Spacer(modifier = Modifier.height(itemGap))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.padding(end = HabitualTheme.spacing.md)) {
                    Text(
                        text = "Set as active companion",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Walks across the bottom of every tab.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { checked ->
                        companionViewModel.setActive(if (checked) entry.name else null)
                    }
                )
            }

            Spacer(modifier = Modifier.height(itemGap))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(itemGap))

            Text(
                text = "Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))
            Text(
                text = "Required level: ${entry.requiredLevel}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (entry.unlockStatus) "Status: Unlocked" else "Status: Locked",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(sectionGap))
        }
    }
}
