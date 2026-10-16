package com.resqnow.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*

@Composable fun Tile(e: String, t: String, m: Modifier, go: () -> Unit) {
    Column(m.height(104.dp).clip(RoundedCornerShape(22.dp)).background(Bg).clickable(onClick = go).padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween) {
        Text(e, fontSize = 34.sp); Text(t, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
    }
}

@Composable fun HomeScreen(nav: NavController) {
    Column(Modifier.fillMaxSize().background(Bg)) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
            .background(Orange).statusBarsPadding().padding(18.dp)) {
            Text("ResQNow 🚑\nHelp, on the way.", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(12.dp))
            listOf(listOf("🚑" to "Basic", "❤️" to "ICU"), listOf("🚁" to "Air", "🩺" to "First aid")).forEach { r ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    r.forEach { (e, t) -> Tile(e, t, Modifier.weight(1f)) { nav.navigate("track") } }
                }
            }
        }
        Row(Modifier.padding(16.dp).fillMaxWidth().clip(RoundedCornerShape(50)).background(Panel).padding(18.dp)) {
            Text("🔍  Nearest hospital?", color = Ink)
        }
        listOf("SMS Hospital" to "Jaipur", "Fortis Escorts" to "Malviya Nagar").forEach { (a, b) ->
            Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text(a, color = Ink, fontWeight = FontWeight.Medium); Text(b, color = Color.Gray, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = { nav.navigate("sos") }, Modifier.fillMaxWidth().padding(16.dp).height(60.dp),
            colors = ButtonDefaults.buttonColors(Sos, Color.White), shape = RoundedCornerShape(18.dp)) {
            Text("🆘  SOS EMERGENCY", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
    }
}

@Composable fun SosScreen(nav: NavController, vm: TrackViewModel) {
    val t = rememberInfiniteTransition(label = "p")
    val s by t.animateFloat(1f, 1.12f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "s")
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFC62828), Color(0xFFFF7B72))))
        .statusBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (!vm.sos) {
            Spacer(Modifier.height(40.dp))
            Text("Are you in an emergency?", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("Press the button below, help will reach you soon.", color = Color.White.copy(.8f), textAlign = TextAlign.Center)
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(260.dp).scale(s).clip(CircleShape).background(Color.White.copy(.2f)), Alignment.Center) {
                Box(Modifier.size(170.dp).clip(CircleShape).background(Color.White).clickable { vm.sendSos() }, Alignment.Center) {
                    Text("SOS", color = Sos, fontSize = 44.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.weight(1f))
        } else {
            Spacer(Modifier.weight(1f))
            Text("🚑", fontSize = 90.sp)
            Text("Ambulance is on the way", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("${vm.status} · ${vm.etaMin} min", color = Color.White.copy(.85f), textAlign = TextAlign.Center)
            Spacer(Modifier.weight(1f))
            Button(onClick = { nav.navigate("track") }, Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(Color.White, Sos)) { Text("See ambulance on map", fontWeight = FontWeight.Bold) }
            TextButton(onClick = { vm.cancel() }) { Text("Cancel request", color = Color.White) }
        }
    }
}

private const val DARK = """[{"elementType":"geometry","stylers":[{"color":"#1d1f23"}]},{"elementType":"labels.text.fill","stylers":[{"color":"#8a8f98"}]},{"elementType":"labels.text.stroke","stylers":[{"color":"#1d1f23"}]},{"featureType":"road","elementType":"geometry","stylers":[{"color":"#2c2f36"}]},{"featureType":"water","elementType":"geometry","stylers":[{"color":"#0e1116"}]}]"""

@Composable fun TrackScreen(vm: TrackViewModel) {
    val cam = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(vm.patient, 13f) }
    var sel by remember { mutableIntStateOf(0) }
    LaunchedEffect(vm.amb) { if (vm.route.isNotEmpty()) cam.move(CameraUpdateFactory.newLatLng(vm.amb)) }
    Box(Modifier.fillMaxSize()) {
        GoogleMap(Modifier.fillMaxSize(), cameraPositionState = cam,
            properties = MapProperties(mapStyleOptions = MapStyleOptions(DARK)),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)) {
            if (vm.route.isNotEmpty()) Polyline(listOf(vm.amb) + vm.route, color = Amber, width = 12f)
            Marker(rememberUpdatedMarkerState(vm.patient), title = "Patient",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            Marker(rememberUpdatedMarkerState(vm.hospital), title = "Hospital",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            Marker(rememberUpdatedMarkerState(vm.amb), title = "Ambulance",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        }
        Surface(Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(12.dp), RoundedCornerShape(50), Panel.copy(.94f)) {
            Text(vm.status + if (vm.etaMin > 0) " · ${vm.etaMin} min" else "", Modifier.padding(horizontal = 18.dp, vertical = 10.dp), color = Ink)
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Bg)))) {
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("🚑" to "Basic", "❤️" to "ICU", "👶" to "Neonatal").forEachIndexed { i, (e, n) ->
                    Column(Modifier.width(110.dp).clip(RoundedCornerShape(18.dp)).background(if (sel == i) Amber else Panel)
                        .clickable { sel = i }.padding(14.dp)) {
                        Text(e, fontSize = 34.sp); Text(n, fontWeight = FontWeight.Bold, color = if (sel == i) Color.Black else Ink)
                    }
                }
            }
            Button(onClick = { vm.request() }, Modifier.fillMaxWidth().padding(12.dp).height(54.dp),
                colors = ButtonDefaults.buttonColors(Amber, Color.Black), shape = RoundedCornerShape(14.dp)) {
                Text("REQUEST NOW", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
        }
    }
}
