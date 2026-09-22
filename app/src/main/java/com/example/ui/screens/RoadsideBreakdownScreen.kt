package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.LargeActionButton
import com.example.ui.theme.DarkGrayText
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MediumGrayText
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusIssueRed
import com.example.ui.theme.StatusNormalGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.viewmodel.FleetViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadsideBreakdownScreen(viewModel: FleetViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val assignedVehicle by viewModel.assignedVehicle.collectAsState()
    val assignedPair by viewModel.assignedPair.collectAsState()

    var symptom by remember { mutableStateOf("") }
    var situation by remember { mutableStateOf("STOPPED_SAFE") } // STOPPED_SAFE vs MOVING_OBSERVED
    var locationText by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var locationAccuracy by remember { mutableStateOf<Float?>(null) }
    var gpsStatusMessage by remember { mutableStateOf<String?>(null) }
    var photoUriString by remember { mutableStateOf<String?>(null) }

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUriString = uri.toString()
        }
    }

    // GPS Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            // Simulated accurate GPS fix on Android phone
            latitude = 13.7563
            longitude = 100.5018
            locationAccuracy = 12.5f
            gpsStatusMessage = "รับพิกัด GPS สำเร็จ: 13.7563, 100.5018 (ความแม่นยำ 12.5 ม.)"
            if (locationText.isBlank()) {
                locationText = "ถนนเพชรเกษม กม.42 ขาออกนครปฐม"
            }
        } else {
            gpsStatusMessage = "ไม่ได้รับสิทธิ์ GPS: สามารถพิมพ์ระบุสถานที่ได้โดยตรง"
        }
    }

    fun dialPhone(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "แจ้งรถมีปัญหาระหว่างทาง",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.DriverHome) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "กลับ",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC)),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Truck and driver banner
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ข้อมูลรถและผู้แจ้ง",
                            fontSize = 15.sp,
                            color = MediumGrayText
                        )
                        Text(
                            text = "รถ: ${assignedVehicle?.displayCode ?: "-"} (${assignedVehicle?.plateNumber ?: "-"})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGrayText
                        )
                        if (assignedPair != null) {
                            Text(
                                text = "ชุดแม่–ลูก: ${assignedPair?.pairCode ?: "-"}",
                                fontSize = 14.sp,
                                color = NavyPrimary
                            )
                        }
                        Text(
                            text = "ผู้แจ้ง: ${currentUser?.name ?: "-"} (${currentUser?.phone ?: "-"})",
                            fontSize = 15.sp,
                            color = DarkGrayText
                        )
                    }
                }
            }

            // Situation Selector
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "สถานการณ์ของรถในปัจจุบัน *",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGrayText
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { situation = "STOPPED_SAFE" }
                        ) {
                            RadioButton(
                                selected = situation == "STOPPED_SAFE",
                                onClick = { situation = "STOPPED_SAFE" }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "รถหยุดจอดในจุดปลอดภัยแล้ว (เช่น ไหล่ทาง/ปั๊ม)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkGrayText
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { situation = "MOVING_OBSERVED" }
                        ) {
                            RadioButton(
                                selected = situation == "MOVING_OBSERVED",
                                onClick = { situation = "MOVING_OBSERVED" }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ยังเดินทางได้แต่พบอาการผิดปกติ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkGrayText
                            )
                        }

                        if (situation == "MOVING_OBSERVED") {
                            Surface(
                                color = Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusIssueRed)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "คำเตือนสำคัญ: การเลือก 'ยังเดินทางได้แต่พบอาการ' ไม่ถือเป็นการอนุญาตให้ขับต่อ หากอาการกระทบความปลอดภัยให้จอดในจุดปลอดภัยทันที",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusIssueRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Symptom Description
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "รายละเอียดอาการผิดปกติ *",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGrayText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = symptom,
                            onValueChange = { symptom = it },
                            placeholder = { Text("เช่น เกจ์ลมตกเหลือ 5 บาร์ มีเสียงลมรั่วใต้หัวลาก หรือ ยางล้อหลังขวาแตก") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("symptom_input")
                        )
                    }
                }
            }

            // Location
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "สถานที่เกิดเหตุ *",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGrayText
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("กดเพื่อดึงพิกัด GPS จากมือถือ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        if (gpsStatusMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = gpsStatusMessage!!,
                                fontSize = 13.sp,
                                color = if (latitude != null) StatusNormalGreen else StatusWarningAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = locationText,
                            onValueChange = { locationText = it },
                            label = { Text("พิมพ์ระบุสถานที่ / จุดสังเกต (ส่งเรื่องได้แม้ไม่มี GPS)") },
                            placeholder = { Text("เช่น ถนนมิตรภาพ กม.130 ใกล้ปั๊ม ปตท. สีคิ้ว") },
                            modifier = Modifier.fillMaxWidth().testTag("location_text_input")
                        )
                    }
                }
            }

            // Photo Attachment
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "แนบรูปถ่ายความเสียหาย (ถ้ามี)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGrayText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ไม่บังคับรูปถ่ายจนขัดขวางการแจ้งเหตุเร่งด่วน",
                            fontSize = 13.sp,
                            color = MediumGrayText
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (photoUriString != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = photoUriString,
                                    contentDescription = "รูปที่เลือก",
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { photoUriString = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.White, RoundedCornerShape(20.dp))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "ลบรูป", tint = StatusIssueRed)
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ถ่ายภาพหรือเลือกรูปจากเครื่อง", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Emergency Telephone Calls
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ปุ่มโทรติดต่อด่วน (กรณีเหตุฉุกเฉิน)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "คำชี้แจง: ปุ่มโทรเปิดโทรศัพท์จริง ไม่เท่ากับการส่งเรื่องในระบบ ต้องกดส่งเรื่องด้านล่างด้วย",
                            fontSize = 12.sp,
                            color = Color(0xFFB45309)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { dialPhone("0812345678") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("โทรช่างเต็ม", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }

                            Button(
                                onClick = { dialPhone("0898765432") },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("โทรเจ้าของ", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // Submit Button
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    LargeActionButton(
                        text = "ส่งเรื่องแจ้งเสียเข้าระบบ",
                        onClick = {
                            if (symptom.isBlank()) {
                                viewModel.showMessage("กรุณาระบุอาการผิดปกติ")
                                return@LargeActionButton
                            }
                            if (locationText.isBlank()) {
                                viewModel.showMessage("กรุณาระบุสถานที่หรือดึงพิกัด GPS")
                                return@LargeActionButton
                            }
                            viewModel.reportRoadsideBreakdown(
                                symptom = symptom,
                                situation = situation,
                                latitude = latitude,
                                longitude = longitude,
                                accuracy = locationAccuracy,
                                locationText = locationText,
                                photoUri = photoUriString
                            ) {}
                        },
                        isPrimary = true,
                        testTag = "submit_breakdown_button"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
