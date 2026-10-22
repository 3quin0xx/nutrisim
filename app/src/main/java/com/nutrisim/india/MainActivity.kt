package com.nutrisim.india

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutrisim.india.ui.screens.*
import com.nutrisim.india.ui.theme.*
import com.nutrisim.india.viewmodel.NutriViewModel

private data class NavItem(val label: String, val icon: ImageVector, val route: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutriSimTheme {
                NutriSimApp()
            }
        }
    }
}

@Composable
private fun NutriSimApp() {
    val viewModel: NutriViewModel = viewModel()
    var currentRoute by remember { mutableStateOf("dashboard") }

    val navItems = listOf(
        NavItem("Dashboard", Icons.Default.Dashboard,      "dashboard"),
        NavItem("Builder",   Icons.Default.AddCircle,      "builder"),
        NavItem("Budget",    Icons.Default.AccountBalance, "budget"),
        NavItem("Settings",  Icons.Default.Settings,       "settings")
    )

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick  = { currentRoute = item.route },
                        icon     = { Icon(item.icon, contentDescription = item.label) },
                        label    = { Text(item.label) },
                        colors   = NavigationBarItemDefaults.colors(
                            selectedIconColor   = Saffron,
                            selectedTextColor   = Saffron,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary,
                            indicatorColor      = Saffron.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundDark)
        ) {
            when (currentRoute) {
                "dashboard" -> DashboardScreen(viewModel)
                "builder"   -> MealBuilderScreen(viewModel)
                "budget"    -> BudgetScreen(viewModel)
                "settings"  -> SettingsScreen(viewModel)
            }
        }
    }
}
