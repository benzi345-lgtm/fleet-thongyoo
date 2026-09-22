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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rule
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.VehicleAuthStatus
import com.example.data.model.WorkOrderEntity
import com.example.data.model.WorkOrderStatus
import com.example.ui.components.AuthStatusBadge
import com.example.ui.components.RoleBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.SyncStatusBanner
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkGrayText
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MediumGrayText
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusIssueRed
import com.example.ui.theme.StatusNormalGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.viewmodel.FleetViewModel
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicScreen(
    viewModel: FleetViewModel,
    onOpenUserSwitch: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allWorkOrders by viewModel.allWorkOrders.collectAsState()
    val allVehicles by viewModel.allVehicles.collectAsState()
    val lastRefreshTime by viewModel.lastRefreshTime.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("รอตรวจ/ประเมิน", "กำลังซ่อม/รออะไหล่", "รอคนขับตรวจรับ/รอปล่อย", "เสร็จสิ้น")

    var selectedOrderForEvaluation by remember { mutableStateOf<WorkOrderEntity?>(null) }
    var selectedOrderForRepairRecord by remember { mutableStateOf<WorkOrderEntity?>(null) }
    var selectedOrderForFinalRelease by remember { mutableStateOf<WorkOrderEntity?>(null) }

    val filteredOrders = remember(allWorkOrders, selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> allWorkOrders.filter {
                it.status == WorkOrderStatus.WAITING_INSPECTION.name ||
                it.status == WorkOrderStatus.INSPECTED_PENDING.name
            }
            1 -> allWorkOrders.filter {
                it.status == WorkOrderStatus.IN_REPAIR.name ||
                it.status == WorkOrderStatus.WAITING_PARTS.name ||
                it.status == WorkOrderStatus.SENT_EXTERNAL.name
            }
            2 -> allWorkOrders.filter {
                it.status == WorkOrderStatus.REPAIRED_WAITING_DRIVER.name ||
                it.status == WorkOrderStatus.DRIVER_ACCEPTED_WAITING_MECHANIC.name
            }
            else -> allWorkOrders.filter { it.status == WorkOrderStatus.COMPLETED.name }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ช่างและงานซ่อมบำรุง",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            SyncStatusBanner(lastRefreshTime)

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = NavyPrimary
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredOrders.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusNormalGreen, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("ไม่มีรายการในหมวดนี้", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGrayText)
                            }
                        }
                    }
                }

                items(filteredOrders) { wo ->
                    val vehicle = allVehicles.find { it.id == wo.vehicleId }

                    WorkOrderCard(
                        workOrder = wo,
                        vehicle = vehicle,
                        onEvaluate = { selectedOrderForEvaluation = wo },
                        onRecordRepair = { selectedOrderForRepairRecord = wo },
                        onFinalRelease = { selectedOrderForFinalRelease = wo }
                    )
                }
            }
        }
    }

    // Evaluation Dialog
    if (selectedOrderForEvaluation != null) {
        MechanicEvaluationDialog(
            workOrder = selectedOrderForEvaluation!!,
            onConfirm = { authStatus, conditions, nextStatus, reason ->
                viewModel.evaluateWorkOrder(
                    workOrderId = selectedOrderForEvaluation!!.id,
                    authStatus = authStatus,
                    conditions = conditions,
                    nextStatus = nextStatus,
                    reason = reason
                )
                selectedOrderForEvaluation = null
            },
            onDismiss = { selectedOrderForEvaluation = null }
        )
    }

    // Repair Record Dialog
    if (selectedOrderForRepairRecord != null) {
        MechanicRepairRecordDialog(
            workOrder = selectedOrderForRepairRecord!!,
            onConfirm = { summary, parts, photoUri ->
                viewModel.recordRepair(
                    workOrderId = selectedOrderForRepairRecord!!.id,
                    summary = summary,
                    parts = parts,
                    photoUri = photoUri
                )
                selectedOrderForRepairRecord = null
            },
            onDismiss = { selectedOrderForRepairRecord = null }
        )
    }

    // Final Release Dialog
    if (selectedOrderForFinalRelease != null) {
        MechanicFinalReleaseDialog(
            workOrder = selectedOrderForFinalRelease!!,
            onConfirm = { newAuth ->
                viewModel.mechanicFinalRelease(selectedOrderForFinalRelease!!.id, newAuth)
                selectedOrderForFinalRelease = null
            },
            onDismiss = { selectedOrderForFinalRelease = null }
        )
    }
}

