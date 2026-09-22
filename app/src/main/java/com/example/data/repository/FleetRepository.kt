package com.example.data.repository

import com.example.data.local.FleetDao
import com.example.data.model.AssignmentEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.BreakdownReportEntity
import com.example.data.model.CheckResult
import com.example.data.model.DispatchReleaseEntity
import com.example.data.model.InspectionEntity
import com.example.data.model.InspectionItemEntity
import com.example.data.model.InspectionTemplate
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleAuthStatus
import com.example.data.model.VehicleEntity
import com.example.data.model.VehiclePairEntity
import com.example.data.model.WorkOrderEntity
import com.example.data.model.WorkOrderStatus
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class ReleaseEligibility(
    val isEligible: Boolean,
    val inspectionCompleted: Boolean,
    val hasUnresolvedMechanicChecks: Boolean,
    val hasProhibitedConstraint: Boolean,
    val isGarageTransitOnly: Boolean,
    val hasActiveUnresolvedDefect: Boolean,
    val blockingReasons: List<String>
)

class FleetRepository(private val dao: FleetDao) {

    // Current Active User Simulation (Stored in memory for trial period session)
    private val _currentUserId = MutableStateFlow("driver-somchai-01")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    fun setCurrentUser(userId: String) {
        _currentUserId.value = userId
    }

    // USERS
    fun getAllUsers(): Flow<List<UserEntity>> = dao.getAllUsers()
    fun getUser(id: String): Flow<UserEntity?> = dao.getUserById(id)
    suspend fun getUserDirect(id: String): UserEntity? = dao.getUserByIdDirect(id)

    // VEHICLES & PAIRS
    fun getAllVehicles(): Flow<List<VehicleEntity>> = dao.getAllVehicles()
    fun getVehicle(id: String): Flow<VehicleEntity?> = dao.getVehicleById(id)
    suspend fun getVehicleDirect(id: String): VehicleEntity? = dao.getVehicleByIdDirect(id)
    fun getAllActivePairs(): Flow<List<VehiclePairEntity>> = dao.getAllActivePairs()
    fun getPair(id: String): Flow<VehiclePairEntity?> = dao.getPairById(id)
    suspend fun getPairDirect(id: String): VehiclePairEntity? = dao.getPairByIdDirect(id)

    // ASSIGNMENTS
    fun getActiveAssignmentForDriver(driverId: String): Flow<AssignmentEntity?> =
        dao.getActiveAssignmentForDriver(driverId)

    fun getAllActiveAssignments(): Flow<List<AssignmentEntity>> = dao.getAllActiveAssignments()

    // INSPECTIONS
    fun getInspectionForDriverToday(driverId: String, workDate: String, vehicleId: String): Flow<InspectionEntity?> =
        dao.getInspectionForDriverToday(driverId, workDate, vehicleId)

    fun getInspectionForVehicleToday(vehicleId: String, workDate: String): Flow<InspectionEntity?> =
        dao.getInspectionForVehicleToday(vehicleId, workDate)

    fun getInspectionItems(inspectionId: String): Flow<List<InspectionItemEntity>> =
        dao.getInspectionItems(inspectionId)

    fun getAllInspectionsToday(workDate: String): Flow<List<InspectionEntity>> =
        dao.getAllInspectionsToday(workDate)

    fun getAllReleasesToday(workDate: String): Flow<List<DispatchReleaseEntity>> =
        dao.getAllReleasesToday(workDate)

    /**
     * Initializes or returns existing inspection for driver on current vehicle today.
     */
    suspend fun getOrCreateTodayInspection(
        driverId: String,
        vehicleId: String,
        pairId: String?,
        isSemiTrailer: Boolean,
        trailerId: String?
    ): InspectionEntity {
        val workDate = DateUtils.getWorkDate()
        val existing = dao.getInspectionForVehicleTodayDirect(vehicleId, workDate)
        if (existing != null) return existing

        val newInspection = InspectionEntity(
            id = UUID.randomUUID().toString(),
            workDate = workDate,
            vehicleId = vehicleId,
            pairId = pairId,
            driverId = driverId,
            templateVersion = "v1.0-trial",
            isComplete = false,
            hasIssues = false,
            hasPendingMechanicCheck = false
        )
        dao.insertInspection(newInspection)

        // Generate the 9-category checklist items
        val items = InspectionTemplate.createItemsForInspection(
            inspectionId = newInspection.id,
            isSemiTrailer = isSemiTrailer,
            motherVehicleId = vehicleId,
            trailerVehicleId = trailerId
        )
        dao.insertInspectionItems(items)

        return newInspection
    }

