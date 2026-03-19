package com.ablauncher.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import com.ablauncher.data.model.WeatherData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient
) {
    @SuppressLint("MissingPermission")
    suspend fun fetchWeather(unit: String, manualCity: String): WeatherData? = withContext(Dispatchers.IO) {
        try {
            val (lat, lon, cityName) = if (manualCity.isNotBlank()) {
                resolveCity(manualCity) ?: return@withContext null
            } else {
                resolveGps() ?: return@withContext null
            }

            val url = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,apparent_temperature,weathercode,windspeed_10m" +
                "&timezone=auto"

            val request = Request.Builder().url(url).build()
            val body = httpClient.newCall(request).execute().use { it.body?.string() }
                ?: return@withContext null

            val json = JSONObject(body)
            val current = json.getJSONObject("current")

            WeatherData(
                temperature = current.getDouble("temperature_2m"),
                feelsLike = current.getDouble("apparent_temperature"),
                windSpeed = current.getDouble("windspeed_10m"),
                weatherCode = current.getInt("weathercode"),
                locationName = cityName,
                unit = unit
            )
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    private fun resolveGps(): Triple<Double, Double, String>? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = try {
            lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) { null } ?: return null

        val cityName = try {
            @Suppress("DEPRECATION")
            Geocoder(context, Locale.getDefault())
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
                ?.let { it.locality ?: it.adminArea ?: "Unknown" }
                ?: "Unknown"
        } catch (e: Exception) { "Unknown" }

        return Triple(location.latitude, location.longitude, cityName)
    }

    private fun resolveCity(cityName: String): Triple<Double, Double, String>? {
        return try {
            @Suppress("DEPRECATION")
            val results = Geocoder(context, Locale.getDefault()).getFromLocationName(cityName, 1)
            val addr = results?.firstOrNull() ?: return null
            Triple(addr.latitude, addr.longitude, addr.locality ?: cityName)
        } catch (e: Exception) { null }
    }
}
