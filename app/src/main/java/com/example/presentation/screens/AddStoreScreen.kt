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

enum class AddStoreMode { SINGLE, MULTIPLE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoreScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Select & Input, 2: Success
    var mode by remember { mutableStateOf(AddStoreMode.SINGLE) }
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
            when (step) {
                1 -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Option Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardSurface)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (mode == AddStoreMode.SINGLE) PrimaryBlue else Color.Transparent)
                                .clickable { mode = AddStoreMode.SINGLE }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Thêm 1 Store", color = if (mode == AddStoreMode.SINGLE) DarkBackground else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (mode == AddStoreMode.MULTIPLE) PrimaryBlue else Color.Transparent)
                                .clickable { mode = AddStoreMode.MULTIPLE }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Thêm bằng JSON", color = if (mode == AddStoreMode.MULTIPLE) DarkBackground else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (mode == AddStoreMode.SINGLE) {
                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("Tên Store (ví dụ: VN Store)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = jsonText,
                            onValueChange = { jsonText = it },
                            label = { Text("Nội dung Service Account Key (JSON)") },
                            placeholder = { Text("Dán nội dung file service_account.json của bạn vào đây...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                    } else {
                        OutlinedTextField(
                            value = jsonText,
                            onValueChange = { jsonText = it },
                            label = { Text("Nội dung Catalog JSON") },
                            placeholder = { Text("{\n  \"stores\": [\n    {\n      \"storeId\": \"store_vn\",\n      \"storeName\": \"Store Việt Nam\",\n      \"serviceAccountKey\": { ... }\n    }\n  ]\n}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage!!, color = StatusRejected, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (jsonText.isBlank()) {
                                errorMessage = "Vui lòng nhập nội dung JSON."
                                return@Button
                            }
                            if (mode == AddStoreMode.SINGLE && storeName.isBlank()) {
                                errorMessage = "Vui lòng nhập tên Store."
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null

                            if (mode == AddStoreMode.MULTIPLE) {
                                viewModel.importCatalog(jsonText) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        successMessage = msg
                                        step = 2
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            } else {
                                // SINGLE mode
                                if (jsonText.contains("service_account")) {
                                    viewModel.verifyAndAddStoreCredential(storeId, storeName, jsonText) { success, msg ->
                                        isLoading = false
                                        if (success) {
                                            successMessage = msg
                                            step = 2
                                        } else {
                                            errorMessage = msg
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    errorMessage = "Nội dung không phải là file Service Account JSON hợp lệ."
                                }
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
                            Text("Xác thực & Lưu", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                2 -> {
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