    suspend fun updateInspectionItemResult(
        itemId: String,
        result: CheckResult,
        notes: String?,
        photoUri: String?
    ) {
        val items = dao.getInspectionItemsDirect(itemId) // or we query directly
        // Let's update by querying inspection item
    }

    suspend fun updateInspectionItem(item: InspectionItemEntity) {
        dao.updateInspectionItem(item)
    }

    suspend fun submitInspection(
        inspectionId: String,
        mileage: String?,
        mileageBroken: Boolean,
        driverUser: UserEntity
    ): InspectionEntity? {
        val inspection = dao.getInspectionByIdDirect(inspectionId) ?: return null
        val items = dao.getInspectionItemsDirect(inspectionId)

        val anyUnchecked = items.any { it.result == null }
        val hasIssues = items.any { it.result == CheckResult.ISSUE.name }
        val hasMechanicCheck = items.any { it.result == CheckResult.MECHANIC_CHECK.name }

        val updated = inspection.copy(
            isComplete = !anyUnchecked,
            hasIssues = hasIssues,
            hasPendingMechanicCheck = hasMechanicCheck,
            mileage = if (mileageBroken) null else mileage,
            mileageBroken = mileageBroken,
            syncStatus = "STANDALONE_LOCAL"
        )
        dao.updateInspection(updated)

        // If issues or mechanic checks found, automatically create a WorkOrder for review
        if (hasIssues || hasMechanicCheck) {
            val defectiveItems = items.filter { it.result == CheckResult.ISSUE.name || it.result == CheckResult.MECHANIC_CHECK.name }
            val summary = defectiveItems.joinToString(", ") { "${it.categoryName}: ${it.itemTitle} (${it.notes ?: "ไม่มีหมายเหตุ"})" }

            val wo = WorkOrderEntity(
                id = UUID.randomUUID().toString(),
                vehicleId = inspection.vehicleId,
                status = if (hasIssues) WorkOrderStatus.INSPECTED_PENDING.name else WorkOrderStatus.WAITING_INSPECTION.name,
                authStatus = VehicleAuthStatus.NOT_EVALUATED.name,
                reportedByUserId = driverUser.id,
                repairSummary = "รายงานจากแบบตรวจก่อนออก: $summary",
                createdAt = System.currentTimeMillis()
            )
            dao.insertWorkOrder(wo)

            // Audit
            dao.insertAuditLog(
                AuditLogEntity(
                    entityType = "INSPECTION",
                    entityId = inspectionId,
                    action = "SUBMIT_WITH_DEFECTS",
                    fromStatus = "DRAFT",
                    toStatus = "SUBMITTED",
                    actorId = driverUser.id,
                    actorName = driverUser.name,
                    reason = "พบรายการผิดปกติหรือต้องการให้ช่างตรวจ: $summary"
                )
            )
        } else {
            dao.insertAuditLog(
                AuditLogEntity(
                    entityType = "INSPECTION",
                    entityId = inspectionId,
                    action = "SUBMIT_NORMAL",
                    fromStatus = "DRAFT",
                    toStatus = "COMPLETED",
                    actorId = driverUser.id,
                    actorName = driverUser.name,
                    reason = "ตรวจก่อนออกครบถ้วน ทุกรายการปกติ"
                )
            )
        }

        return updated
    }

    // BREAKDOWNS
    fun getAllBreakdowns(): Flow<List<BreakdownReportEntity>> = dao.getAllBreakdowns()

