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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.WorkOrderEntity
import com.example.ui.components.AuthStatusBadge
import com.example.ui.components.LargeActionButton
import com.example.ui.components.RoleBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.SyncStatusBanner
import com.example.ui.components.VehicleSummaryCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkGrayText
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MediumGrayText
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusIssueRed
import com.example.ui.theme.StatusNormalGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.viewmodel.FleetViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    viewModel: FleetViewModel,
    onOpenUserSwitch: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val assignedVehicle by viewModel.assignedVehicle.collectAsState()
    val assignedPair by viewModel.assignedPair.collectAsState()
    val assignedTrailer by viewModel.assignedTrailer.collectAsState()
    val activeAssignment by viewModel.activeAssignment.collectAsState()
    val regularVehicle by viewModel.regularVehicle.collectAsState()
    val regularWorkOrders by viewModel.regularVehicleWorkOrders.collectAsState()
    val todayInspection by viewModel.todayInspection.collectAsState()
    val todayItems by viewModel.todayInspectionItems.collectAsState()
    val pendingAcceptances by viewModel.pendingAcceptanceWorkOrders.collectAsState()
    val todayRelease by viewModel.todayRelease.collectAsState()
    val lastRefreshTime by viewModel.lastRefreshTime.collectAsState()

    var showReturnDialog by remember { mutableStateOf(false) }
    var selectedAcceptanceWorkOrder by remember { mutableStateOf<WorkOrderEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "บริษัททองอยู่ — ผู้ปฏิบัติงาน",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "ระบบคนขับรถบรรทุกน้ำมัน",
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
            // Standalone notice
            item {
                SyncStatusBanner(lastRefreshTime)
            }

            // Driver Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "ผู้ขับขี่ปัจจุบัน",
                                fontSize = 14.sp,
                                color = MediumGrayText
                            )
                            Text(
                                text = currentUser?.name ?: "กำลังโหลด...",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGrayText
                            )
                            Text(
                                text = "วันทำงาน: ${DateUtils.formatThaiDate(DateUtils.getWorkDate())}",
                                fontSize = 14.sp,
                                color = NavyPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (currentUser != null) {
                            RoleBadge(currentUser!!.role)
                        }
                    }
                }
            }

            // Assigned Vehicle Details
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    VehicleSummaryCard(
                        vehicle = assignedVehicle,
                        pair = assignedPair,
                        trailer = assignedTrailer,
                        isSpare = activeAssignment?.isSpareTruck == true
                    )
                }
            }

            // Inspection Status Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "การตรวจก่อนออก",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGrayText,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )

                            if (todayInspection == null) {
                                StatusBadge("ยังไม่ได้เริ่ม", "warning")
                            } else if (todayInspection!!.isComplete && !todayInspection!!.hasIssues && !todayInspection!!.hasPendingMechanicCheck) {
                                StatusBadge("ตรวจครบ ปกติ", "normal")
                            } else if (todayInspection!!.hasIssues || todayInspection!!.hasPendingMechanicCheck) {
                                StatusBadge("พบปัญหา/รอช่าง", "issue")
                            } else {
                                StatusBadge("กำลังตรวจ", "warning")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (todayInspection != null) {
                            val total = todayItems.size
                            val checked = todayItems.count { it.result != null }
                            val issues = todayItems.count { it.result == "ISSUE" }
                            val mechChecks = todayItems.count { it.result == "MECHANIC_CHECK" }

                            Text(
                                text = "ความคืบหน้า: ตรวจแล้ว $checked จาก $total ข้อ",
                                fontSize = 15.sp,
                                color = MediumGrayText
                            )

                            if (issues > 0 || mechChecks > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ข้อผิดปกติ: $issues ข้อ, ให้ช่างช่วยตรวจ: $mechChecks ข้อ",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusIssueRed
                                )
                            }
                        } else {
                            Text(
                                text = "ต้องตรวจรถครั้งแรกของวันก่อนนำรถออกวิ่งรับ–ส่งน้ำมัน",
                                fontSize = 15.sp,
                                color = MediumGrayText
                            )
                        }

                        // Dispatch Release Status
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = if (todayRelease != null) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(
                                    if (todayRelease != null) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = if (todayRelease != null) StatusNormalGreen else MediumGrayText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (todayRelease != null) "พี่ก้อมอนุมัติปล่อยรถแล้ว (${DateUtils.formatThaiTime(todayRelease!!.releasedAt)})" else "สถานะปล่อยรถ: รอพี่ก้อมตรวจสอบและปล่อยรถ",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (todayRelease != null) Color(0xFF14532D) else DarkGrayText
                                    )
                                    if (todayRelease?.isPaperFallback == true) {
                                        Text(
                                            text = "ปล่อยด้วยเอกสารกระดาษเลขที่: ${todayRelease?.paperDocNo ?: "-"}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Big Action Buttons for Driver
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LargeActionButton(
                        text = if (todayInspection == null) "ตรวจก่อนออก (ครั้งแรกของวัน)" else "ดู/ทำต่อ แบบตรวจก่อนออก",
                        onClick = { viewModel.startOrResumeInspection() },
                        icon = Icons.Default.Assignment,
                        isPrimary = true,
                        testTag = "pre_trip_inspection_button"
                    )

                    LargeActionButton(
                        text = "แจ้งรถมีปัญหาระหว่างทาง",
                        onClick = { viewModel.navigateTo(Screen.RoadsideBreakdown) },
                        icon = Icons.Default.ReportProblem,
                        isPrimary = false,
                        testTag = "roadside_breakdown_button"
                    )
                }
            }

            // Pending Acceptance Work Orders (คนขับต้องตรวจรับหลังซ่อม)
            if (pendingAcceptances.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = AmberAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "งานซ่อมเสร็จแล้ว รอคนขับตรวจรับ (${pendingAcceptances.size} รายการ)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "คนขับประจำต้องร่วมตรวจรับจุดซ่อมกับช่างก่อน จึงจะสามารถปล่อยรถออกงานได้",
                                fontSize = 15.sp,
                                color = DarkGrayText
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            pendingAcceptances.forEach { wo ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = wo.repairSummary ?: "ไม่มีรายละเอียด",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "อะไหล่ที่เปลี่ยน: ${wo.partsReplaced ?: "-"}",
                                            fontSize = 14.sp,
                                            color = MediumGrayText
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { selectedAcceptanceWorkOrder = wo },
                                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) {
                                            Text("กดเพื่อตรวจรับงานซ่อมนี้", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Regular truck status when using spare truck
            if (activeAssignment?.isSpareTruck == true && regularVehicle != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "สถานะรถประจำของคุณ (ขณะใช้รถสำรอง)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGrayText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "รถประจำ: ${regularVehicle!!.displayCode} (${regularVehicle!!.plateNumber})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text("สถานะสิทธิ์รถประจำ:", fontSize = 14.sp, color = MediumGrayText)
                                AuthStatusBadge(regularVehicle!!.authStatus)
                            }

                            if (regularWorkOrders.isNotEmpty()) {
                                Text(
                                    text = "งานซ่อมรถประจำที่กำลังดำเนินการ: ${regularWorkOrders.size} รายการ",
                                    fontSize = 14.sp,
                                    color = StatusWarningAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "รถประจำซ่อมเสร็จหรือพร้อมใช้งานแล้ว",
                                    fontSize = 14.sp,
                                    color = StatusNormalGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            LargeActionButton(
                                text = "ตรวจและคืนรถสำรอง",
                                onClick = { showReturnDialog = true },
                                icon = Icons.Default.KeyboardReturn,
                                isPrimary = false,
                                testTag = "return_spare_truck_button"
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Return Spare Truck Dialog
    if (showReturnDialog && activeAssignment != null) {
        ReturnSpareTruckDialog(
            assignment = activeAssignment!!,
            vehicle = assignedVehicle,
            onConfirm = { mileage, mileageBroken, symptoms ->
                viewModel.requestSpareTruckReturn(activeAssignment!!.id, mileage, mileageBroken, symptoms)
                showReturnDialog = false
            },
            onDismiss = { showReturnDialog = false }
        )
    }

    // Driver Acceptance Dialog
    if (selectedAcceptanceWorkOrder != null) {
        DriverAcceptanceDialog(
            workOrder = selectedAcceptanceWorkOrder!!,
            onAccept = { notes ->
                viewModel.recordDriverAcceptance(selectedAcceptanceWorkOrder!!.id, true, notes)
                selectedAcceptanceWorkOrder = null
            },
            onReject = { notes ->
                viewModel.recordDriverAcceptance(selectedAcceptanceWorkOrder!!.id, false, notes)
                selectedAcceptanceWorkOrder = null
            },
            onDismiss = { selectedAcceptanceWorkOrder = null }
        )
    }
}

@Composable
fun DriverAcceptanceDialog(
    workOrder: WorkOrderEntity,
    onAccept: (notes: String) -> Unit,
    onReject: (notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("คนขับตรวจรับงานซ่อมร่วมกับผู้ซ่อม", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "รายการซ่อม: ${workOrder.repairSummary ?: "-"}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "สิ่งที่เปลี่ยน: ${workOrder.partsReplaced ?: "-"}",
                    fontSize = 15.sp,
                    color = MediumGrayText
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("บันทึกผลการตรวจรับ / ข้อสังเกต") },
                    placeholder = { Text("เช่น ทดสอบแล้วใช้งานได้ปกติ หรือ ยังมีอาการสะดุด") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAccept(notes.ifBlank { "คนขับตรวจรับเรียบร้อย สมบูรณ์" }) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusNormalGreen)
            ) {
                Text("ตรวจรับผ่าน", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = { onReject(notes.ifBlank { "ตรวจรับไม่ผ่าน พบอาการเดิม" }) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusIssueRed)
            ) {
                Text("ไม่ผ่าน (ส่งกลับซ่อม)", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ReturnSpareTruckDialog(
    assignment: com.example.data.model.AssignmentEntity,
    vehicle: com.example.data.model.VehicleEntity?,
    onConfirm: (mileage: String?, mileageBroken: Boolean, symptoms: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var mileage by remember { mutableStateOf("") }
    var mileageBroken by remember { mutableStateOf(false) }
    var symptoms by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("ตรวจสภาพและขอคืนรถสำรอง", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "รถสำรอง: ${vehicle?.displayCode ?: "-"} (${vehicle?.plateNumber ?: "-"})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "คนขับต้องตรวจสภาพ บันทึกเลขไมล์ และรายงานอาการก่อนคืน (โต้ง/เหวียน จะเป็นผู้กดยืนยันรับคืน)",
                    fontSize = 14.sp,
                    color = MediumGrayText
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (!mileageBroken) {
                    OutlinedTextField(
                        value = mileage,
                        onValueChange = { mileage = it },
                        label = { Text("เลขไมล์ปัจจุบันของรถสำรอง") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { mileageBroken = !mileageBroken }
                ) {
                    Checkbox(checked = mileageBroken, onCheckedChange = { mileageBroken = it })
                    Text("ไมล์ใช้ไม่ได้ / ชำรุด", fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("อาการที่พบระหว่างใช้งานรถสำรอง (ถ้ามี)") },
                    placeholder = { Text("เช่น ปกติดี หรือ เบรกลมเริ่มดัง") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(mileage, mileageBroken, symptoms) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("ส่งเรื่องขอคืนรถสำรอง", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก")
            }
        }
    )
}
