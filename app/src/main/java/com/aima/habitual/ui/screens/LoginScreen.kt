package com.aima.habitual.ui.screens

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

@Composable
fun LoginScreen(
    onLoginAttempt: (String, String) -> Unit,
    errorMessage: String?,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isFormValid = email.isNotBlank() && password.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = HabitualTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Hero Title ──
        Text(
            text = stringResource(R.string.login_welcome),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))  // 32dp breathing room

        // ── Email Input ──
        TextField(
            value = email,
            onValueChange = { email = it },
            label = {
                Text(
                    stringResource(R.string.login_email_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(HabitualTheme.radius.xl),           // 20dp corners
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))       // 20dp between inputs

        // ── Password Input ──
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
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(HabitualTheme.radius.xl),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        // ── Error Message ──
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))  // 32dp before CTA

        // ── Primary Button ──
        Button(
            onClick = { onLoginAttempt(email, password) },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(HabitualTheme.components.buttonHeight),            // 56dp
            shape = RoundedCornerShape(HabitualTheme.radius.xl),           // 20dp
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = HabitualTheme.elevation.low,            // Subtle lift
                pressedElevation = HabitualTheme.elevation.medium,
                disabledElevation = HabitualTheme.elevation.none
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                    alpha = HabitualTheme.alpha.disabled                    // Opacity fade, not color shift
                ),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(
                    alpha = HabitualTheme.alpha.muted
                )
            )
        ) {
            Text(
                stringResource(R.string.login_btn_text),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))      // 20dp before helper link

        // ── Helper Link ──
        TextButton(onClick = onNavigateToRegister) {
            Text(
                stringResource(R.string.login_register_prompt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = HabitualTheme.alpha.secondary                  // Muted secondary text
                )
            )
        }
    }
}