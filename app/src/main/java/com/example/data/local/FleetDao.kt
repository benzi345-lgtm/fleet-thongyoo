package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AssignmentEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.BreakdownReportEntity
import com.example.data.model.DispatchReleaseEntity
import com.example.data.model.InspectionEntity
import com.example.data.model.InspectionItemEntity
import com.example.data.model.SystemConfigEntity
import com.example.data.model.UserEntity
import com.example.data.model.VehicleEntity
import com.example.data.model.VehiclePairEntity
import com.example.data.model.WorkOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FleetDao {

    // USERS
    @Query("SELECT * FROM users ORDER BY displayOrder ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdDirect(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    // VEHICLES
    @Query("SELECT * FROM vehicles ORDER BY displayCode ASC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    fun getVehicleById(id: String): Flow<VehicleEntity?>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getVehicleByIdDirect(id: String): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<VehicleEntity>)

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET authStatus = :authStatus, authReason = :authReason, authUpdatedBy = :updatedBy, authUpdatedAt = :updatedAt WHERE id = :id")
    suspend fun updateVehicleAuth(id: String, authStatus: String, authReason: String?, updatedBy: String, updatedAt: Long)

    @Query("UPDATE vehicles SET currentStatus = :status WHERE id = :id")
    suspend fun updateVehicleStatus(id: String, status: String)

    // VEHICLE PAIRS
    @Query("SELECT * FROM vehicle_pairs WHERE isActive = 1")
    fun getAllActivePairs(): Flow<List<VehiclePairEntity>>

    @Query("SELECT * FROM vehicle_pairs WHERE id = :id LIMIT 1")
    fun getPairById(id: String): Flow<VehiclePairEntity?>

    @Query("SELECT * FROM vehicle_pairs WHERE id = :id LIMIT 1")
    suspend fun getPairByIdDirect(id: String): VehiclePairEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPairs(pairs: List<VehiclePairEntity>)

    // ASSIGNMENTS
    @Query("SELECT * FROM assignments WHERE driverId = :driverId AND status != 'RETURNED' ORDER BY assignedAt DESC LIMIT 1")
    fun getActiveAssignmentForDriver(driverId: String): Flow<AssignmentEntity?>

    @Query("SELECT * FROM assignments WHERE driverId = :driverId AND status != 'RETURNED' ORDER BY assignedAt DESC LIMIT 1")
    suspend fun getActiveAssignmentForDriverDirect(driverId: String): AssignmentEntity?

    @Query("SELECT * FROM assignments WHERE status != 'RETURNED' ORDER BY assignedAt DESC")
    fun getAllActiveAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE id = :id LIMIT 1")
    suspend fun getAssignmentByIdDirect(id: String): AssignmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    // INSPECTIONS
    @Query("SELECT * FROM inspections WHERE driverId = :driverId AND workDate = :workDate AND vehicleId = :vehicleId LIMIT 1")
    fun getInspectionForDriverToday(driverId: String, workDate: String, vehicleId: String): Flow<InspectionEntity?>

    @Query("SELECT * FROM inspections WHERE vehicleId = :vehicleId AND workDate = :workDate LIMIT 1")
    fun getInspectionForVehicleToday(vehicleId: String, workDate: String): Flow<InspectionEntity?>

    @Query("SELECT * FROM inspections WHERE vehicleId = :vehicleId AND workDate = :workDate LIMIT 1")
    suspend fun getInspectionForVehicleTodayDirect(vehicleId: String, workDate: String): InspectionEntity?

    @Query("SELECT * FROM inspections WHERE id = :id LIMIT 1")
    fun getInspectionById(id: String): Flow<InspectionEntity?>

    @Query("SELECT * FROM inspections WHERE id = :id LIMIT 1")
    suspend fun getInspectionByIdDirect(id: String): InspectionEntity?

    @Query("SELECT * FROM inspections WHERE workDate = :workDate ORDER BY createdAt DESC")
    fun getAllInspectionsToday(workDate: String): Flow<List<InspectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity)

    @Update
    suspend fun updateInspection(inspection: InspectionEntity)

    // INSPECTION ITEMS
    @Query("SELECT * FROM inspection_items WHERE inspectionId = :inspectionId ORDER BY categoryId ASC, itemKey ASC")
    fun getInspectionItems(inspectionId: String): Flow<List<InspectionItemEntity>>

    @Query("SELECT * FROM inspection_items WHERE inspectionId = :inspectionId")
    suspend fun getInspectionItemsDirect(inspectionId: String): List<InspectionItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspectionItems(items: List<InspectionItemEntity>)

    @Update
    suspend fun updateInspectionItem(item: InspectionItemEntity)

    // BREAKDOWNS
    @Query("SELECT * FROM breakdown_reports ORDER BY createdAt DESC")
    fun getAllBreakdowns(): Flow<List<BreakdownReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreakdown(report: BreakdownReportEntity)

    @Update
    suspend fun updateBreakdown(report: BreakdownReportEntity)

    // WORK ORDERS
    @Query("SELECT * FROM work_orders ORDER BY updatedAt DESC")
    fun getAllWorkOrders(): Flow<List<WorkOrderEntity>>

    @Query("SELECT * FROM work_orders WHERE status != 'COMPLETED' ORDER BY updatedAt DESC")
    fun getActiveWorkOrders(): Flow<List<WorkOrderEntity>>

    @Query("SELECT * FROM work_orders WHERE vehicleId = :vehicleId AND status != 'COMPLETED' ORDER BY updatedAt DESC")
    fun getActiveWorkOrdersForVehicle(vehicleId: String): Flow<List<WorkOrderEntity>>

    @Query("SELECT * FROM work_orders WHERE vehicleId = :vehicleId AND status != 'COMPLETED' ORDER BY updatedAt DESC")
    suspend fun getActiveWorkOrdersForVehicleDirect(vehicleId: String): List<WorkOrderEntity>

    @Query("SELECT * FROM work_orders WHERE id = :id LIMIT 1")
    fun getWorkOrderById(id: String): Flow<WorkOrderEntity?>

    @Query("SELECT * FROM work_orders WHERE id = :id LIMIT 1")
    suspend fun getWorkOrderByIdDirect(id: String): WorkOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrder(order: WorkOrderEntity)

    @Update
    suspend fun updateWorkOrder(order: WorkOrderEntity)

    // AUDIT LOGS
    @Query("SELECT * FROM audit_logs ORDER BY createdAt DESC LIMIT 150")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // DISPATCH RELEASES
    @Query("SELECT * FROM dispatch_releases WHERE vehicleId = :vehicleId AND workDate = :workDate LIMIT 1")
    fun getReleaseForVehicleToday(vehicleId: String, workDate: String): Flow<DispatchReleaseEntity?>

    @Query("SELECT * FROM dispatch_releases WHERE vehicleId = :vehicleId AND workDate = :workDate LIMIT 1")
    suspend fun getReleaseForVehicleTodayDirect(vehicleId: String, workDate: String): DispatchReleaseEntity?

    @Query("SELECT * FROM dispatch_releases WHERE workDate = :workDate ORDER BY releasedAt DESC")
    fun getAllReleasesToday(workDate: String): Flow<List<DispatchReleaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispatchRelease(release: DispatchReleaseEntity)

    // SYSTEM CONFIGS
    @Query("SELECT * FROM system_configs")
    fun getAllConfigs(): Flow<List<SystemConfigEntity>>

    @Query("SELECT configValue FROM system_configs WHERE configKey = :key LIMIT 1")
    suspend fun getConfigValueDirect(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: SystemConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigs(configs: List<SystemConfigEntity>)
}