@Composable
fun WorkOrderCard(
    workOrder: WorkOrderEntity,
    vehicle: com.example.data.model.VehicleEntity?,
    onEvaluate: () -> Unit,
    onRecordRepair: () -> Unit,
    onFinalRelease: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
        modifier = Modifier.fillMaxWidth()
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
                        text = "เวลาแจ้ง: ${DateUtils.formatThaiTime(workOrder.createdAt)}",
                        fontSize = 13.sp,
                        color = MediumGrayText
                    )
                }

                StatusBadge(
                    statusText = when (workOrder.status) {
                        WorkOrderStatus.WAITING_INSPECTION.name -> "รอตรวจ"
                        WorkOrderStatus.INSPECTED_PENDING.name -> "รอช่าง"
                        WorkOrderStatus.IN_REPAIR.name -> "กำลังซ่อม"
                        WorkOrderStatus.WAITING_PARTS.name -> "รออะไหล่"
                        WorkOrderStatus.SENT_EXTERNAL.name -> "ส่งอู่นอก"
                        WorkOrderStatus.REPAIRED_WAITING_DRIVER.name -> "รอคนขับรับ"
                        WorkOrderStatus.DRIVER_ACCEPTED_WAITING_MECHANIC.name -> "รอปล่อยรถ"
                        WorkOrderStatus.COMPLETED.name -> "เสร็จสิ้น"
                        else -> workOrder.status
                    },
                    type = when (workOrder.status) {
                        WorkOrderStatus.COMPLETED.name -> "normal"
                        WorkOrderStatus.DRIVER_ACCEPTED_WAITING_MECHANIC.name -> "warning"
                        WorkOrderStatus.REPAIRED_WAITING_DRIVER.name -> "info"
                        else -> "issue"
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "รายละเอียด: ${workOrder.repairSummary ?: "-"}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkGrayText
            )

            if (!workOrder.partsReplaced.isNullOrBlank()) {
                Text(
                    text = "อะไหล่: ${workOrder.partsReplaced}",
                    fontSize = 14.sp,
                    color = MediumGrayText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("สิทธิ์รถปัจจุบัน:", fontSize = 14.sp, color = MediumGrayText)
                AuthStatusBadge(workOrder.authStatus)
            }

            if (workOrder.driverAccepted != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = if (workOrder.driverAccepted == true) Color(0xFFDCFCE7) else Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (workOrder.driverAccepted == true)
                            "คนขับตรวจรับแล้ว: ${workOrder.driverAcceptanceNotes ?: "ผ่าน"}"
                        else
                            "คนขับไม่รับงาน: ${workOrder.driverAcceptanceNotes ?: "-"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (workOrder.driverAccepted == true) Color(0xFF15803D) else StatusIssueRed,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons based on status
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Action 1: Evaluate
                if (workOrder.status == WorkOrderStatus.WAITING_INSPECTION.name ||
                    workOrder.status == WorkOrderStatus.INSPECTED_PENDING.name ||
                    workOrder.status == WorkOrderStatus.IN_REPAIR.name) {
                    Button(
                        onClick = onEvaluate,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("ประเมินสิทธิ์รถ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Action 2: Record Repair
                if (workOrder.status == WorkOrderStatus.IN_REPAIR.name ||
                    workOrder.status == WorkOrderStatus.WAITING_PARTS.name ||
                    workOrder.status == WorkOrderStatus.SENT_EXTERNAL.name) {
                    Button(
                        onClick = onRecordRepair,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("บันทึกผลการซ่อม", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Action 3: Final Release after Driver Acceptance
                if (workOrder.status == WorkOrderStatus.DRIVER_ACCEPTED_WAITING_MECHANIC.name) {
                    Button(
                        onClick = onFinalRelease,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusNormalGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("ยืนยันปล่อยรถจบงาน", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MechanicEvaluationDialog(
    workOrder: WorkOrderEntity,
    onConfirm: (VehicleAuthStatus, String?, WorkOrderStatus, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAuth by remember { mutableStateOf(VehicleAuthStatus.CONDITIONAL_USE) }
    var selectedNextStatus by remember { mutableStateOf(WorkOrderStatus.IN_REPAIR) }
    var conditions by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("ช่างประเมินอาการและกำหนดสิทธิ์รถ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("กำหนดสิทธิ์การใช้รถ *", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                listOf(
                    VehicleAuthStatus.CONDITIONAL_USE to "ใช้ได้ตามเงื่อนไข (เช่น ขับช้า ไม่เกิน 60)",
                    VehicleAuthStatus.PROHIBITED to "ห้ามใช้งานเด็ดขาด (หยุดรถ รอซ่อม)",
                    VehicleAuthStatus.GARAGE_TRANSIT_ONLY to "อนุญาตเฉพาะเดินทางไปอู่ (ห้ามบรรทุกน้ำมัน)"
                ).forEach { (auth, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAuth = auth }
                    ) {
                        RadioButton(
                            selected = selectedAuth == auth,
                            onClick = { selectedAuth = auth }
                        )
                        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = conditions,
                    onValueChange = { conditions = it },
                    label = { Text("เงื่อนไขหรือข้อจำกัด (เช่น วิ่งไม่เกิน 60 กม./ชม.)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("สถานะขั้นตอนการซ่อมถัดไป:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                listOf(
                    WorkOrderStatus.IN_REPAIR to "กำลังดำเนินการซ่อมเอง",
                    WorkOrderStatus.WAITING_PARTS to "รออะไหล่",
                    WorkOrderStatus.SENT_EXTERNAL to "ส่งอู่นอก (อู่สหยนต์ นครปฐม)"
                ).forEach { (st, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedNextStatus = st }
                    ) {
                        RadioButton(
                            selected = selectedNextStatus == st,
                            onClick = { selectedNextStatus = st }
                        )
                        Text(label, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedAuth,
                        conditions.ifBlank { null },
                        selectedNextStatus,
                        "ช่างประเมินสิทธิ์: ${selectedAuth.thaiLabel}"
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("บันทึกการประเมิน", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก")
            }
        }
    )
}

@Composable
fun MechanicRepairRecordDialog(
    workOrder: WorkOrderEntity,
    onConfirm: (String, String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var summary by remember { mutableStateOf("") }
    var parts by remember { mutableStateOf("") }
    var noPartsChanged by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("บันทึกผลการซ่อมแซม", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "รายการแจ้งเดิม: ${workOrder.repairSummary ?: "-"}",
                    fontSize = 14.sp,
                    color = MediumGrayText
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("สรุปงานที่ทำ *") },
                    placeholder = { Text("เช่น ขันแน่นท่อลม เปลี่ยนสายพาน หรือ เชื่อมยึดแป้น") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (!noPartsChanged) {
                    OutlinedTextField(
                        value = parts,
                        onValueChange = { parts = it },
                        label = { Text("รายการอะไหล่ที่เปลี่ยน") },
                        placeholder = { Text("เช่น ซีลยางวาล์ว 2 ตัว, หัวต่อแคมล็อก") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        noPartsChanged = !noPartsChanged
                        if (noPartsChanged) parts = "ไม่มีการเปลี่ยนอะไหล่" else parts = ""
                    }
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = noPartsChanged,
                        onCheckedChange = {
                            noPartsChanged = it
                            if (it) parts = "ไม่มีการเปลี่ยนอะไหล่" else parts = ""
                        }
                    )
                    Text("ไม่มีการเปลี่ยนอะไหล่ (ปรับตั้ง/แก้ไขเดิม)", fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (summary.isBlank()) return@Button
                    val finalParts = if (noPartsChanged) "ไม่มีการเปลี่ยนอะไหล่" else parts.ifBlank { "ไม่มีการเปลี่ยนอะไหล่" }
                    onConfirm(summary, finalParts, null)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("ส่งต่อให้คนขับตรวจรับ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก")
            }
        }
    )
}

@Composable
fun MechanicFinalReleaseDialog(
    workOrder: WorkOrderEntity,
    onConfirm: (VehicleAuthStatus) -> Unit,
    onDismiss: () -> Unit
) {
    var newAuth by remember { mutableStateOf(VehicleAuthStatus.CONDITIONAL_USE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("ช่างยืนยันปล่อยรถหลังคนขับตรวจรับผ่าน", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "คนขับได้ตรวจรับงานซ่อมผ่านเรียบร้อยแล้ว ช่างกรุณายืนยันสิทธิ์รถหลังจบงาน:",
                    fontSize = 15.sp,
                    color = DarkGrayText
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { newAuth = VehicleAuthStatus.CONDITIONAL_USE }
                ) {
                    RadioButton(
                        selected = newAuth == VehicleAuthStatus.CONDITIONAL_USE,
                        onClick = { newAuth = VehicleAuthStatus.CONDITIONAL_USE }
                    )
                    Text("ใช้งานได้ตามเงื่อนไข / ปกติ", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newAuth) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusNormalGreen)
            ) {
                Text("ยืนยันปล่อยรถ (จบงาน)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก")
            }
        }
    )
}
