package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String, // from UserRole enum
    val phone: String,
    val displayOrder: Int = 0
)

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val displayCode: String, // e.g. "01", "02", "SP-01" (can be non-unique for mother/trailer)
    val plateNumber: String, // e.g. "70-1234 กทม."
    val vehicleType: String, // VehicleType enum
    val isSpare: Boolean = false,
    val currentStatus: String = "ACTIVE", // ACTIVE, IN_REPAIR, RETIRED
    val authStatus: String = VehicleAuthStatus.NOT_EVALUATED.name,
    val authReason: String? = null,
    val authUpdatedBy: String? = null,
    val authUpdatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vehicle_pairs")
data class VehiclePairEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val motherVehicleId: String,
    val trailerVehicleId: String,
    val pairCode: String, // e.g. "ชุดรถ 01 (70-1234 / 70-5678)"
    val isActive: Boolean = true,
    val effectiveFrom: Long = System.currentTimeMillis()
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val driverId: String,
    val vehicleId: String, // If single truck or mother truck
    val pairId: String? = null, // If semi-trailer
    val isSpareTruck: Boolean = false,
    val regularVehicleId: String? = null, // Reference to original truck when using spare
    val status: String = "ACTIVE", // ACTIVE, RETURN_PENDING, RETURNED
    val assignedAt: Long = System.currentTimeMillis(),
    val returnedAt: Long? = null,
    val returnedConfirmedBy: String? = null,
    val returnNotes: String? = null
)

@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val workDate: String, // YYYY-MM-DD (Asia/Bangkok)
    val vehicleId: String,
    val pairId: String? = null,
    val driverId: String,
    val templateVersion: String = "v1.0-trial",
    val isComplete: Boolean = false,
    val hasIssues: Boolean = false,
    val hasPendingMechanicCheck: Boolean = false,
    val mileage: String? = null,
    val mileageBroken: Boolean = false,
    val isPaperImport: Boolean = false,
    val paperDocNo: String? = null,
    val paperAuthorizedBy: String? = null,
    val paperEventTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "STANDALONE_LOCAL"
)

@Entity(tableName = "inspection_items")
data class InspectionItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val inspectionId: String,
    val categoryId: Int, // 1 to 9
    val categoryName: String,
    val itemKey: String,
    val itemTitle: String,
    val targetVehicleId: String, // Linked to exact mother or trailer ID
    val targetLabel: String, // e.g. "รถแม่", "รถลูก", "ทั่วไป"
    val result: String? = null, // CheckResult (NORMAL, ISSUE, MECHANIC_CHECK) or null if not checked yet
    val notes: String? = null,
    val photoUri: String? = null,
    val inspectionMethod: String? = null
)

@Entity(tableName = "breakdown_reports")
data class BreakdownReportEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val workDate: String,
    val vehicleId: String,
    val driverId: String,
    val reportedByRole: String = UserRole.DRIVER.name,
    val recordedByUserId: String? = null, // for office recording on behalf
    val symptomDescription: String,
    val vehicleSituation: String, // "STOPPED_SAFE" vs "MOVING_OBSERVED"
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationAccuracy: Float? = null,
    val locationText: String,
    val photoUri: String? = null,
    val status: String = "REPORTED", // REPORTED, TRIAGED, RESOLVED
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "STANDALONE_LOCAL"
)

@Entity(tableName = "work_orders")
data class WorkOrderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val breakdownId: String? = null,
    val vehicleId: String,
    val status: String = WorkOrderStatus.WAITING_INSPECTION.name,
    val authStatus: String = VehicleAuthStatus.NOT_EVALUATED.name,
    val authConditions: String? = null,
    val assignedMechanicId: String? = null,
    val recordedByUserId: String? = null, // office recording on phone
    val reportedByUserId: String? = null,
    val authorizedByUserId: String? = null,
    val repairSummary: String? = null,
    val partsReplaced: String? = null, // e.g. "เปลี่ยนสายลมเบรก" or "ไม่มีการเปลี่ยนอะไหล่"
    val repairPhotoUri: String? = null,
    val driverAccepted: Boolean? = null,
    val driverAcceptanceNotes: String? = null,
    val driverAcceptedAt: Long? = null,
    val driverAcceptorId: String? = null,
    val releaseConfirmedByMechanicId: String? = null,
    val releaseConfirmedAt: Long? = null,
    val externalGarageName: String? = null,
    val officeFollowUpAssignee: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "STANDALONE_LOCAL"
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val entityType: String, // "VEHICLE", "WORK_ORDER", "INSPECTION", "DISPATCH"
    val entityId: String,
    val action: String,
    val fromStatus: String? = null,
    val toStatus: String? = null,
    val actorId: String,
    val actorName: String,
    val reportedBy: String? = null,
    val authorizedBy: String? = null,
    val reason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dispatch_releases")
data class DispatchReleaseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val workDate: String,
    val vehicleId: String,
    val pairId: String? = null,
    val driverId: String,
    val releasedById: String,
    val releasedByName: String,
    val isPaperFallback: Boolean = false,
    val paperDocNo: String? = null,
    val paperAuthorizedBy: String? = null,
    val notes: String? = null,
    val releasedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_configs")
data class SystemConfigEntity(
    @PrimaryKey val configKey: String,
    val configValue: String,
    val description: String? = null
)
