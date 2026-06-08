package com.aima.habitual.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.aima.habitual.R
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * RegisterScreen: The entry point for new users.
 * Combines secure credential handling with a "Reflective" design aesthetic.
 */
@Composable
fun RegisterScreen(
    onRegisterSuccess: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // 1. FORM STATE: Tracks local inputs before submission
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    // VALIDATION LOGIC: Ensures mandatory fields are populated and criteria met
    val isFormValid = termsAccepted && email.isNotBlank() && password.length >= 6 && userName.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Procedural background pattern for visual continuity
            .wavePattern(MaterialTheme.colorScheme.primary)
    ) {
        // BUG FIX 1: Do NOT combine verticalArrangement = Arrangement.Center with
        // verticalScroll. When a Column is measured with verticalScroll its height
        // is unbounded (infinite). Arrangement.Center then has nothing to centre
        // within, AND the layout no longer correctly responds to WindowInsets
        // (keyboard), so the Sign Up button becomes unreachable on small screens.
        //
        // Solution: Arrangement.Top + a pair of weight(1f) Spacers. The spacers
        // give the same visually-centred look on tall screens, but collapse to
        // nothing on small screens / when the keyboard is open, allowing proper
        // scroll and keyboard-avoidance.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HabitualTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Flexible top spacer — pushes content down on tall screens to look
            // centred, but collapses to nothing on small screens / with keyboard open.
            Spacer(modifier = Modifier.weight(1f))

            // ── HERO TITLE ──
            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            // ── NAME INPUT ──
            TextField(
                value = userName,
                onValueChange = { userName = it },
                label = {
                    Text(
                        stringResource(R.string.register_name_hint),
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

            // ── EMAIL INPUT ──
            TextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    Text(
                        stringResource(R.string.register_email_hint),
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
            // Uses PasswordVisualTransformation to obscure sensitive text
            TextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        stringResource(R.string.register_password_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted)
                    )
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
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

            // ── TERMS CHECKBOX ──
            // BUG FIX 2: The Row had `.clickable { termsAccepted = !termsAccepted }`
            // AND the Checkbox had `onCheckedChange = { termsAccepted = it }`.
            // A single tap fired BOTH handlers: the Checkbox set it true, then the
            // Row's clickable immediately toggled it back to false — so terms were
            // never actually accepted and Sign Up stayed permanently disabled.
            //
            // Fix: set Checkbox onCheckedChange = null so it acts as a pure visual
            // indicator. All state changes are driven solely by the Row's clickable.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { termsAccepted = !termsAccepted },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = null, // Row's clickable is the single source of truth
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted)
                    )
                )
                Text(
                    stringResource(R.string.register_terms),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.secondary)
                )
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

            // ── PRIMARY ACTION (CTA) ──
            Button(
                onClick = { onRegisterSuccess(userName, email, password) },
                enabled = isFormValid, // Button activates only when all inputs are valid
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
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    stringResource(R.string.register_btn_text),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

            // ── HELPER LINK: Alternative path for existing users ──
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    stringResource(R.string.register_login_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.secondary)
                )
            }

            // Flexible bottom spacer — mirrors the top spacer so content stays
            // centred on tall screens, but collapses when keyboard is open.
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}