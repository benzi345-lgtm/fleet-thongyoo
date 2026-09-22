package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.LargeActionButton
import com.example.ui.components.RoleBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.SyncStatusBanner
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkGrayText
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MediumGrayText
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusNormalGreen
import com.example.ui.viewmodel.FleetViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerOfficeScreen(
    viewModel: FleetViewModel,
    onOpenUserSwitch: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allVehicles by viewModel.allVehicles.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val activeWorkOrders by viewModel.activeWorkOrders.collectAsState()
    val lastRefreshTime by viewModel.lastRefreshTime.collectAsState()

    var showOfficePhoneInDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ฝ่ายบริหารและออฟฟิศ (ทองอยู่)",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "ผู้ใช้งาน: ${currentUser?.name ?: "-"} (${currentUser?.role ?: "-"})",
                            fontSize = 13.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "รีเฟรช", tint = Color.White)
                    }
                    Button(
                        onClick = onOpenUserSwitch,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("เปลี่ยนผู้ใช้", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
            item {
                SyncStatusBanner(lastRefreshTime)
            }

            // Office Call-in Defect Recording Action
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "ออฟฟิศรับสายแทนคนขับ",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGrayText
                                )
                            }
                            RoleBadge(UserRole.OFFICE.name)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "กรณีคนขับโทรเข้าออฟฟิศเพื่อแจ้งรถเสีย หรือช่างโทรแจ้งสิทธิ์รถ ออฟฟิศสามารถบันทึกเข้าระบบแทน โดยระบบจะแยกผู้บันทึก (recordedBy) และผู้แจ้ง (reportedBy) ให้ชัดเจน",
                            fontSize = 14.sp,
                            color = MediumGrayText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LargeActionButton(
                            text = "บันทึกรับแจ้งแทน (ทางโทรศัพท์)",
                            onClick = { showOfficePhoneInDialog = true },
                            icon = Icons.Default.PhoneCallback,
                            isPrimary = true,
                            testTag = "office_phone_in_button"
                        )
                    }
                }
            }

            // Owner Decisions & External Garage Overview
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "อู่ซ่อมภายนอก & ทางไกล (เสี่ยสมศักดิ์)",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            RoleBadge(UserRole.OWNER.name)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "อู่พันธมิตรหลัก: อู่ซ่อมรถบรรทุก สหยนต์ นครปฐม\nโทรศัพท์ประสานงาน: 089-876-5432 (เสี่ยสมศักดิ์)",
                            fontSize = 14.sp,
                            color = DarkGrayText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "งานที่ส่งอู่นอกขณะนี้: ${activeWorkOrders.count { it.status == "SENT_EXTERNAL" }} รายการ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }

            // Audit Trail Link
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = NavyPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "สมุดประวัติการแก้ไขและอนุมัติ (Audit Log)",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGrayText
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "บันทึกทุกการเปลี่ยนสถานะ เวลา ผู้อนุมัติ และเหตุผลอย่างครบถ้วนเพื่อความโปร่งใส",
                            fontSize = 14.sp,
                            color = MediumGrayText
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LargeActionButton(
                            text = "เปิดดู Audit Log ทั้งหมด",
                            onClick = { viewModel.navigateTo(Screen.AuditLogs) },
                            icon = Icons.Default.History,
                            isPrimary = false,
                            testTag = "view_audit_logs_button"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Office Phone-in Dialog
    if (showOfficePhoneInDialog) {
        OfficePhoneInDialog(
            allVehicles = allVehicles,
            drivers = allUsers.filter { it.role == UserRole.DRIVER.name },
            onConfirm = { vehicleId, driverId, symptom, location ->
                viewModel.reportRoadsideBreakdown(
                    symptom = symptom,
                    situation = "STOPPED_SAFE",
                    latitude = null,
                    longitude = null,
                    accuracy = null,
                    locationText = location,
                    photoUri = null,
                    isOfficeCall = true
                ) {}
                showOfficePhoneInDialog = false
            },
            onDismiss = { showOfficePhoneInDialog = false }
        )
    }
}

@Composable
fun OfficePhoneInDialog(
    allVehicles: List<com.example.data.model.VehicleEntity>,
    drivers: List<com.example.data.model.UserEntity>,
    onConfirm: (vehicleId: String, driverId: String, symptom: String, location: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedVehId by remember { mutableStateOf(allVehicles.firstOrNull()?.id ?: "") }
    var selectedDriverId by remember { mutableStateOf(drivers.firstOrNull()?.id ?: "") }
    var symptom by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("ออฟฟิศบันทึกรับแจ้งรถเสียแทนคนขับ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("เลือกรถที่เกิดเหตุ:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                allVehicles.take(4).forEach { veh ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedVehId = veh.id }
                    ) {
                        RadioButton(selected = selectedVehId == veh.id, onClick = { selectedVehId = veh.id })
                        Text("${veh.displayCode} (${veh.plateNumber})", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("เลือกคนขับที่โทรแจ้ง:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                drivers.forEach { driver ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDriverId = driver.id }
                    ) {
                        RadioButton(selected = selectedDriverId == driver.id, onClick = { selectedDriverId = driver.id })
                        Text(driver.name, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = symptom,
                    onValueChange = { symptom = it },
                    label = { Text("อาการตามที่คนขับแจ้งทางสาย *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("สถานที่จุดจอดตามที่แจ้งทางสาย *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (symptom.isBlank() || location.isBlank()) return@Button
                    onConfirm(selectedVehId, selectedDriverId, symptom, location)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("บันทึกเข้าระบบ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก")
            }
        }
    )
}
