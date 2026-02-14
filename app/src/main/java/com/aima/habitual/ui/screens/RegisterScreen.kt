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
fun RegisterScreen(
    // CHANGED: Now passes Name, Email, and Password back to MainScreen
    onRegisterSuccess: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(HabitualTheme.spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text(stringResource(R.string.register_name_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(HabitualTheme.radius.input)
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.register_email_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(HabitualTheme.radius.input)
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.register_password_hint)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(HabitualTheme.radius.input)
        )

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = termsAccepted, onCheckedChange = { termsAccepted = it })
            Text(stringResource(R.string.register_terms), style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(HabitualTheme.spacing.section))

        Button(
            // CHANGED: Pass all three fields to save them
            onClick = { onRegisterSuccess(userName, email, password) },
            enabled = termsAccepted && email.isNotBlank() && password.length >= 6 && userName.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(HabitualTheme.components.buttonHeight),
            shape = RoundedCornerShape(HabitualTheme.radius.medium)
        ) {
            Text(stringResource(R.string.register_btn_text))
        }

        TextButton(onClick = onNavigateToLogin) {
            Text(stringResource(R.string.register_login_prompt))
        }
    }
}