    suspend fun reportBreakdown(
        vehicleId: String,
        driverId: String,
        symptom: String,
        situation: String,
        latitude: Double?,
        longitude: Double?,
        locationAccuracy: Float?,
        locationText: String,
        photoUri: String?,
        actorUser: UserEntity,
        isRecordedByOffice: Boolean = false
    ): BreakdownReportEntity {
        val workDate = DateUtils.getWorkDate()
        val report = BreakdownReportEntity(
            id = UUID.randomUUID().toString(),
            workDate = workDate,
            vehicleId = vehicleId,
            driverId = driverId,
            reportedByRole = if (isRecordedByOffice) UserRole.OFFICE.name else UserRole.DRIVER.name,
            recordedByUserId = if (isRecordedByOffice) actorUser.id else null,
            symptomDescription = symptom,
            vehicleSituation = situation,
            latitude = latitude,
            longitude = longitude,
            locationAccuracy = locationAccuracy,
            locationText = locationText,
            photoUri = photoUri,
            status = "REPORTED"
        )
        dao.insertBreakdown(report)

        // Automatically create a WorkOrder in WAITING_INSPECTION
        val wo = WorkOrderEntity(
            id = UUID.randomUUID().toString(),
            breakdownId = report.id,
            vehicleId = vehicleId,
            status = WorkOrderStatus.WAITING_INSPECTION.name,
            authStatus = VehicleAuthStatus.NOT_EVALUATED.name,
            reportedByUserId = driverId,
            recordedByUserId = if (isRecordedByOffice) actorUser.id else null,
            repairSummary = "แจ้งเสียระหว่างทาง: $symptom (สถานการณ์: $situation, พิกัด: $locationText)"
        )
        dao.insertWorkOrder(wo)

        dao.insertAuditLog(
            AuditLogEntity(
                entityType = "BREAKDOWN",
                entityId = report.id,
                action = "REPORT_BREAKDOWN",
                fromStatus = null,
                toStatus = "REPORTED",
                actorId = actorUser.id,
                actorName = actorUser.name,
                reportedBy = driverId,
                reason = "แจ้งเสีย: $symptom สถานที่: $locationText"
            )
        )

        return report
    }

    // WORK ORDERS & MECHANIC WORKFLOW
    fun getAllWorkOrders(): Flow<List<WorkOrderEntity>> = dao.getAllWorkOrders()
    fun getActiveWorkOrders(): Flow<List<WorkOrderEntity>> = dao.getActiveWorkOrders()
    fun getWorkOrder(id: String): Flow<WorkOrderEntity?> = dao.getWorkOrderById(id)

