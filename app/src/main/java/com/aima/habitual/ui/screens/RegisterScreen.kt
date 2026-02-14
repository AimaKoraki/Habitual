package com.aima.habitual.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun RegisterScreen(
    onRegisterSuccess: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    val isFormValid = termsAccepted && email.isNotBlank() && password.length >= 6 && userName.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HabitualTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Hero Title ──
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))  // 32dp breathing room

        // ── Name Input ──
        TextField(
            value = userName,
            onValueChange = { userName = it },
            label = {
                Text(
                    stringResource(R.string.register_name_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted)
                )
            },
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

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))       // 20dp between inputs

        // ── Email Input ──
        TextField(
            value = email,
            onValueChange = { email = it },
            label = {
                Text(
                    stringResource(R.string.register_email_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HabitualTheme.alpha.muted)
                )
            },
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

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

        // ── Password Input ──
        TextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text(
                    stringResource(R.string.register_password_hint),
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

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

        // ── Terms Checkbox ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { termsAccepted = !termsAccepted },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = HabitualTheme.alpha.muted
                    )
                )
            )
            Text(
                stringResource(R.string.register_terms),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = HabitualTheme.alpha.secondary
                )
            )
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))  // 32dp before CTA

        // ── Primary Button ──
        Button(
            onClick = { onRegisterSuccess(userName, email, password) },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(HabitualTheme.components.buttonHeight),
            shape = RoundedCornerShape(HabitualTheme.radius.xl),           // 20dp
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = HabitualTheme.elevation.low,
                pressedElevation = HabitualTheme.elevation.medium,
                disabledElevation = HabitualTheme.elevation.none
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                    alpha = HabitualTheme.alpha.disabled
                ),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(
                    alpha = HabitualTheme.alpha.muted
                )
            )
        ) {
            Text(
                stringResource(R.string.register_btn_text),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))      // 20dp before helper link

        // ── Helper Link ──
        TextButton(onClick = onNavigateToLogin) {
            Text(
                stringResource(R.string.register_login_prompt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = HabitualTheme.alpha.secondary
                )
            )
        }
    }
}