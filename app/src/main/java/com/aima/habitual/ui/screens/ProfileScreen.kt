package com.aima.habitual.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aima.habitual.R

@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,            // Received from NavGraph -> MainScreen
    onThemeChange: (Boolean) -> Unit // Received from NavGraph -> MainScreen
) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "User Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // REQUIREMENT: Optimized Media (using WebP or SVG)
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile_placeholder),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // REQUIREMENT: Suitably formatted text with Material3 Typography
        Text(text = "Computer Science Student", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Joined: January 2026",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // REQUIREMENT: Card Component for layout organization
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Account Settings", style = MaterialTheme.typography.labelLarge)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                ProfileSettingItem(
                    icon = Icons.Default.Badge,
                    label = "User ID",
                    value = "CS-2023-04"
                )
                ProfileSettingItem(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = "student@aima.habitual.lk"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // REQUIREMENT: Component Variety (Switches for Preferences)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Preferences", style = MaterialTheme.typography.labelLarge)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Notification Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Notifications")
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Manual Theme Toggle (Requirement: Changes app mode)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Dark Mode")
                    }
                    // This Switch now controls the GLOBAL theme state
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onThemeChange(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileSettingItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}