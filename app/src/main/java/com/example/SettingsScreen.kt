package com.example

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val currentKey by viewModel.apiKey.collectAsStateWithLifecycle()
    val videoInterval by viewModel.videoInterval.collectAsStateWithLifecycle()
    val detailLevel by viewModel.detailLevel.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()
    val textSize by viewModel.textSize.collectAsStateWithLifecycle()
    
    var apiKeyInput by remember { mutableStateOf(currentKey ?: "") }
    var customTextSizeInput by remember { mutableStateOf(if (textSize !in listOf(20, 24, 32, 40)) textSize.toString() else "") }

    val intervalOptions = listOf(
        2 to "Через 2 сек", 
        5 to "Через 5 сек", 
        10 to "Через 10 сек", 
        15 to "Через 15 сек"
    )
    val detailOptions = listOf(
        "brief" to "Не подробно", 
        "medium" to "Средне"
    )
    val speechRateOptions = listOf(
        0.5f to "0.5x (Медленно)",
        1.0f to "1.0x (Нормально)",
        1.5f to "1.5x (Быстро)",
        2.0f to "2.0x (Очень быстро)"
    )
    val textSizeOptions = listOf(
        20 to "Маленький (20)",
        24 to "Средний (24)",
        32 to "Большой (32)",
        40 to "Очень большой (40)"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Настройка нейросети",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Для работы приложения требуется API-ключ Gemini.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Как получить ключ:\n1. Зайдите на сайт Google AI Studio (aistudio.google.com).\n2. Нажмите 'Get API key'.\n3. Создайте новый ключ и скопируйте его сюда.\n\nВнимание: Если у вас не получается зайти на сайт Google AI Studio, или нейросеть недоступна в вашем регионе, вам необходимо использовать VPN.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("Gemini API Ключ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Подробность описания",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            detailOptions.forEach { (value, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.saveDetailLevel(value) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = detailLevel == value,
                        onClick = { viewModel.saveDetailLevel(value) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Интервал описания в видеорежиме",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Отсчет начинается после завершения предыдущего описания.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            intervalOptions.forEach { (value, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.saveVideoInterval(value) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = videoInterval == value,
                        onClick = { viewModel.saveVideoInterval(value) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Скорость голоса",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            speechRateOptions.forEach { (value, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.saveSpeechRate(value) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = speechRate == value,
                        onClick = { viewModel.saveSpeechRate(value) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Размер текста при озвучке",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            textSizeOptions.forEach { (value, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.saveTextSize(value) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = textSize == value,
                        onClick = { viewModel.saveTextSize(value) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, fontSize = 16.sp)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = textSize !in listOf(20, 24, 32, 40),
                    onClick = { 
                        val newSize = customTextSizeInput.toIntOrNull() ?: 50
                        viewModel.saveTextSize(newSize) 
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Свой размер:", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = customTextSizeInput,
                    onValueChange = { 
                        customTextSizeInput = it
                        it.toIntOrNull()?.let { size -> viewModel.saveTextSize(size) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(100.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.saveApiKey(apiKeyInput)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(
                    text = "СОХРАНИТЬ И ВЫЙТИ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
