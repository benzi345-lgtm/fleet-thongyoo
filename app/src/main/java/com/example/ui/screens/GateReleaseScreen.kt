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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.DispatchReleaseEntity
import com.example.data.model.UserEntity
import com.example.data.model.VehicleEntity
import com.example.data.model.VehiclePairEntity
import com.example.data.repository.ReleaseEligibility
import com.example.ui.components.LargeActionButton
import com.example.ui.components.StatusBadge
import com.example.ui.components.SyncStatusBanner
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkGrayText
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MediumGrayText
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusIssueRed
import com.example.ui.theme.StatusNormalGreen
import com.example.ui.viewmodel.FleetViewModel
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GateReleaseScreen(
    viewModel: FleetViewModel,
    onOpenUserSwitch: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allAssignments: List<AssignmentEntity> by viewModel.repository.getAllActiveAssignments().collectAsState(emptyList())
    val allVehicles by viewModel.allVehicles.collectAsState()
    val allPairs: List<VehiclePairEntity> by viewModel.repository.getAllActivePairs().collectAsState(emptyList())
    val allUsers by viewModel.allUsers.collectAsState()
    val allReleasesToday: List<DispatchReleaseEntity> by viewModel.repository.getAllReleasesToday(DateUtils.getWorkDate()).collectAsState(emptyList())
    val lastRefreshTime by viewModel.lastRefreshTime.collectAsState()

    var showPaperFallbackFor by remember { mutableStateOf<AssignmentEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ตรวจสอบและปล่อยรถ (พี่ก้อม)",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "ผู้ตรวจสอบ: ${currentUser?.name ?: "พี่ก้อม"}",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SyncStatusBanner(lastRefreshTime)
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "เกณฑ์การปล่อยรถและจ่ายเบี้ยเลี้ยง (ด่านพี่ก้อม)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. ต้องตรวจก่อนออกครบทุกข้อ (ไม่มีค้าง)\n2. ต้องไม่มีรายการ 'ให้ช่างช่วยตรวจ' ค้าง\n3. รถแม่และหางต้องไม่ติด 'ห้ามใช้งาน' หรือ 'เฉพาะไปอู่'\n4. ไม่มีใบงานซ่อมค้างที่ยังไม่เสร็จ",
                            fontSize = 14.sp,
                            color = DarkGrayText
                        )
                    }
                }
            }

            items(allAssignments) { assign ->
                val vehicle = allVehicles.find { it.id == assign.vehicleId }
                val pair = allPairs.find { it.id == assign.pairId }
                val trailer = if (pair != null) allVehicles.find { it.id == pair.trailerVehicleId } else null
                val driver = allUsers.find { it.id == assign.driverId }
                val release = allReleasesToday.find { it.vehicleId == assign.vehicleId }

                GateVehicleCheckCard(
                    assignment = assign,
                    vehicle = vehicle,
                    pair = pair,
                    trailer = trailer,
                    driver = driver,
                    release = release,
                    viewModel = viewModel,
                    onOpenPaperFallback = { showPaperFallbackFor = assign }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showPaperFallbackFor != null) {
        PaperFallbackReleaseDialog(
            assignment = showPaperFallbackFor!!,
            vehicle = allVehicles.find { it.id == showPaperFallbackFor!!.vehicleId },
            driver = allUsers.find { it.id == showPaperFallbackFor!!.driverId },
            onConfirm = { docNo, authorizer, notes ->
                viewModel.confirmPaperFallbackRelease(
                    vehicleId = showPaperFallbackFor!!.vehicleId,
                    pairId = showPaperFallbackFor!!.pairId,
                    driverId = showPaperFallbackFor!!.driverId,
                    paperDocNo = docNo,
                    paperAuthorizer = authorizer,
                    notes = notes
                )
                showPaperFallbackFor = null
            },
            onDismiss = { showPaperFallbackFor = null }
        )
    }
}

