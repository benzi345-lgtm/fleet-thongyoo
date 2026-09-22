package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CheckResult
import com.example.data.model.InspectionItemEntity
import com.example.ui.components.LargeActionButton
import com.example.ui.components.StatusBadge
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreTripInspectionScreen(viewModel: FleetViewModel) {
    val assignedVehicle by viewModel.assignedVehicle.collectAsState()
    val assignedPair by viewModel.assignedPair.collectAsState()
    val inspection by viewModel.todayInspection.collectAsState()
    val allItems by viewModel.todayInspectionItems.collectAsState()

    var activeCategoryIndex by remember { mutableIntStateOf(1) }
    var mileageText by remember { mutableStateOf(inspection?.mileage ?: "") }
    var mileageBroken by remember { mutableStateOf(inspection?.mileageBroken ?: false) }
    var showMethodDialogFor by remember { mutableStateOf<InspectionItemEntity?>(null) }
    var showConfirmSubmitDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        1 to "1. ยางและล้อ",
        2 to "2. เบรกและระบบลม",
        3 to "3. ไฟและสัญญาณ",
        4 to "4. ทัศนวิสัยและห้องขับ",
        5 to "5. เครื่องยนต์และของเหลว",
        6 to "6. ถังและอุปกรณ์ขนน้ำมัน",
        7 to "7. อุปกรณ์ประจำรถและฉุกเฉิน",
        8 to "8. จุดเชื่อมแม่–ลูก",
        9 to "9. อาการเดิมและงานค้าง"
    )

    val checkedCount = allItems.count { it.result != null }
    val totalCount = allItems.size
    val progress = if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f

    val currentCategoryItems = allItems.filter { it.categoryId == activeCategoryIndex }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "แบบตรวจก่อนออก (9 กลุ่ม)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "รถ: ${assignedVehicle?.displayCode ?: "-"} (${assignedVehicle?.plateNumber ?: "-"})",
                            fontSize = 13.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.DriverHome) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "กลับหน้าหลัก",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Overall Progress Bar
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ความคืบหน้ารวม",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGrayText
                            )
                            Text(
                                text = "ตรวจแล้ว $checkedCount / $totalCount ข้อ",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = if (progress >= 1f) StatusNormalGreen else AmberAccent,
                            trackColor = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "คำชี้แจง: ห้ามเดาผลตรวจ ไม่มีปุ่มผ่านทั้งหมด ต้องตรวจทีละข้ออย่างรอบคอบ",
                            fontSize = 12.sp,
                            color = MediumGrayText
                        )
                    }
                }
            }

            // Category Selector Tabs / Chips
            item {
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { (catId, catName) ->
                        val catItems = allItems.filter { it.categoryId == catId }
                        val catChecked = catItems.count { it.result != null }
                        val isCurrent = activeCategoryIndex == catId
                        val isDone = catItems.isNotEmpty() && catChecked == catItems.size

                        Surface(
                            color = if (isCurrent) NavyPrimary else if (isDone) Color(0xFFDCFCE7) else Color.White,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCurrent) NavyPrimary else LightBorder
                            ),
                            modifier = Modifier.clickable { activeCategoryIndex = catId }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = catName,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) Color.White else DarkGrayText
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (isCurrent) Color(0xFF334155) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "$catChecked/${catItems.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) Color.White else MediumGrayText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Current Category Items List
            items(currentCategoryItems) { item ->
                InspectionItemCard(
                    item = item,
                    onUpdateResult = { result ->
                        viewModel.updateCheckItem(item, result, item.notes, item.photoUri)
                    },
                    onUpdateNotes = { notes ->
                        val res = if (item.result != null) CheckResult.valueOf(item.result) else CheckResult.NORMAL
                        viewModel.updateCheckItem(item, res, notes, item.photoUri)
                    },
                    onShowMethod = { showMethodDialogFor = item }
                )
            }

            // Bottom Navigation between categories + Submit
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "บันทึกเลขไมล์รถ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGrayText
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (!mileageBroken) {
                            OutlinedTextField(
                                value = mileageText,
                                onValueChange = { mileageText = it },
                                label = { Text("เลขกิโลเมตรหน้าปัด (กม.)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { mileageBroken = !mileageBroken }
                        ) {
                            Checkbox(checked = mileageBroken, onCheckedChange = { mileageBroken = it })
                            Text("ไมล์ใช้ไม่ได้ / หน้าปัดชำรุด (ไม่บังคับเลขสมมติ)", fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (activeCategoryIndex > 1) {
                                OutlinedButton(
                                    onClick = { activeCategoryIndex-- },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Text("กลุ่มก่อนหน้า", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (activeCategoryIndex < 9) {
                                Button(
                                    onClick = { activeCategoryIndex++ },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Text("กลุ่มถัดไป", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LargeActionButton(
                            text = "ส่งผลตรวจก่อนออก",
                            onClick = { showConfirmSubmitDialog = true },
                            isPrimary = true,
                            testTag = "submit_inspection_button"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Method dialog
    if (showMethodDialogFor != null) {
        AlertDialog(
            onDismissRequest = { showMethodDialogFor = null },
            title = {
                Text(showMethodDialogFor!!.itemTitle, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "คำแนะนำวิธีตรวจ (ช่างสอน):",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = showMethodDialogFor!!.inspectionMethod ?: "ยังไม่มีวิธีตรวจเฉพาะ ให้สอบถามช่างสอน",
                        fontSize = 15.sp,
                        color = DarkGrayText
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodDialogFor = null }) {
                    Text("รับทราบ", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Confirm Submit Dialog
    if (showConfirmSubmitDialog) {
        val uncheckLeft = totalCount - checkedCount
        val issues = allItems.count { it.result == "ISSUE" }
        val mechChecks = allItems.count { it.result == "MECHANIC_CHECK" }

        AlertDialog(
            onDismissRequest = { showConfirmSubmitDialog = false },
            title = {
                Text("ยืนยันการส่งแบบตรวจก่อนออก", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    if (uncheckLeft > 0) {
                        Text(
                            text = "คำเตือน: ยังตรวจไม่ครบทุกข้อ (เหลือ $uncheckLeft ข้อ) จะบันทึกเป็นฉบับร่าง พี่ก้อมจะไม่สามารถปล่อยรถได้จนกว่าจะตรวจครบ",
                            fontSize = 15.sp,
                            color = StatusIssueRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (issues > 0 || mechChecks > 0) {
                        Text(
                            text = "ตรวจพบข้อผิดปกติ $issues รายการ และต้องการให้ช่างตรวจ $mechChecks รายการ ระบบจะสร้างใบงานส่งต่อให้ช่างประเมินก่อนปล่อยรถ",
                            fontSize = 15.sp,
                            color = StatusWarningAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "ตรวจครบถ้วน $totalCount ข้อ ทุกรายการปกติ รถพร้อมสำหรับการตรวจสอบปล่อยรถของพี่ก้อม",
                            fontSize = 15.sp,
                            color = Color(0xFF15803D),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmSubmitDialog = false
                        viewModel.submitInspection(mileageText, mileageBroken) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("ยืนยันการส่ง", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSubmitDialog = false }) {
                    Text("ตรวจต่อ")
                }
            }
        )
    }
}

@Composable
fun InspectionItemCard(
    item: InspectionItemEntity,
    onUpdateResult: (CheckResult) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onShowMethod: () -> Unit
) {
    var notesText by remember(item.id) { mutableStateOf(item.notes ?: "") }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(
            width = if (item.result != null) 1.5.dp else 1.dp,
            color = when (item.result) {
                "NORMAL" -> StatusNormalGreen
                "ISSUE" -> StatusIssueRed
                "MECHANIC_CHECK" -> StatusWarningAmber
                else -> LightBorder
            }
        ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = item.targetLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.itemTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGrayText
                    )
                }

                IconButton(
                    onClick = onShowMethod,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.HelpOutline,
                        contentDescription = "วิธีตรวจ",
                        tint = NavyPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Option Buttons: NORMAL, ISSUE, MECHANIC_CHECK
            // NO default selection! NO pass-all button!
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // ปกติ (NORMAL)
                val isNormal = item.result == CheckResult.NORMAL.name
                Button(
                    onClick = { onUpdateResult(CheckResult.NORMAL) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNormal) StatusNormalGreen else Color(0xFFF1F5F9),
                        contentColor = if (isNormal) Color.White else DarkGrayText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("item_normal_${item.itemKey}")
                ) {
                    Text(
                        text = "ปกติ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // พบปัญหา (ISSUE)
                val isIssue = item.result == CheckResult.ISSUE.name
                Button(
                    onClick = { onUpdateResult(CheckResult.ISSUE) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIssue) StatusIssueRed else Color(0xFFF1F5F9),
                        contentColor = if (isIssue) Color.White else DarkGrayText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1.15f)
                        .height(48.dp)
                        .testTag("item_issue_${item.itemKey}")
                ) {
                    Text(
                        text = "พบปัญหา",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // ให้ช่างช่วยตรวจ (MECHANIC_CHECK)
                val isMech = item.result == CheckResult.MECHANIC_CHECK.name
                Button(
                    onClick = { onUpdateResult(CheckResult.MECHANIC_CHECK) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMech) StatusWarningAmber else Color(0xFFF1F5F9),
                        contentColor = if (isMech) Color.White else DarkGrayText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("item_mech_${item.itemKey}")
                ) {
                    Text(
                        text = "ให้ช่างช่วย",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            // If issue or mechanic check, expand notes input
            AnimatedVisibility(visible = item.result == CheckResult.ISSUE.name || item.result == CheckResult.MECHANIC_CHECK.name) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = {
                            notesText = it
                            onUpdateNotes(it)
                        },
                        label = { Text("ระบุรายละเอียดปัญหา / จุดที่พบ") },
                        placeholder = { Text("เช่น ล้อหลังซ้ายนอก บวมด้านข้าง หรือ สายลมมีเสียงฟี่") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
