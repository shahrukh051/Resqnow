package com.resqnow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*

val Bg = Color(0xFF1B1C1E); val Panel = Color(0xFF26282B); val Orange = Color(0xFFFFA45C)
val Amber = Color(0xFFFFC233); val Sos = Color(0xFFE53935); val Ink = Color(0xFFF2F2F2)

class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b); enableEdgeToEdge()
        setContent {
            MaterialTheme(darkColorScheme(primary = Orange, background = Bg, surface = Panel)) { App() }
        }
    }
}

@Composable fun App() {
    val nav = rememberNavController(); val vm: TrackViewModel = viewModel()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(containerColor = Bg, bottomBar = {
        if (route != "sos") NavigationBar(containerColor = Panel) {
            listOf("home" to Icons.Default.Home, "track" to Icons.Default.LocationOn, "sos" to Icons.Default.Warning)
                .forEach { (r, i) ->
                    NavigationBarItem(selected = route == r, onClick = { nav.navigate(r) { launchSingleTop = true } },
                        icon = { Icon(i, r) })
                }
        }
    }) { p ->
        NavHost(nav, "home", Modifier.padding(p)) {
            composable("home") { HomeScreen(nav) }
            composable("track") { TrackScreen(vm) }
            composable("sos") { SosScreen(nav, vm) }
        }
    }
}
