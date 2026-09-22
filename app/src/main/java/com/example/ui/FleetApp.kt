package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AuditLogScreen
import com.example.ui.screens.DriverHomeScreen
import com.example.ui.screens.GateReleaseScreen
import com.example.ui.screens.MechanicScreen
import com.example.ui.screens.OwnerOfficeScreen
import com.example.ui.screens.PreTripInspectionScreen
import com.example.ui.screens.RoadsideBreakdownScreen
import com.example.ui.screens.SpareTruckScreen
import com.example.ui.screens.UserSwitchDialog
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.FleetViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun FleetApp(viewModel: FleetViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showUserSwitchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Show bottom bar on primary role dashboards
            if (currentScreen is Screen.DriverHome ||
                currentScreen is Screen.MechanicBoard ||
                currentScreen is Screen.GateReleaseCheck ||
                currentScreen is Screen.SpareTruckManagement ||
                currentScreen is Screen.OwnerOffice
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = NavyPrimary
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.DriverHome,
                        onClick = { viewModel.navigateTo(Screen.DriverHome) },
                        icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "คนขับ") },
                        label = { Text("คนขับ", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            indicatorColor = Color(0xFFE2E8F0)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.MechanicBoard,
                        onClick = { viewModel.navigateTo(Screen.MechanicBoard) },
                        icon = { Icon(Icons.Default.Handyman, contentDescription = "ช่าง") },
                        label = { Text("ช่าง", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            indicatorColor = Color(0xFFE2E8F0)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.GateReleaseCheck,
                        onClick = { viewModel.navigateTo(Screen.GateReleaseCheck) },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "ปล่อยรถ") },
                        label = { Text("ปล่อยรถ", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            indicatorColor = Color(0xFFE2E8F0)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.SpareTruckManagement,
                        onClick = { viewModel.navigateTo(Screen.SpareTruckManagement) },
                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = "จัดคิว/สำรอง") },
                        label = { Text("จัดคิว/สำรอง", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            indicatorColor = Color(0xFFE2E8F0)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.OwnerOffice,
                        onClick = { viewModel.navigateTo(Screen.OwnerOffice) },
                        icon = { Icon(Icons.Default.Business, contentDescription = "ออฟฟิศ/เจ้าของ") },
                        label = { Text("ออฟฟิศ", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            indicatorColor = Color(0xFFE2E8F0)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.DriverHome -> DriverHomeScreen(
                    viewModel = viewModel,
                    onOpenUserSwitch = { showUserSwitchDialog = true }
                )
                is Screen.PreTripInspection -> PreTripInspectionScreen(viewModel = viewModel)
                is Screen.RoadsideBreakdown -> RoadsideBreakdownScreen(viewModel = viewModel)
                is Screen.MechanicBoard -> MechanicScreen(
                    viewModel = viewModel,
                    onOpenUserSwitch = { showUserSwitchDialog = true }
                )
                is Screen.GateReleaseCheck -> GateReleaseScreen(
                    viewModel = viewModel,
                    onOpenUserSwitch = { showUserSwitchDialog = true }
                )
                is Screen.SpareTruckManagement -> SpareTruckScreen(
                    viewModel = viewModel,
                    onOpenUserSwitch = { showUserSwitchDialog = true }
                )
                is Screen.OwnerOffice -> OwnerOfficeScreen(
                    viewModel = viewModel,
                    onOpenUserSwitch = { showUserSwitchDialog = true }
                )
                is Screen.AuditLogs -> AuditLogScreen(viewModel = viewModel)
            }
        }
    }

    if (showUserSwitchDialog) {
        UserSwitchDialog(
            users = allUsers,
            currentUser = currentUser,
            onSelectUser = { user ->
                viewModel.switchUser(user)
                showUserSwitchDialog = false
            },
            onDismiss = { showUserSwitchDialog = false }
        )
    }
}
