package com.aima.habitual.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * LoginScreen: The primary authentication portal for returning users.
 * Supports email/password, Google Sign-In, and biometric Quick Login.
 *
 * Stateless: Only triggers authentication and observes isLoggedIn from ViewModel.
 */
@Composable
fun LoginScreen(
    onLoginAttempt: (String, String) -> Unit,
    errorMessage: String?,
    onNavigateToRegister: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onBiometricLogin: () -> Unit,
    isBiometricAvailable: Boolean
) {
    // Authentication state: Tracks user input locally before verification
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // UI Guard: Ensures buttons are only active when data is present
    val isFormValid = email.isNotBlank() && password.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                visualTransformation = PasswordVisualTransformation(),
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
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            // ── PRIMARY ACTION (Email/Password Login) ──
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

            // ── "OR" DIVIDER ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = stringResource(R.string.login_or_divider),
                    modifier = Modifier.padding(horizontal = HabitualTheme.spacing.lg),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.secondary)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

            // ── GOOGLE SIGN-IN BUTTON ──
            ElevatedButton(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HabitualTheme.components.buttonHeight),
                shape = RoundedCornerShape(HabitualTheme.radius.xl),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = HabitualTheme.elevation.low
                ),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "G",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(HabitualTheme.spacing.md))
                Text(
                    stringResource(R.string.login_google_btn),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // ── BIOMETRIC QUICK LOGIN (Conditional) ──
            if (isBiometricAvailable) {
                Spacer(modifier = Modifier.height(HabitualTheme.spacing.md))

                ElevatedButton(
                    onClick = onBiometricLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HabitualTheme.components.buttonHeight),
                    shape = RoundedCornerShape(HabitualTheme.radius.xl),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = HabitualTheme.elevation.low
                    ),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = stringResource(R.string.login_biometric_desc),
                        modifier = Modifier.size(HabitualTheme.components.iconDefault)
                    )
                    Spacer(modifier = Modifier.width(HabitualTheme.spacing.md))
                    Text(
                        stringResource(R.string.login_biometric_btn),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
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