package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.components.RoleBadge
import com.example.ui.theme.DarkGrayText
import com.example.ui.theme.MediumGrayText
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusNormalGreen

@Composable
fun UserSwitchDialog(
    users: List<UserEntity>,
    currentUser: UserEntity?,
    onSelectUser: (UserEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "เลือกผู้ปฏิบัติงาน (ช่วงทดลอง)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Text(
                    text = "กดเลือกชื่อเพื่อสลับบทบาทในการทดสอบระบบ",
                    fontSize = 14.sp,
                    color = MediumGrayText
                )
            }
        },
        text = {
            Column {
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "คำชี้แจง: ช่วงทดลองใช้แบบเลือกชื่อไม่มีรหัสผ่าน ยังไม่ใช่ระบบยืนยันตัวตนที่ปลอดภัยในระดับการใช้งานจริง",
                        fontSize = 12.sp,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().height(360.dp)
                ) {
                    items(users) { user ->
                        val isSelected = currentUser?.id == user.id
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE2E8F0) else Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) NavyPrimary else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectUser(user) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(12.dp).fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isSelected) NavyPrimary else MediumGrayText,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = user.name,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkGrayText
                                        )
                                        RoleBadge(user.role)
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "ใช้งานอยู่",
                                        tint = StatusNormalGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ปิด", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}
