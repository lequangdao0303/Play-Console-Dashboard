package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoreScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: JSON Key, 2: Paste Text, 3: Verify & Complete
    var jsonText by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var storeId by remember { mutableStateOf("store_${System.currentTimeMillis() / 1000}") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm Store", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stepper indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepBadge(stepNumber = 1, label = "JSON Key", active = step == 1)
                Divider(modifier = Modifier.width(32.dp), color = CardBorder)
                StepBadge(stepNumber = 2, label = "Xác thực", active = step == 2)
                Divider(modifier = Modifier.width(32.dp), color = CardBorder)
                StepBadge(stepNumber = 3, label = "Hoàn tất", active = step == 3)
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (step) {
                1 -> {
                    // Upload File Zone + Paste Link
                    Card(
                
        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Kéo thả file vào đây hoặc", color = TextSecondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { step = 2 },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        
        shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Chọn file JSON", color = DarkBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Link mandated in section 2.4
                    Text(
                        text = "Không có file? Dán nội dung JSON thủ công",
                        color = PrimaryBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { step = 2 }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Hướng dẫn định dạng JSON:", color = PrimaryBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Single Store: Dán trực tiếp nội dung file Service Account JSON (Google Cloud).", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Multiple Stores (Catalog): Dùng định dạng sau:", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "{\n  \"stores\": [\n    {\n      \"storeId\": \"store_vn\",\n      \"storeName\": \"VN Store\",\n      \"serviceAccountKey\": { /* Dán service account json */ }\n    }\n  ]\n}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.background(DarkBackground, RoundedCornerShape(8.dp)).padding(8.dp).fillMaxWidth()
                            )
                        }
                    }
                }

                2 -> {
                    // Paste JSON Text Area
                    Text("Nhập thông tin Store & JSON Key", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Tên Store (ví dụ: VN Store)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = jsonText,
                        onValueChange = { jsonText = it },
                        label = { Text("Nội dung Service Account JSON") },
                        placeholder = { Text("Dán nội dung Service Account JSON hoặc Catalog JSON tại đây...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage!!, color = StatusRejected, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (jsonText.isBlank()) {
                                errorMessage = "Vui lòng nhập nội dung JSON."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null

                            if (jsonText.contains("service_account")) {
                                viewModel.verifyAndAddStoreCredential(storeId, storeName, jsonText) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        successMessage = msg
                                        step = 3
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            } else {
                                // Import Store Catalog
                                viewModel.importCatalog(jsonText)
                                isLoading = false
                                successMessage = "Đã nhập Store Catalog thành công!"
                                step = 3
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                
        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Xác thực & Lưu Store", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                3 -> {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusLive, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Thêm Store thành công!", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(successMessage ?: "Store đã sẵn sàng đồng bộ Google API.", color = TextSecondary, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                onSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    
        shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Hoàn tất & Quay lại", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepBadge(stepNumber: Int, label: String, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (active) PrimaryBlue else CardSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(stepNumber.toString(), color = if (active) DarkBackground else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = if (active) PrimaryBlue else TextSecondary, fontSize = 12.sp)
    }
}
