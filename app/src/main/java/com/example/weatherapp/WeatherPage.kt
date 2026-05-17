package com.example.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.api.NetworkResponse
import com.example.weatherapp.api.WeatherModel

@Composable
fun WeatherPage(viewModel: WeatherViewModel, modifier: Modifier = Modifier) {
    var city by remember { mutableStateOf("") }
    val weatherResult by viewModel.weatherResult.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = city,
                onValueChange = { city = it },
                label = { Text(text = "Search for any location") }
            )
            IconButton(onClick = {
                viewModel.getData(city)
                keyboardController?.hide()
            }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            }
        }

        when (val result = weatherResult) {
            is NetworkResponse.Error -> {
                Text(text = result.message, color = Color.Red)
            }
            NetworkResponse.Loading -> {
                CircularProgressIndicator()
            }
            is NetworkResponse.Success -> {
                WeatherDetails(data = result.data)
            }
        }
    }
}

@Composable
fun WeatherDetails(data: WeatherModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // NagÅ‚Ã³wek: Data i Lokalizacja
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Today, ${data.location.localtime.split(" ")[0]}", color = Color.Gray, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Text(
                    text = "${data.location.name}, ${data.location.region}, ${data.location.country}",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GÅ‚Ã³wna karta z pogodÄ…
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https:${data.current.condition.icon}".replace("64x64", "128x128"),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                    Text(text = "${data.current.temp_c}°C", fontSize = 60.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Statystyki: Wind, Humidity, Chance of rain
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherStatItem("Wind", "${data.current.wind_kph} km/h", "https://cdn.weatherapi.com/weather/64x64/day/113.png")
                    WeatherStatItem("Humidity", "${data.current.humidity}%", "https://cdn.weatherapi.com/weather/64x64/day/113.png")
                    WeatherStatItem("Chance of rain", "${data.forecast?.forecastday?.get(0)?.day?.daily_chance_of_rain ?: 0}%", "https://cdn.weatherapi.com/weather/64x64/day/113.png")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista godzinowa
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.forecast?.forecastday?.get(0)?.hour?.let { hours ->
                items(hours) { hour ->
                    HourlyItem(hour)
                }
            }
        }
    }
}

@Composable
fun WeatherStatItem(label: String, value: String, icon: String) {
    Card(
        modifier = Modifier.width(100.dp),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(model = icon, contentDescription = null, modifier = Modifier.size(30.dp))
            Text(text = label, fontSize = 10.sp, color = Color.Gray)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HourlyItem(hour: com.example.weatherapp.api.Hour) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = hour.time.split(" ")[1], modifier = Modifier.weight(1f))
            Text(text = "${hour.temp_c}°C", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(text = "${hour.feelslike_c}°C", color = Color.Gray, modifier = Modifier.weight(1f))
            AsyncImage(model = "https:${hour.condition.icon}", contentDescription = null, modifier = Modifier.size(30.dp))
        }
    }
}

