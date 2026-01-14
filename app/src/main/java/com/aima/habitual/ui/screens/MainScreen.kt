package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aima.habitual.navigation.SetupNavGraph
// Import your NavigationBar component here once created

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            // We will place your BottomNavigationBar here in the next step
        }
    ) { innerPadding ->
        SetupNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}