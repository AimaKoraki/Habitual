package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun LoginScreen(
    // CHANGED: Now passes Email and Password for validation, and receives an error message
    onLoginAttempt: (String, String) -> Unit,
    errorMessage: String?,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(HabitualTheme.spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.login_welcome),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.login_email_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(HabitualTheme.radius.input)
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.login_password_hint)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(HabitualTheme.radius.input)
        )

        // NEW: Display error message if login fails
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(HabitualTheme.spacing.sm))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.xxl))

        Button(
            // CHANGED: Trigger login attempt with the typed data
            onClick = { onLoginAttempt(email, password) },
            enabled = email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(HabitualTheme.components.buttonHeight),
            shape = RoundedCornerShape(HabitualTheme.radius.medium)
        ) {
            Text(stringResource(R.string.login_btn_text), style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

        TextButton(onClick = onNavigateToRegister) {
            Text(stringResource(R.string.login_register_prompt))
        }
    }
}