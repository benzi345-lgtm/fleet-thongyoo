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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.AssignmentEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleEntity
import com.example.ui.components.AuthStatusBadge
import com.example.ui.components.LargeActionButton
import com.example.ui.components.StatusBadge
import com.example.ui.components.SyncStatusBanner
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkGrayText
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MediumGrayText
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusNormalGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.viewmodel.FleetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpareTruckScreen(
    viewModel: FleetViewModel,
    onOpenUserSwitch: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allAssignments by viewModel.repository.getAllActiveAssignments().collectAsState(emptyList())
    val allVehicles by viewModel.allVehicles.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val activeWorkOrders by viewModel.activeWorkOrders.collectAsState()
    val lastRefreshTime by viewModel.lastRefreshTime.collectAsState()

    val drivers = allUsers.filter { it.role == UserRole.DRIVER.name }
    val spareVehicles = allVehicles.filter { it.isSpare }

    var showAssignDialog by remember { mutableStateOf(false) }
    var showConfirmReturnDialogFor by remember { mutableStateOf<AssignmentEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "จัดคิวและรถสำรอง (โต้ง / เหวียน)",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "ผู้ใช้งาน: ${currentUser?.name ?: "-"}",
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

            // Quick Stats & Action
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
                                text = "รถสำรองในระบบฟลีท",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGrayText
                            )
                            StatusBadge("${spareVehicles.size} คัน", "info")
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "เมื่อรถประจำเกิดเหตุเสีย โต้ง/เหวียน สามารถสลับมอบหมายรถสำรองให้คนขับออกงานได้ทันที",
                            fontSize = 14.sp,
                            color = MediumGrayText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LargeActionButton(
                            text = "มอบหมายรถสำรองให้คนขับ",
                            onClick = { showAssignDialog = true },
                            icon = Icons.Default.LocalShipping,
                            isPrimary = true,
                            testTag = "assign_spare_button"
                        )
                    }
                }
            }

            // Current Active Spare Truck Assignments
            item {
                Text(
                    text = "รายการมอบหมายรถสำรองที่ใช้งานอยู่",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            val spareAssignments = allAssignments.filter { it.isSpareTruck }
            if (spareAssignments.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "ขณะนี้ไม่มีคนขับที่กำลังใช้รถสำรอง (รถประจำวิ่งปกติครบทุกคัน)",
                            fontSize = 15.sp,
                            color = MediumGrayText,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            items(spareAssignments) { assign ->
                val spareVeh = allVehicles.find { it.id == assign.vehicleId }
                val driver = allUsers.find { it.id == assign.driverId }
                val regularVeh = allVehicles.find { it.id == assign.regularVehicleId }

                Card(
                    shape = RoundedCornerShape(12.dp),
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
                                text = "รถสำรอง: ${spareVeh?.displayCode ?: "-"} (${spareVeh?.plateNumber ?: "-"})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                modifier = Modifier.weight(1f).padding(end = 6.dp)
                            )
                            if (assign.status == "RETURN_PENDING") {
                                StatusBadge("ขอคืนรถแล้ว", "warning")
                            } else {
                                StatusBadge("กำลังใช้งาน", "info")
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "คนขับ: ${driver?.name ?: "-"} (รถประจำเดิม: ${regularVeh?.displayCode ?: "-"} ${regularVeh?.plateNumber ?: "-"})",
                            fontSize = 15.sp,
                            color = DarkGrayText
                        )

                        if (!assign.returnNotes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "หมายเหตุการคืน: ${assign.returnNotes}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showConfirmReturnDialogFor = assign },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusNormalGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Icon(Icons.Default.KeyboardReturn, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("โต้ง/เหวียน ยืนยันรับคืนรถสำรอง", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Vehicles in repair overview
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ภาพรวมรถที่กำลังอยู่ระหว่างซ่อมในอู่ / รออะไหล่",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            items(activeWorkOrders) { wo ->
                val veh = allVehicles.find { it.id == wo.vehicleId }
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp).fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "รถ: ${veh?.displayCode ?: "-"} (${veh?.plateNumber ?: "-"})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = wo.repairSummary ?: "-",
                                fontSize = 14.sp,
                                color = MediumGrayText
                            )
                        }
                        StatusBadge(wo.status, "issue")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Assign Spare Truck Dialog
    if (showAssignDialog) {
        AssignSpareTruckDialog(
            drivers = drivers,
            spareVehicles = spareVehicles,
            onConfirm = { driverId, spareId ->
                viewModel.assignSpareTruck(driverId, spareId)
                showAssignDialog = false
            },
            onDismiss = { showAssignDialog = false }
        )
    }

    // Confirm Return Dialog
    if (showConfirmReturnDialogFor != null) {
        AlertDialog(
            onDismissRequest = { showConfirmReturnDialogFor = null },
            title = {
                Text("ยืนยันรับคืนรถสำรอง", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "โต้ง/เหวียน ตรวจสอบสภาพรถสำรองเรียบร้อย และยืนยันการรับคืนรถสำรองเข้าลาน ระบบจะสลับคนขับกลับไปผูกกับรถประจำตามเดิม",
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmSpareTruckReturn(
                            showConfirmReturnDialogFor!!.id,
                            "ตรวจสอบสภาพเรียบร้อย รถสำรองพร้อมหมุนเวียน"
                        )
                        showConfirmReturnDialogFor = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusNormalGreen)
                ) {
                    Text("ยืนยันรับคืน", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmReturnDialogFor = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
fun AssignSpareTruckDialog(
    drivers: List<UserEntity>,
    spareVehicles: List<VehicleEntity>,
    onConfirm: (driverId: String, spareId: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDriverId by remember { mutableStateOf(drivers.firstOrNull()?.id ?: "") }
    var selectedSpareId by remember { mutableStateOf(spareVehicles.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("มอบหมายรถสำรองให้คนขับ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("เลือกคนขับที่ต้องการเปลี่ยนมาใช้รถสำรอง:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                drivers.forEach { driver ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDriverId = driver.id }
                    ) {
                        RadioButton(
                            selected = selectedDriverId == driver.id,
                            onClick = { selectedDriverId = driver.id }
                        )
                        Text(driver.name, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("เลือกรถสำรอง:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                spareVehicles.forEach { spare ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSpareId = spare.id }
                    ) {
                        RadioButton(
                            selected = selectedSpareId == spare.id,
                            onClick = { selectedSpareId = spare.id }
                        )
                        Text("${spare.displayCode} (${spare.plateNumber})", fontSize = 15.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDriverId, selectedSpareId) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("ยืนยันมอบหมาย", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก")
            }
        }
    )
}
