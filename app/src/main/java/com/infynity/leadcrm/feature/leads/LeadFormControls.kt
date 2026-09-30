package com.infynity.leadcrm.feature.leads

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun LeadDropdownField(
    label: String,
    selectedValue: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedValue }?.second
        ?: selectedValue.takeIf { it.isNotBlank() }
        ?: "-- None --"
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        androidx.compose.foundation.layout.Box {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) { Text(selectedLabel) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (value, title) ->
                    DropdownMenuItem(
                        text = { Text(title) },
                        onClick = {
                            onSelected(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

data class CustomerLocationData(
    val latitude: String = "",
    val longitude: String = "",
    val accuracy: String = "",
    val capturedAt: String = ""
)

@Composable
fun CustomerLocationFields(
    enabled: Boolean,
    checked: Boolean,
    location: CustomerLocationData,
    onCheckedChange: (Boolean) -> Unit,
    onLocationChange: (CustomerLocationData) -> Unit
) {
    var status by remember { mutableStateOf("") }
    var capturePending by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun captureLocation() {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) {
            status = "Location permission is required to capture this location."
            capturePending = true
            return
        }
        requestCurrentLocation(context, fine, onLocationChange) { status = it }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        capturePending = false
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            captureLocation()
        } else {
            status = "Location permission was denied. Allow access and try again."
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("At Customer Location", modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = { value ->
                    onCheckedChange(value)
                    status = ""
                    if (!value) onLocationChange(CustomerLocationData())
                },
                enabled = enabled
            )
        }
        if (checked) {
            OutlinedButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        capturePending = true
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    } else captureLocation()
                },
                enabled = enabled && !capturePending,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Use My Location") }
            Text(
                status.ifBlank {
                    if (location.latitude.isNotBlank() && location.longitude.isNotBlank()) "Location captured" else "Location not captured"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (status.contains("denied", true) || status.contains("unable", true) || status.contains("unavailable", true) || status.contains("timed out", true)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            LocationReadOnlyValue("Latitude", location.latitude)
            LocationReadOnlyValue("Longitude", location.longitude)
            LocationReadOnlyValue("Accuracy (metres)", location.accuracy)
            LocationReadOnlyValue("Captured At", location.capturedAt)
        }
    }
}

@Composable
private fun LocationReadOnlyValue(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun requestCurrentLocation(
    context: Context,
    finePermission: Boolean,
    onLocation: (CustomerLocationData) -> Unit,
    onStatus: (String) -> Unit
) {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    try {
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { provider ->
                (finePermission || provider != LocationManager.GPS_PROVIDER) &&
                    manager.isProviderEnabled(provider)
            }
        if (providers.isEmpty()) {
            onStatus("Unable to capture location. Turn on location services and try again.")
            return
        }
        onStatus("Requesting your current location…")
        val handler = Handler(Looper.getMainLooper())
        val completed = AtomicBoolean(false)
        val listeners = mutableListOf<LocationListener>()
        val cancellationSignals = mutableListOf<CancellationSignal>()
        fun stopRequests() {
            listeners.forEach(manager::removeUpdates)
            cancellationSignals.forEach(CancellationSignal::cancel)
        }
        val timeout = Runnable {
            if (completed.compareAndSet(false, true)) {
                stopRequests()
                onStatus("Location request timed out. Please try again.")
            }
        }
        fun deliverLocation(location: Location) {
            if (completed.compareAndSet(false, true)) {
                handler.removeCallbacks(timeout)
                stopRequests()
                onLocation(
                    CustomerLocationData(
                        latitude = "%.7f".format(java.util.Locale.US, location.latitude),
                        longitude = "%.7f".format(java.util.Locale.US, location.longitude),
                        accuracy = "%.2f".format(java.util.Locale.US, location.accuracy),
                        capturedAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    )
                )
                onStatus("Location captured. Accuracy: %.1f metres".format(java.util.Locale.US, location.accuracy))
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            providers.forEach { provider ->
                val cancellation = CancellationSignal()
                cancellationSignals += cancellation
                manager.getCurrentLocation(provider, cancellation, { command -> handler.post(command) }) { location ->
                    if (location != null) deliverLocation(location)
                }
            }
        } else {
            providers.forEach { provider ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) = deliverLocation(location)
                    override fun onProviderEnabled(provider: String) = Unit
                    override fun onProviderDisabled(provider: String) = Unit
                    @Suppress("DEPRECATION")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
                }
                listeners += listener
                manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            }
        }
        handler.postDelayed(timeout, 30_000L)
    } catch (_: SecurityException) {
        onStatus("Location permission was denied. Allow access and try again.")
    } catch (_: IllegalArgumentException) {
        onStatus("Current location is unavailable. Please try again.")
    }
}
