package com.example

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val statusText by viewModel.statusText.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val isVideoMode by viewModel.isVideoMode.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val textSize by viewModel.textSize.collectAsStateWithLifecycle()

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreviewView(
                modifier = Modifier.fillMaxSize(),
                onImageCaptureCreated = { capture -> imageCapture = capture },
                onVideoFrameCaptured = { bitmap -> viewModel.processVideoFrame(bitmap) },
                isVideoMode = isVideoMode
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет доступа к камере",
                    color = Color.White,
                    fontSize = 24.sp
                )
            }
        }

        // Overlay UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top 40%
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFE3E2E6))
                    .border(1.dp, Color(0xFFC7C6CA), RoundedCornerShape(32.dp))
                    .clickable(onClick = onNavigateToSettings)
                    .testTag("settings_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF005AC1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "НАСТРОЙКИ",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1F),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "И КЛЮЧ GEMINI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF44474E).copy(alpha = 0.8f)
                    )
                }
            }

            // Bottom 60%
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left - Photo Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFFADC6FF))
                        .clickable(enabled = !isProcessing) {
                            imageCapture?.let { capture ->
                                takePhoto(
                                    imageCapture = capture,
                                    executor = ContextCompat.getMainExecutor(context),
                                    onPhotoCaptured = { bitmap ->
                                        viewModel.processPhoto(bitmap)
                                    },
                                    onError = { /* Handle error */ }
                                )
                            }
                        }
                        .testTag("photo_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF001D4B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Сделать фото",
                                tint = Color(0xFFD7E2FF),
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "СДЕЛАТЬ\nФОТО",
                            color = Color(0xFF001D4B),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp
                        )
                    }
                }

                // Right - Video Button
                val videoBgColor = if (isVideoMode) Color(0xFFBA1A1A) else Color(0xFF005AC1)
                val videoContentColor = if (isVideoMode) Color(0xFFFFDAD6) else Color(0xFFD7E2FF)
                val videoIconBgColor = if (isVideoMode) Color(0xFFFFDAD6) else Color(0xFFD7E2FF)
                val videoIconColor = if (isVideoMode) Color(0xFF410002) else Color(0xFF001D4B)
                val videoText = if (isVideoMode) "СТОП\nВИДЕО" else "ЖИВОЕ\nВИДЕО"
                val videoIcon = if (isVideoMode) Icons.Default.VideocamOff else Icons.Default.Videocam

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(32.dp))
                        .background(videoBgColor)
                        .clickable {
                            viewModel.toggleVideoMode()
                        }
                        .testTag("video_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(videoIconBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = videoIcon,
                                contentDescription = if (isVideoMode) "Остановить видео" else "Запустить видео",
                                tint = videoIconColor,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = videoText,
                            color = videoContentColor,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp
                        )
                    }
                }
            }
        }

        // Floating Status Text Overlay
        if (isProcessing || isSpeaking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1B1B1F).copy(alpha = 0.95f))
                    .clickable(enabled = false) {} // block touches
            ) {
                if (isSpeaking && !isProcessing) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(0.75f)
                                .fillMaxWidth()
                                .padding(32.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = statusText,
                                color = Color.White,
                                fontSize = textSize.sp,
                                lineHeight = (textSize * 1.4).sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Start
                            )
                        }
                        Button(
                            onClick = { viewModel.stopSpeaking() },
                            modifier = Modifier
                                .weight(0.25f)
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Text(
                                text = "ПРОПУСТИТЬ\nОЗВУЧКУ",
                                fontSize = 28.sp,
                                lineHeight = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (isProcessing) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFADC6FF),
                            modifier = Modifier.size(80.dp),
                            strokeWidth = 8.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = statusText,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
