package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.model.VehicleAuthStatus
import com.example.data.model.VehicleEntity
import com.example.data.model.VehiclePairEntity
import com.example.ui.theme.DarkGrayText
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MediumGrayText
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusInfoBlue
import com.example.ui.theme.StatusInfoContainer
import com.example.ui.theme.StatusIssueContainer
import com.example.ui.theme.StatusIssueOnContainer
import com.example.ui.theme.StatusIssueRed
import com.example.ui.theme.StatusNormalContainer
import com.example.ui.theme.StatusNormalGreen
import com.example.ui.theme.StatusNormalOnContainer
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.StatusWarningContainer
import com.example.ui.theme.StatusWarningOnContainer

@Composable
fun StatusBadge(
    statusText: String,
    type: String = "info", // normal, issue, warning, info
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (type) {
        "normal", "success" -> Triple(StatusNormalContainer, StatusNormalOnContainer, StatusNormalGreen)
        "issue", "danger" -> Triple(StatusIssueContainer, StatusIssueOnContainer, StatusIssueRed)
        "warning" -> Triple(StatusWarningContainer, StatusWarningOnContainer, StatusWarningAmber)
        else -> Triple(StatusInfoContainer, StatusInfoBlue, StatusInfoBlue)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(textColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = statusText,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AuthStatusBadge(authStatusStr: String) {
    when (authStatusStr) {
        VehicleAuthStatus.PROHIBITED.name -> {
            StatusBadge("ห้ามใช้งาน", "danger")
        }
        VehicleAuthStatus.GARAGE_TRANSIT_ONLY.name -> {
            StatusBadge("เฉพาะไปอู่", "warning")
        }
        VehicleAuthStatus.CONDITIONAL_USE.name -> {
            StatusBadge("ใช้ตามเงื่อนไข", "info")
        }
        else -> {
            StatusBadge("ยังไม่ประเมิน", "warning")
        }
    }
}

@Composable
fun RoleBadge(roleStr: String) {
    val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { null }
    val label = role?.thaiLabel ?: roleStr
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun LargeActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val btnMod = modifier
        .fillMaxWidth()
        .height(56.dp)
        .let { if (testTag != null) it.testTag(testTag) else it }

    if (isPrimary) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyPrimary,
                contentColor = Color.White
            ),
            modifier = btnMod
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            modifier = btnMod
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VehicleSummaryCard(
    vehicle: VehicleEntity?,
    pair: VehiclePairEntity?,
    trailer: VehicleEntity?,
    isSpare: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isSpare) "รถสำรองที่ใช้วันนี้" else "รถประจำที่ปฏิบัติงาน",
                            fontSize = 15.sp,
                            color = MediumGrayText,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (vehicle != null) "รหัส ${vehicle.displayCode} (${vehicle.plateNumber})" else "ยังไม่ได้มอบหมายรถ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGrayText
                        )
                    }
                }
                if (isSpare) {
                    StatusBadge("รถสำรอง", "warning")
                }
            }

            if (vehicle != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "สถานะสิทธิ์รถ:",
                        fontSize = 14.sp,
                        color = MediumGrayText
                    )
                    AuthStatusBadge(vehicle.authStatus)
                }

                if (pair != null && trailer != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "คู่แม่–ลูก: ${pair.pairCode}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGrayText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "หาง: ${trailer.plateNumber} (${trailer.displayCode})",
                                    fontSize = 14.sp,
                                    color = MediumGrayText,
                                    modifier = Modifier.weight(1f, fill = false).padding(end = 6.dp)
                                )
                                AuthStatusBadge(trailer.authStatus)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SyncStatusBanner(lastUpdatedTime: Long) {
    Surface(
        color = Color(0xFFEFF6FF),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = StatusInfoBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "เฟส 1: ทดลองแบบ Standalone (ข้อมูลปลอดภัยในเครื่อง / Adapter ตัวอย่าง)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E40AF)
                )
                Text(
                    text = "เวลาอัปเดต: ${com.example.util.DateUtils.formatThaiTime(lastUpdatedTime)} (ไม่ส่งทับระบบจริงโดยไม่ได้รับอนุญาต)",
                    fontSize = 12.sp,
                    color = Color(0xFF3B82F6)
                )
            }
        }
    }
}
