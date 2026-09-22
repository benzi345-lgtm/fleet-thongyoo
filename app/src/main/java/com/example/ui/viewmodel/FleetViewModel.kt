package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseSeeder
import com.example.data.model.AssignmentEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.BreakdownReportEntity
import com.example.data.model.CheckResult
import com.example.data.model.DispatchReleaseEntity
import com.example.data.model.InspectionEntity
import com.example.data.model.InspectionItemEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleAuthStatus
import com.example.data.model.VehicleEntity
import com.example.data.model.VehiclePairEntity
import com.example.data.model.WorkOrderEntity
import com.example.data.model.WorkOrderStatus
import com.example.data.repository.FleetRepository
import com.example.data.repository.ReleaseEligibility
import com.example.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object DriverHome : Screen()
    object PreTripInspection : Screen()
    object RoadsideBreakdown : Screen()
    object MechanicBoard : Screen()
    object GateReleaseCheck : Screen()
    object SpareTruckManagement : Screen()
    object OwnerOffice : Screen()
    object AuditLogs : Screen()
}

@OptIn(ExperimentalCoroutinesApi::class)
class FleetViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = FleetRepository(db.fleetDao())

    // Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.DriverHome)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Status Message / Snackbar notification
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // Refresh Trigger
    private val _refreshTrigger = MutableStateFlow(0L)
    val lastRefreshTime: StateFlow<Long> = _refreshTrigger.asStateFlow()

    fun refreshData() {
        _refreshTrigger.value = System.currentTimeMillis()
        showMessage("อัปเดตข้อมูลล่าสุดแล้ว")
    }

    init {
        viewModelScope.launch {
            DatabaseSeeder.seedIfEmpty(db.fleetDao())
            _refreshTrigger.value = System.currentTimeMillis()
        }
    }

    // Current User
    val currentUserId: StateFlow<String> = repository.currentUserId

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = repository.currentUserId
        .flatMapLatest { id -> repository.getUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun switchUser(user: UserEntity) {
        repository.setCurrentUser(user.id)
        showMessage("สลับผู้ใช้เป็น: ${user.name} (${user.role})")
        // Route to home suitable for role
        when (user.role) {
            UserRole.DRIVER.name -> navigateTo(Screen.DriverHome)
            UserRole.MECHANIC_HEAD.name, UserRole.MECHANIC_FIELD.name -> navigateTo(Screen.MechanicBoard)
            UserRole.GATE_KOM.name -> navigateTo(Screen.GateReleaseCheck)
            UserRole.DISPATCHER_TONG.name, UserRole.DISPATCHER_NGUYEN.name -> navigateTo(Screen.SpareTruckManagement)
            UserRole.OWNER.name, UserRole.OFFICE.name -> navigateTo(Screen.OwnerOffice)
            else -> navigateTo(Screen.DriverHome)
        }
    }

    // Active Assignment for Current Driver
    val activeAssignment: StateFlow<AssignmentEntity?> = repository.currentUserId
        .flatMapLatest { id -> repository.getActiveAssignmentForDriver(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Assigned Vehicle
    val assignedVehicle: StateFlow<VehicleEntity?> = activeAssignment
        .flatMapLatest { assign ->
            if (assign != null) repository.getVehicle(assign.vehicleId) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Assigned Pair
    val assignedPair: StateFlow<VehiclePairEntity?> = activeAssignment
        .flatMapLatest { assign ->
            if (assign?.pairId != null) repository.getPair(assign.pairId) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Assigned Trailer Vehicle (if Semi)
    val assignedTrailer: StateFlow<VehicleEntity?> = assignedPair
        .flatMapLatest { pair ->
            if (pair != null) repository.getVehicle(pair.trailerVehicleId) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Regular Vehicle (if currently on Spare)
    val regularVehicle: StateFlow<VehicleEntity?> = activeAssignment
        .flatMapLatest { assign ->
            if (assign?.regularVehicleId != null) repository.getVehicle(assign.regularVehicleId) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Work Orders for assigned vehicle (to see repairs if regular truck is in repair)
    val regularVehicleWorkOrders: StateFlow<List<WorkOrderEntity>> = regularVehicle
        .flatMapLatest { reg ->
            if (reg != null) db.fleetDao().getActiveWorkOrdersForVehicle(reg.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Vehicles
    val allVehicles: StateFlow<List<VehicleEntity>> = repository.getAllVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Work Orders
    val allWorkOrders: StateFlow<List<WorkOrderEntity>> = repository.getAllWorkOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeWorkOrders: StateFlow<List<WorkOrderEntity>> = repository.getActiveWorkOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Breakdowns
    val allBreakdowns: StateFlow<List<BreakdownReportEntity>> = repository.getAllBreakdowns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Audit Logs
    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's Inspection for Driver
    val todayInspection: StateFlow<InspectionEntity?> = combine(
        currentUserId,
        assignedVehicle,
        _refreshTrigger
    ) { driverId, vehicle, _ ->
        if (vehicle != null) {
            repository.getInspectionForVehicleToday(vehicle.id, DateUtils.getWorkDate()).first()
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Today's Inspection Items
    val todayInspectionItems: StateFlow<List<InspectionItemEntity>> = todayInspection
        .flatMapLatest { insp ->
            if (insp != null) repository.getInspectionItems(insp.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pending acceptance work orders for current driver
    val pendingAcceptanceWorkOrders: StateFlow<List<WorkOrderEntity>> = combine(
        activeWorkOrders,
        assignedVehicle
    ) { orders, vehicle ->
        if (vehicle != null) {
            orders.filter { it.vehicleId == vehicle.id && it.status == WorkOrderStatus.REPAIRED_WAITING_DRIVER.name }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Release for current vehicle today
    val todayRelease: StateFlow<DispatchReleaseEntity?> = assignedVehicle
        .flatMapLatest { v ->
            if (v != null) db.fleetDao().getReleaseForVehicleToday(v.id, DateUtils.getWorkDate()) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // START OR RESUME PRE-TRIP INSPECTION
    fun startOrResumeInspection() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val vehicle = assignedVehicle.value ?: run {
                showMessage("ยังไม่พบรถที่ได้รับมอบหมาย")
                return@launch
            }
            val pair = assignedPair.value
            val isSemi = pair != null
            val trailerId = pair?.trailerVehicleId

            val inspection = repository.getOrCreateTodayInspection(
                driverId = user.id,
                vehicleId = vehicle.id,
                pairId = pair?.id,
                isSemiTrailer = isSemi,
                trailerId = trailerId
            )
            _refreshTrigger.value = System.currentTimeMillis()
            navigateTo(Screen.PreTripInspection)
        }
    }

    // UPDATE INSPECTION ITEM
    fun updateCheckItem(item: InspectionItemEntity, newResult: CheckResult, notes: String?, photoUri: String?) {
        viewModelScope.launch {
            val updated = item.copy(
                result = newResult.name,
                notes = notes,
                photoUri = photoUri
            )
            repository.updateInspectionItem(updated)
        }
    }

    // SUBMIT INSPECTION
    fun submitInspection(mileage: String?, mileageBroken: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val insp = todayInspection.value ?: return@launch

            val result = repository.submitInspection(
                inspectionId = insp.id,
                mileage = mileage,
                mileageBroken = mileageBroken,
                driverUser = user
            )
            if (result != null) {
                if (result.isComplete && !result.hasIssues && !result.hasPendingMechanicCheck) {
                    showMessage("บันทึกแบบตรวจครบถ้วน ทุกรายการปกติ")
                } else if (result.hasIssues || result.hasPendingMechanicCheck) {
                    showMessage("บันทึกแบบตรวจแล้ว: มีรายการที่ส่งต่อช่างประเมิน")
                } else {
                    showMessage("บันทึกฉบับร่างแล้ว (ยังตรวจไม่ครบทุกข้อ)")
                }
                _refreshTrigger.value = System.currentTimeMillis()
                onSuccess()
                navigateTo(Screen.DriverHome)
            }
        }
    }

    // REPORT ROADSIDE BREAKDOWN
    fun reportRoadsideBreakdown(
        symptom: String,
        situation: String,
        latitude: Double?,
        longitude: Double?,
        accuracy: Float?,
        locationText: String,
        photoUri: String?,
        isOfficeCall: Boolean = false,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val vehicle = assignedVehicle.value ?: run {
                showMessage("ไม่พบรถที่เกิดเหตุ")
                return@launch
            }

            repository.reportBreakdown(
                vehicleId = vehicle.id,
                driverId = user.id,
                symptom = symptom,
                situation = situation,
                latitude = latitude,
                longitude = longitude,
                locationAccuracy = accuracy,
                locationText = locationText,
                photoUri = photoUri,
                actorUser = user,
                isRecordedByOffice = isOfficeCall
            )
            _refreshTrigger.value = System.currentTimeMillis()
            showMessage("ส่งแจ้งเหตุเสียเรียบร้อยแล้ว แจ้งช่างเข้าดำเนินการ")
            onSuccess()
            navigateTo(Screen.DriverHome)
        }
    }

    // MECHANIC EVALUATION
    fun evaluateWorkOrder(
        workOrderId: String,
        authStatus: VehicleAuthStatus,
        conditions: String?,
        nextStatus: WorkOrderStatus,
        reason: String
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.updateWorkOrderEvaluation(
                workOrderId = workOrderId,
                authStatus = authStatus,
                authConditions = conditions,
                nextStatus = nextStatus,
                mechanicUser = user,
                reason = reason
            )
            _refreshTrigger.value = System.currentTimeMillis()
            showMessage("ประเมินใบงานและสถานะอนุญาตสำเร็จ: ${authStatus.thaiLabel}")
        }
    }

    // MECHANIC REPAIR RECORD
    fun recordRepair(
        workOrderId: String,
        summary: String,
        parts: String,
        photoUri: String?
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.recordRepairCompletion(
                workOrderId = workOrderId,
                repairSummary = summary,
                partsReplaced = parts,
                photoUri = photoUri,
                mechanicUser = user
            )
            _refreshTrigger.value = System.currentTimeMillis()
            showMessage("บันทึกการซ่อมเรียบร้อย ส่งต่อให้คนขับตรวจรับ")
        }
    }

    // DRIVER ACCEPTANCE
    fun recordDriverAcceptance(workOrderId: String, isAccepted: Boolean, notes: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.recordDriverAcceptance(
                workOrderId = workOrderId,
                isAccepted = isAccepted,
                notes = notes,
                driverUser = user
            )
            _refreshTrigger.value = System.currentTimeMillis()
            if (isAccepted) {
                showMessage("คนขับตรวจรับผ่าน รอช่างยืนยันปล่อยรถ")
            } else {
                showMessage("คนขับส่งกลับซ่อมใหม่: $notes")
            }
        }
    }

    // MECHANIC FINAL RELEASE
    fun mechanicFinalRelease(workOrderId: String, newAuthStatus: VehicleAuthStatus) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.confirmMechanicFinalRelease(
                workOrderId = workOrderId,
                mechanicUser = user,
                newAuthStatus = newAuthStatus
            )
            _refreshTrigger.value = System.currentTimeMillis()
            showMessage("ช่างยืนยันปล่อยรถเรียบร้อย จบงานซ่อม")
        }
    }

    // GATE RELEASE CHECK (พี่ก้อม)
    suspend fun checkGateRelease(vehicleId: String, pairId: String?): ReleaseEligibility {
        return repository.checkReleaseEligibility(vehicleId, pairId)
    }

    fun confirmGateRelease(vehicleId: String, pairId: String?, driverId: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.confirmDispatchRelease(vehicleId, pairId, driverId, user)
            _refreshTrigger.value = System.currentTimeMillis()
            showMessage("พี่ก้อมยืนยันปล่อยรถและจ่ายเบี้ยเลี้ยงสำเร็จ!")
        }
    }

    fun confirmPaperFallbackRelease(
        vehicleId: String,
        pairId: String?,
        driverId: String,
        paperDocNo: String,
        paperAuthorizer: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.confirmPaperFallbackRelease(
                vehicleId = vehicleId,
                pairId = pairId,
                driverId = driverId,
                paperDocNo = paperDocNo,
                paperAuthorizedBy = paperAuthorizer,
                notes = notes,
                actorUser = user
            )
            _refreshTrigger.value = System.currentTimeMillis()
            showMessage("บันทึกย้อนหลังจากเอกสารกระดาษสำเร็จ (ใบปล่อยรถ: $paperDocNo)")
        }
    }

    // SPARE TRUCK ACTIONS (โต้ง & เหวียน)
    fun assignSpareTruck(driverId: String, spareVehicleId: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.assignSpareTruck(driverId, spareVehicleId, user)
            _refreshTrigger.value = System.currentTimeMillis()
            showMessage("จัดรถสำรองให้คนขับเรียบร้อย")
        }
    }

    fun requestSpareTruckReturn(assignmentId: String, mileage: String?, mileageBroken: Boolean, symptoms: String?) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.initiateSpareTruckReturn(assignmentId, user, mileage, mileageBroken, symptoms)
            _refreshTrigger.value = System.currentTimeMillis()
            showMessage("ส่งคำขอคืนรถสำรองแล้ว รอโต้ง/เหวียนยืนยันรับคืน")
        }
    }

    fun confirmSpareTruckReturn(assignmentId: String, notes: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.confirmSpareTruckReturn(assignmentId, user, notes)
            _refreshTrigger.value = System.currentTimeMillis()
            showMessage("ยืนยันรับคืนรถสำรองสำเร็จ การมอบหมายกลับเป็นรถประจำ")
        }
    }
}
