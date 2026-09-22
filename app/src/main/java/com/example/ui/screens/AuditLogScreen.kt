package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DarkGrayText
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MediumGrayText
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.FleetViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(viewModel: FleetViewModel) {
    val auditLogs by viewModel.allAuditLogs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ประวัติการปฏิบัติงาน (Audit Log)",
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
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "รีเฟรช", tint = Color.White)
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = NavyPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "บันทึกประวัติความรับผิดชอบ: ผู้บันทึก (actor), ผู้อนุมัติ (authorizedBy), และเหตุผลทุกครั้งที่มีการเปลี่ยนสถานะ",
                            fontSize = 13.sp,
                            color = NavyPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (auditLogs.isEmpty()) {
                item {
                    Text(
                        text = "ยังไม่มีประวัติการทำรายการ",
                        fontSize = 15.sp,
                        color = MediumGrayText,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(auditLogs) { log ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = log.action,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = DateUtils.formatThaiTime(log.createdAt),
                                fontSize = 13.sp,
                                color = MediumGrayText
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ผู้ดำเนินการ: ${log.actorName} (${log.actorId})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkGrayText
                        )

                        if (!log.authorizedBy.isNullOrBlank()) {
                            Text(
                                text = "ผู้อนุมัติ: ${log.authorizedBy}",
                                fontSize = 14.sp,
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (log.fromStatus != null || log.toStatus != null) {
                            Text(
                                text = "การเปลี่ยนสถานะ: ${log.fromStatus ?: "-"} ➔ ${log.toStatus ?: "-"}",
                                fontSize = 14.sp,
                                color = MediumGrayText
                            )
                        }

                        if (!log.reason.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "เหตุผล/รายละเอียด: ${log.reason}",
                                fontSize = 14.sp,
                                color = DarkGrayText
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
}