@Composable
fun GateVehicleCheckCard(
    assignment: AssignmentEntity,
    vehicle: VehicleEntity?,
    pair: VehiclePairEntity?,
    trailer: VehicleEntity?,
    driver: UserEntity?,
    release: DispatchReleaseEntity?,
    viewModel: FleetViewModel,
    onOpenPaperFallback: () -> Unit
) {
    var eligibility by remember { mutableStateOf<ReleaseEligibility?>(null) }

    LaunchedEffect(assignment.vehicleId, assignment.pairId, release) {
        eligibility = viewModel.checkGateRelease(assignment.vehicleId, assignment.pairId)
    }

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
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "รถ: ${vehicle?.displayCode ?: "-"} (${vehicle?.plateNumber ?: "-"})",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "คนขับ: ${driver?.name ?: "-"}",
                        fontSize = 14.sp,
                        color = DarkGrayText
                    )
                }

                if (release != null) {
                    StatusBadge("ปล่อยรถแล้ว", "normal")
                } else if (eligibility?.isEligible == true) {
                    StatusBadge("พร้อมปล่อย", "info")
                } else {
                    StatusBadge("ยังปล่อยไม่ได้", "issue")
                }
            }

            if (pair != null && trailer != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "หางน้ำมัน: ${trailer.plateNumber} (รหัส ${trailer.displayCode})",
                    fontSize = 14.sp,
                    color = MediumGrayText
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Check details
            if (release != null) {
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "ปล่อยรถแล้วเวลา: ${DateUtils.formatThaiTime(release.releasedAt)} โดย: ${release.releasedByName}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                        if (release.isPaperFallback) {
                            Text(
                                text = "ปล่อยด้วยกระดาษเลขที่: ${release.paperDocNo ?: "-"} (ผู้อนุญาต: ${release.paperAuthorizedBy ?: "-"})",
                                fontSize = 13.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }
            } else if (eligibility != null) {
                if (eligibility!!.isEligible) {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusNormalGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ผ่านเกณฑ์ครบถ้วน: ตรวจก่อนออกครบ, ไม่มีคำสั่งห้ามใช้งาน, ไม่มีใบงานค้าง",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.confirmGateRelease(assignment.vehicleId, assignment.pairId, assignment.driverId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusNormalGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ยืนยันปล่อยรถและจ่ายเบี้ยเลี้ยง", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Show exact blocking reasons
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = StatusIssueRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ไม่สามารถปล่อยรถได้เนื่องจาก:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusIssueRed
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            eligibility!!.blockingReasons.forEach { reason ->
                                Text(
                                    text = "• $reason",
                                    fontSize = 13.sp,
                                    color = Color(0xFF991B1B),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Fallback paper release option
            if (release == null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onOpenPaperFallback,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("บันทึกปล่อยรถด้วยเอกสารกระดาษ (กรณีระบบขัดข้อง)", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun PaperFallbackReleaseDialog(
    assignment: AssignmentEntity,
    vehicle: VehicleEntity?,
    driver: UserEntity?,
    onConfirm: (String, String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var docNo by remember { mutableStateOf("") }
    var authorizer by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("บันทึกปล่อยรถด้วยเอกสารกระดาษย้อนหลัง", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "รถ: ${vehicle?.displayCode ?: "-"} (${vehicle?.plateNumber ?: "-"}) คนขับ: ${driver?.name ?: "-"}",
                    fontSize = 14.sp,
                    color = MediumGrayText
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = docNo,
                    onValueChange = { docNo = it },
                    label = { Text("เลขที่เอกสารปล่อยรถกระดาษ *") },
                    placeholder = { Text("เช่น DOC-20260921-001") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = authorizer,
                    onValueChange = { authorizer = it },
                    label = { Text("ชื่อผู้อนุมัติปล่อยรถตามเอกสารกระดาษ *") },
                    placeholder = { Text("เช่น พี่ก้อม หรือ เสี่ยสมศักดิ์") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("หมายเหตุ / สาเหตุที่ใช้กระดาษ") },
                    placeholder = { Text("เช่น เน็ตชั่วคราวขัดข้อง หรือ ปล่อยรถเร่งด่วนช่วงเช้าตรู่") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (docNo.isBlank() || authorizer.isBlank()) return@Button
                    onConfirm(docNo, authorizer, notes.ifBlank { null })
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
