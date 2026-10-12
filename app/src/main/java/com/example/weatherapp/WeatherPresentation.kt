package com.example.weatherapp

import java.net.URI
import java.util.Locale

internal fun weatherIconUrl(icon: String?): String? {
    val url = icon?.let { if (it.startsWith("//")) "https:$it" else it } ?: return null
    return try {
        val uri = URI(url)
        url.takeIf {
            uri.scheme == "https" && uri.host == "cdn.weatherapi.com" &&
                uri.userInfo == null && (uri.port == -1 || uri.port == 443)
        }
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: java.net.URISyntaxException) {
        null
    }
}

internal fun Double?.weatherValue(unit: String): String =
    if (this != null && isFinite()) String.format(Locale.getDefault(), "%.1f%s", this, unit) else "—"

internal fun Int?.percentage(): String = if (this != null && this in 0..100) "$this%" else "—"

internal fun hourlyTime(time: String?): String =
    time?.substringAfter(' ', time)?.takeIf { it.isNotBlank() } ?: "—"
