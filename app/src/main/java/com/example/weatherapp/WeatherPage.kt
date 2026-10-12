package com.example.weatherapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.weatherapp.api.Hour
import com.example.weatherapp.api.NetworkResponse
import com.example.weatherapp.api.WeatherModel

@Composable
fun WeatherPage(viewModel: WeatherViewModel, modifier: Modifier = Modifier) {
    var city by rememberSaveable { mutableStateOf("") }
    val weatherResult by viewModel.weatherResult.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val search = {
        if (city.isNotBlank()) {
            viewModel.getData(city)
            keyboardController?.hide()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = city,
                onValueChange = { city = it },
                label = { Text("Search for a location") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { search() })
            )
            IconButton(onClick = search, enabled = city.isNotBlank()) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }
        Spacer(Modifier.height(16.dp))

        when (val result = weatherResult) {
            is NetworkResponse.Error -> {
                Text(result.message, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = viewModel::retry) { Text("Retry") }
            }
            NetworkResponse.Loading -> CircularProgressIndicator()
            is NetworkResponse.Success -> WeatherDetails(result.data, Modifier.weight(1f))
        }
    }
}

@Composable
fun WeatherDetails(data: WeatherModel, modifier: Modifier = Modifier) {
    val current = data.current
    val location = data.location
    val forecast = data.forecast?.days?.firstOrNull { it != null }
    val hours = forecast?.hours.orEmpty().filterNotNull()

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                location?.localtime?.substringBefore(' ')?.takeIf { it.isNotBlank() }?.let {
                    Text("Today, $it", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Text(
                        listOfNotNull(location?.name, location?.region, location?.country)
                            .filter { it.isNotBlank() }.joinToString(", "),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = weatherIconUrl(current?.condition?.icon),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            current?.temperatureC.weatherValue("°C"),
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                    current?.condition?.text?.let { Text(it) }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeatherStatItem("Wind", current?.windKph.weatherValue(" km/h"), Modifier.weight(1f))
                        WeatherStatItem("Humidity", current?.humidity.percentage(), Modifier.weight(1f))
                        WeatherStatItem("Rain chance", forecast?.day?.rainChance.percentage(), Modifier.weight(1f))
                    }
                }
            }
        }
        item { Text("Hourly forecast", style = MaterialTheme.typography.titleMedium) }
        if (hours.isEmpty()) {
            item { Text("Hourly forecast is unavailable for this location.") }
        } else {
            item { Text("Time · temperature · feels like", style = MaterialTheme.typography.bodySmall) }
            items(hours) { hour -> HourlyItem(hour) }
        }
    }
}

@Composable
private fun WeatherStatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun HourlyItem(hour: Hour) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(hourlyTime(hour.time), Modifier.weight(1f))
            Text(hour.temperatureC.weatherValue("°C"), Modifier.weight(1f))
            Text(hour.feelsLikeC.weatherValue("°C"), Modifier.weight(1f))
            AsyncImage(
                model = weatherIconUrl(hour.condition?.icon),
                contentDescription = hour.condition?.text,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}