    suspend fun updateWorkOrderEvaluation(
        workOrderId: String,
        authStatus: VehicleAuthStatus,
        authConditions: String?,
        nextStatus: WorkOrderStatus,
        mechanicUser: UserEntity,
        reason: String
    ) {
        val wo = dao.getWorkOrderByIdDirect(workOrderId) ?: return
        val updated = wo.copy(
            authStatus = authStatus.name,
            authConditions = authConditions,
            status = nextStatus.name,
            assignedMechanicId = mechanicUser.id,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateWorkOrder(updated)

        // Update Vehicle Authorization Status
        dao.updateVehicleAuth(
            id = wo.vehicleId,
            authStatus = authStatus.name,
            authReason = authConditions ?: reason,
            updatedBy = mechanicUser.name,
            updatedAt = System.currentTimeMillis()
        )

        dao.insertAuditLog(
            AuditLogEntity(
                entityType = "WORK_ORDER",
                entityId = workOrderId,
                action = "MECHANIC_EVALUATE",
                fromStatus = wo.status,
                toStatus = nextStatus.name,
                actorId = mechanicUser.id,
                actorName = mechanicUser.name,
                authorizedBy = mechanicUser.name,
                reason = "ประเมินสถานะอนุญาต: ${authStatus.thaiLabel}, เงื่อนไข: ${authConditions ?: reason}"
            )
        )
    }

    suspend fun recordRepairCompletion(
        workOrderId: String,
        repairSummary: String,
        partsReplaced: String,
        photoUri: String?,
        mechanicUser: UserEntity
    ) {
        val wo = dao.getWorkOrderByIdDirect(workOrderId) ?: return
        val updated = wo.copy(
            repairSummary = repairSummary,
            partsReplaced = partsReplaced,
            repairPhotoUri = photoUri,
            status = WorkOrderStatus.REPAIRED_WAITING_DRIVER.name,
            assignedMechanicId = mechanicUser.id,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateWorkOrder(updated)

        dao.insertAuditLog(
            AuditLogEntity(
                entityType = "WORK_ORDER",
                entityId = workOrderId,
                action = "REPAIR_COMPLETED",
                fromStatus = wo.status,
                toStatus = WorkOrderStatus.REPAIRED_WAITING_DRIVER.name,
                actorId = mechanicUser.id,
                actorName = mechanicUser.name,
                reason = "ซ่อมเสร็จสิ้น: $repairSummary, อะไหล่: $partsReplaced (รอคนขับตรวจรับ)"
            )
        )
    }

    suspend fun recordDriverAcceptance(
        workOrderId: String,
        isAccepted: Boolean,
        notes: String,
        driverUser: UserEntity
    ) {
        val wo = dao.getWorkOrderByIdDirect(workOrderId) ?: return
        val nextStatus = if (isAccepted) {
            WorkOrderStatus.DRIVER_ACCEPTED_WAITING_MECHANIC.name
        } else {
            WorkOrderStatus.IN_REPAIR.name // Rejected by driver: returns back to repair!
        }

        val updated = wo.copy(
            driverAccepted = isAccepted,
            driverAcceptanceNotes = notes,
            driverAcceptedAt = System.currentTimeMillis(),
            driverAcceptorId = driverUser.id,
            status = nextStatus,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateWorkOrder(updated)

        dao.insertAuditLog(
            AuditLogEntity(
                entityType = "WORK_ORDER",
                entityId = workOrderId,
                action = if (isAccepted) "DRIVER_ACCEPTED" else "DRIVER_REJECTED",
                fromStatus = wo.status,
                toStatus = nextStatus,
                actorId = driverUser.id,
                actorName = driverUser.name,
                reason = if (isAccepted) "คนขับตรวจรับผ่าน: $notes" else "คนขับตรวจรับไม่ผ่าน ให้กลับไปซ่อม: $notes"
            )
        )
    }

    suspend fun confirmMechanicFinalRelease(
        workOrderId: String,
        mechanicUser: UserEntity,
        newAuthStatus: VehicleAuthStatus
    ) {
        val wo = dao.getWorkOrderByIdDirect(workOrderId) ?: return
        val updated = wo.copy(
            status = WorkOrderStatus.COMPLETED.name,
            releaseConfirmedByMechanicId = mechanicUser.id,
            releaseConfirmedAt = System.currentTimeMillis(),
            authStatus = newAuthStatus.name,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateWorkOrder(updated)

        // Update Vehicle Auth
        dao.updateVehicleAuth(
            id = wo.vehicleId,
            authStatus = newAuthStatus.name,
            authReason = "ช่างยืนยันปล่อยรถหลังจบงานซ่อม",
            updatedBy = mechanicUser.name,
            updatedAt = System.currentTimeMillis()
        )

        dao.insertAuditLog(
            AuditLogEntity(
                entityType = "WORK_ORDER",
                entityId = workOrderId,
                action = "MECHANIC_CONFIRM_RELEASE",
                fromStatus = wo.status,
                toStatus = WorkOrderStatus.COMPLETED.name,
                actorId = mechanicUser.id,
                actorName = mechanicUser.name,
                authorizedBy = mechanicUser.name,
                reason = "ช่างยืนยันปล่อยรถหลังคนขับตรวจรับผ่าน สถานะรถ: ${newAuthStatus.thaiLabel}"
            )
        )
    }

    // DISPATCH RELEASE ELIGIBILITY (พี่ก้อม)
    suspend fun checkReleaseEligibility(
        vehicleId: String,
        pairId: String?,
        workDate: String = DateUtils.getWorkDate()
    ): ReleaseEligibility {
        val reasons = mutableListOf<String>()

        // 1. Pre-trip inspection check
        val inspection = dao.getInspectionForVehicleTodayDirect(vehicleId, workDate)
        var inspectionCompleted = false
        var hasUnresolvedMechanicChecks = false

        if (inspection == null) {
            reasons.add("ยังไม่ได้ทำแบบตรวจก่อนออกของวันทำงานวันนี้ ($workDate)")
        } else {
            if (!inspection.isComplete) {
                reasons.add("แบบตรวจก่อนออกยังทำไม่ครบทุกข้อ")
            } else {
                inspectionCompleted = true
            }

            if (inspection.hasPendingMechanicCheck) {
                hasUnresolvedMechanicChecks = true
                reasons.add("มีรายการที่ระบุ 'ให้ช่างช่วยตรวจ' ค้างอยู่ ยังไม่ได้รับการประเมินจากช่าง")
            }
        }

        // 2. Vehicle and trailer authorization check
        val mother = dao.getVehicleByIdDirect(vehicleId)
        var hasProhibited = false
        var isGarageTransitOnly = false

        if (mother?.authStatus == VehicleAuthStatus.PROHIBITED.name) {
            hasProhibited = true
            reasons.add("รถแม่ (${mother.plateNumber}) ติดคำสั่ง 'ห้ามใช้งาน' โดยช่าง: ${mother.authReason ?: "-"}")
        } else if (mother?.authStatus == VehicleAuthStatus.GARAGE_TRANSIT_ONLY.name) {
            isGarageTransitOnly = true
            reasons.add("รถแม่ (${mother.plateNumber}) ได้รับอนุญาต 'เฉพาะเดินทางไปอู่' ห้ามปล่อยวิ่งรับ–ส่งน้ำมัน")
        }

        // Check trailer if semi
        if (pairId != null) {
            val pair = dao.getPairByIdDirect(pairId)
            if (pair != null) {
                val trailer = dao.getVehicleByIdDirect(pair.trailerVehicleId)
                if (trailer?.authStatus == VehicleAuthStatus.PROHIBITED.name) {
                    hasProhibited = true
                    reasons.add("หางบรรทุกน้ำมัน (${trailer.plateNumber}) ติดคำสั่ง 'ห้ามใช้งาน' โดยช่าง: ${trailer.authReason ?: "-"}")
                } else if (trailer?.authStatus == VehicleAuthStatus.GARAGE_TRANSIT_ONLY.name) {
                    isGarageTransitOnly = true
                    reasons.add("หางบรรทุกน้ำมัน (${trailer.plateNumber}) ได้รับอนุญาต 'เฉพาะเดินทางไปอู่' ห้ามปล่อยรับส่งน้ำมัน")
                }
            }
        }

        // 3. Active defects
        val activeWorkOrders = dao.getActiveWorkOrdersForVehicleDirect(vehicleId)
        val unresolvedDefects = activeWorkOrders.filter { it.status != WorkOrderStatus.COMPLETED.name }
        var hasActiveDefect = false
        if (unresolvedDefects.isNotEmpty()) {
            hasActiveDefect = true
            val titles = unresolvedDefects.joinToString(", ") { "${it.repairSummary} (${it.status})" }
            reasons.add("มีใบงานซ่อมยังไม่เสร็จสิ้น: $titles")
        }

        val isEligible = inspectionCompleted &&
                !hasUnresolvedMechanicChecks &&
                !hasProhibited &&
                !isGarageTransitOnly &&
                !hasActiveDefect

        return ReleaseEligibility(
            isEligible = isEligible,
            inspectionCompleted = inspectionCompleted,
            hasUnresolvedMechanicChecks = hasUnresolvedMechanicChecks,
            hasProhibitedConstraint = hasProhibited,
            isGarageTransitOnly = isGarageTransitOnly,
            hasActiveUnresolvedDefect = hasActiveDefect,
            blockingReasons = reasons
        )
    }

    suspend fun confirmDispatchRelease(
        vehicleId: String,
        pairId: String?,
        driverId: String,
        gateUser: UserEntity
    ): DispatchReleaseEntity {
        val workDate = DateUtils.getWorkDate()
        val release = DispatchReleaseEntity(
            id = UUID.randomUUID().toString(),
            workDate = workDate,
            vehicleId = vehicleId,
            pairId = pairId,
            driverId = driverId,
            releasedById = gateUser.id,
            releasedByName = gateUser.name,
            isPaperFallback = false,
            releasedAt = System.currentTimeMillis()
        )
        dao.insertDispatchRelease(release)

        dao.insertAuditLog(
            AuditLogEntity(
                entityType = "DISPATCH",
                entityId = release.id,
                action = "GATE_RELEASE",
                fromStatus = "WAITING_RELEASE",
                toStatus = "RELEASED",
                actorId = gateUser.id,
                actorName = gateUser.name,
                authorizedBy = gateUser.name,
                reason = "พี่ก้อมตรวจสอบความครบถ้วนผ่านเกณฑ์ อนุมัติปล่อยรถและจ่ายเบี้ยเลี้ยง"
            )
        )

        return release
    }

    suspend fun confirmPaperFallbackRelease(
        vehicleId: String,
        pairId: String?,
        driverId: String,
        paperDocNo: String,
        paperAuthorizedBy: String,
        notes: String?,
        actorUser: UserEntity
    ): DispatchReleaseEntity {
        val workDate = DateUtils.getWorkDate()
        val release = DispatchReleaseEntity(
            id = UUID.randomUUID().toString(),
            workDate = workDate,
            vehicleId = vehicleId,
            pairId = pairId,
            driverId = driverId,
            releasedById = actorUser.id,
            releasedByName = actorUser.name,
            isPaperFallback = true,
            paperDocNo = paperDocNo,
            paperAuthorizedBy = paperAuthorizedBy,
            notes = notes,
            releasedAt = System.currentTimeMillis()
        )
        dao.insertDispatchRelease(release)

        dao.insertAuditLog(
            AuditLogEntity(
                entityType = "DISPATCH",
                entityId = release.id,
                action = "PAPER_FALLBACK_RELEASE",
                fromStatus = "PAPER_OFFLINE",
                toStatus = "RELEASED_BACKDATED",
                actorId = actorUser.id,
                actorName = actorUser.name,
                authorizedBy = paperAuthorizedBy,
                reason = "บันทึกย้อนหลังจากเอกสารกระดาษเลขที่: $paperDocNo, ผู้อนุญาตตามกระดาษ: $paperAuthorizedBy"
            )
        )

        return release
    }

    // SPARE TRUCK MANAGEMENT (โต้ง & เหวียน)
    suspend fun assignSpareTruck(
        driverId: String,
        spareVehicleId: String,
        dispatcherUser: UserEntity
    ) {
        val currentAssignment = dao.getActiveAssignmentForDriverDirect(driverId)

        val newAssignment = AssignmentEntity(
            id = UUID.randomUUID().toString(),
            driverId = driverId,
            vehicleId = spareVehicleId,
            pairId = null,
            isSpareTruck = true,
            regularVehicleId = currentAssignment?.vehicleId,
            status = "ACTIVE",
            assignedAt = System.currentTimeMillis()
        )
        dao.insertAssignment(newAssignment)

        dao.insertAuditLog(
            AuditLogEntity(
                entityType = "ASSIGNMENT",
                entityId = newAssignment.id,
                action = "ASSIGN_SPARE_TRUCK",
                fromStatus = currentAssignment?.vehicleId,
                toStatus = spareVehicleId,
                actorId = dispatcherUser.id,
                actorName = dispatcherUser.name,
                reason = "จัดรถสำรองให้คนขับ"
            )
        )
    }

    suspend fun initiateSpareTruckReturn(
        assignmentId: String,
        driverUser: UserEntity,
        mileage: String?,
        mileageBroken: Boolean,
        symptomsObserved: String?
    ) {
        val assignment = dao.getAssignmentByIdDirect(assignmentId) ?: return
        val updated = assignment.copy(
            status = "RETURN_PENDING",
            returnNotes = "คนขับแจ้งขอคืน: ไมล์ ${if (mileageBroken) "ไมล์เสีย" else mileage ?: "-"}, อาการ: ${symptomsObserved ?: "ไม่มี"}"
        )
        dao.updateAssignment(updated)

        dao.insertAuditLog(
            AuditLogEntity(
                entityType = "ASSIGNMENT",
                entityId = assignmentId,
                action = "DRIVER_REQUEST_RETURN",
                fromStatus = "ACTIVE",
                toStatus = "RETURN_PENDING",
                actorId = driverUser.id,
                actorName = driverUser.name,
                reason = updated.returnNotes
            )
        )
    }

    suspend fun confirmSpareTruckReturn(
        assignmentId: String,
        dispatcherUser: UserEntity,
        confirmNotes: String
    ) {
        val assignment = dao.getAssignmentByIdDirect(assignmentId) ?: return
        val updated = assignment.copy(
            status = "RETURNED",
            returnedAt = System.currentTimeMillis(),
            returnedConfirmedBy = dispatcherUser.name,
            returnNotes = "${assignment.returnNotes ?: ""} | ยืนยันรับคืนโดย: ${dispatcherUser.name} ($confirmNotes)"
        )
        dao.updateAssignment(updated)

        // Reinstate regular vehicle assignment for the driver if present
        if (assignment.regularVehicleId != null) {
            val regularAssign = AssignmentEntity(
                id = UUID.randomUUID().toString(),
                driverId = assignment.driverId,
                vehicleId = assignment.regularVehicleId,
                pairId = if (assignment.regularVehicleId == "veh-mother-01") "pair-01" else null,
                isSpareTruck = false,
                status = "ACTIVE",
                assignedAt = System.currentTimeMillis()
            )
            dao.insertAssignment(regularAssign)
        }

        dao.insertAuditLog(
            AuditLogEntity(
                entityType = "ASSIGNMENT",
                entityId = assignmentId,
                action = "DISPATCHER_CONFIRM_RETURN",
                fromStatus = "RETURN_PENDING",
                toStatus = "RETURNED",
                actorId = dispatcherUser.id,
                actorName = dispatcherUser.name,
                reason = "โต้ง/เหวียน ยืนยันรับคืนรถสำรองเรียบร้อย: $confirmNotes"
            )
        )
    }

    // AUDIT LOGS
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()
}
