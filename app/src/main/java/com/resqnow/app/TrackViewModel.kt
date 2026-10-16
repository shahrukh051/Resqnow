package com.resqnow.app

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object RoutesApi {
    const val KEY = "AIzaSyCuK2tdlCHHJGXf-JzvhSXOQf5aFckrFtw"
    class R(val pts: List<LatLng>, val mins: Int)

    private fun loc(p: LatLng) = JSONObject().put("location", JSONObject().put("latLng",
        JSONObject().put("latitude", p.latitude).put("longitude", p.longitude)))

    /** Fastest traffic-aware route out of all alternatives. */
    suspend fun fastest(a: LatLng, b: LatLng): R? = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().put("origin", loc(a)).put("destination", loc(b)).put("travelMode", "DRIVE")
                .put("routingPreference", "TRAFFIC_AWARE_OPTIMAL").put("computeAlternativeRoutes", true).toString()
            val c = URL("https://routes.googleapis.com/directions/v2:computeRoutes").openConnection() as HttpURLConnection
            c.requestMethod = "POST"; c.doOutput = true
            c.setRequestProperty("Content-Type", "application/json")
            c.setRequestProperty("X-Goog-Api-Key", KEY)
            c.setRequestProperty("X-Goog-FieldMask", "routes.duration,routes.polyline.encodedPolyline")
            c.outputStream.use { it.write(body.toByteArray()) }
            val rs = JSONObject(c.inputStream.bufferedReader().readText()).getJSONArray("routes")
            fun secs(o: JSONObject) = o.getString("duration").dropLast(1).toInt()
            (0 until rs.length()).map { rs.getJSONObject(it) }.minByOrNull { secs(it) }?.let {
                R(PolyUtil.decode(it.getJSONObject("polyline").getString("encodedPolyline")), secs(it) / 60)
            }
        } catch (e: Exception) { null }
    }
}

class TrackViewModel : ViewModel() {
    val patient = LatLng(26.9124, 75.7873)   // demo points (Jaipur) - swap for real GPS / backend
    val hospital = LatLng(26.9059, 75.8147)
    var amb by mutableStateOf(LatLng(26.9230, 75.7700))
    var route by mutableStateOf(emptyList<LatLng>())
    var status by mutableStateOf("Choose a type and request")
    var etaMin by mutableIntStateOf(0)
    var sos by mutableStateOf(false)
    private var job: Job? = null

    fun request() {
        job?.cancel()
        job = viewModelScope.launch {
            status = "Ambulance on the way"; drive(patient)
            status = "Patient on board → hospital"; drive(hospital)
            status = "Arrived at hospital ✔"; route = emptyList(); etaMin = 0
        }
    }
    fun sendSos() { sos = true; request() }
    fun cancel() { job?.cancel(); sos = false; route = emptyList(); etaMin = 0; status = "Choose a type and request" }

    private suspend fun drive(to: LatLng) {
        val r = RoutesApi.fastest(amb, to) ?: run { status = "Routes API failed: check key and billing"; return }
        route = r.pts; var tot = r.pts.size; var mins = r.mins; var tick = 0
        while (route.isNotEmpty()) {
            delay(250); amb = route.first(); route = route.drop(2)
            etaMin = mins * route.size / tot.coerceAtLeast(1)
            // every ~15s re-check live traffic; switch if a clearly faster path exists
            if (++tick % 60 == 0) RoutesApi.fastest(amb, to)?.let { n ->
                if (n.mins < etaMin - 1) { status = "⚡ Rerouted: ${etaMin - n.mins} min saved"
                    route = n.pts; tot = n.pts.size; mins = n.mins }
            }
        }
    }
}
