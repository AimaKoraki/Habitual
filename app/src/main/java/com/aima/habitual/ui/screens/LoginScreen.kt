package com.aima.habitual.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * LoginScreen: The primary authentication portal for returning users.
 * Designed with a focus on simplicity, security, and visual continuity
 * with the rest of the app's reflective aesthetic.
 */
@Composable
fun LoginScreen(
    onLoginAttempt: (String, String) -> Unit,
    errorMessage: String?,
    onNavigateToRegister: () -> Unit
) {
    // 1. AUTHENTICATION STATE: Tracks user input locally before verification
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // UI Guard: Ensures buttons are only active when data is present
    val isFormValid = email.isNotBlank() && password.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Applies the signature calm wave texture to the entry screen
            .wavePattern(MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = HabitualTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── HERO TITLE ──
            Text(
                text = stringResource(R.string.login_welcome),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            // ── EMAIL INPUT ──
            TextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    Text(
                        stringResource(R.string.login_email_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = HabitualTheme.components.borderThin,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(HabitualTheme.radius.xl)
                    ),
                singleLine = true,
                shape = RoundedCornerShape(HabitualTheme.radius.xl),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

            // ── PASSWORD INPUT ──
            TextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        stringResource(R.string.login_password_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted)
                    )
                },
                visualTransformation = PasswordVisualTransformation(), // Secure text entry
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = HabitualTheme.components.borderThin,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(HabitualTheme.radius.xl)
                    ),
                singleLine = true,
                shape = RoundedCornerShape(HabitualTheme.radius.xl),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )

            // ── ERROR HANDLING ──
            // Provides dynamic feedback if authentication fails (e.g., "Invalid Credentials")
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            // ── PRIMARY ACTION (CTA) ──
            Button(
                onClick = { onLoginAttempt(email, password) },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HabitualTheme.components.buttonHeight),
                shape = RoundedCornerShape(HabitualTheme.radius.xl),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = HabitualTheme.elevation.low,
                    pressedElevation = HabitualTheme.elevation.medium
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    // Subtle alpha adjustments for disabled states ensure consistency
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                        alpha = HabitualTheme.alpha.disabled
                    )
                )
            ) {
                Text(
                    stringResource(R.string.login_btn_text),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

            // ── HELPER LINK: Directs new users to the registration path ──
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    stringResource(R.string.login_register_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = HabitualTheme.alpha.secondary
                    )
                )
            }
        }
    }
}