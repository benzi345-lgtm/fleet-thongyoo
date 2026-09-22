package com.example.data.local

import com.example.data.model.AssignmentEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.SystemConfigEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleAuthStatus
import com.example.data.model.VehicleEntity
import com.example.data.model.VehiclePairEntity
import com.example.data.model.VehicleType
import kotlinx.coroutines.flow.first

object DatabaseSeeder {

    suspend fun seedIfEmpty(dao: FleetDao) {
        val existingUsers = dao.getAllUsers().first()
        if (existingUsers.isNotEmpty()) return

        // 1. Users
        val driver1 = UserEntity(
            id = "driver-somchai-01",
            name = "สมชาย ใจซื่อ",
            role = UserRole.DRIVER.name,
            phone = "081-111-0001",
            displayOrder = 1
        )
        val driver2 = UserEntity(
            id = "driver-somporn-02",
            name = "สมพร มุ่งมั่น",
            role = UserRole.DRIVER.name,
            phone = "081-111-0002",
            displayOrder = 2
        )
        val driver3 = UserEntity(
            id = "driver-manop-03",
            name = "มานพ คล่องแคล่ว",
            role = UserRole.DRIVER.name,
            phone = "081-111-0003",
            displayOrder = 3
        )
        val mechanicHead = UserEntity(
            id = "mech-sommai-head",
            name = "ช่างสมหมาย (ช่างเต็ม)",
            role = UserRole.MECHANIC_HEAD.name,
            phone = "081-234-5678",
            displayOrder = 4
        )
        val mechanicField = UserEntity(
            id = "mech-decha-field",
            name = "ช่างเดชา (ช่างสวน)",
            role = UserRole.MECHANIC_FIELD.name,
            phone = "082-345-6789",
            displayOrder = 5
        )
        val gateKom = UserEntity(
            id = "gate-kom",
            name = "พี่ก้อม (ตรวจสอบปล่อยรถ)",
            role = UserRole.GATE_KOM.name,
            phone = "083-456-7890",
            displayOrder = 6
        )
        val dispatcherTong = UserEntity(
            id = "disp-tong",
            name = "โต้ง (จัดคิว/รถสำรอง)",
            role = UserRole.DISPATCHER_TONG.name,
            phone = "084-567-8901",
            displayOrder = 7
        )
        val dispatcherNguyen = UserEntity(
            id = "disp-nguyen",
            name = "เหวียน (จัดคิว/รถสำรอง)",
            role = UserRole.DISPATCHER_NGUYEN.name,
            phone = "085-678-9012",
            displayOrder = 8
        )
        val owner = UserEntity(
            id = "owner-somsak",
            name = "เสี่ยสมศักดิ์ (เจ้าของ)",
            role = UserRole.OWNER.name,
            phone = "089-876-5432",
            displayOrder = 9
        )
        val office = UserEntity(
            id = "office-supha",
            name = "สุภา (ออฟฟิศรับสาย)",
            role = UserRole.OFFICE.name,
            phone = "086-789-0123",
            displayOrder = 10
        )

        dao.insertUsers(
            listOf(
                driver1, driver2, driver3,
                mechanicHead, mechanicField,
                gateKom, dispatcherTong, dispatcherNguyen,
                owner, office
            )
        )

        // 2. Vehicles
        // Note: Mother and Trailer share displayCode "01", but have separate IDs and plates!
        val mother01 = VehicleEntity(
            id = "veh-mother-01",
            displayCode = "01",
            plateNumber = "70-1234 กทม.",
            vehicleType = VehicleType.SEMI_MOTHER.name,
            isSpare = false,
            authStatus = VehicleAuthStatus.NOT_EVALUATED.name
        )
        val trailer01 = VehicleEntity(
            id = "veh-trailer-01",
            displayCode = "01",
            plateNumber = "70-5678 กทม.",
            vehicleType = VehicleType.SEMI_TRAILER.name,
            isSpare = false,
            authStatus = VehicleAuthStatus.NOT_EVALUATED.name
        )
        val truck02 = VehicleEntity(
            id = "veh-truck-02",
            displayCode = "02",
            plateNumber = "71-2345 กทม.",
            vehicleType = VehicleType.TEN_WHEEL.name,
            isSpare = false,
            authStatus = VehicleAuthStatus.NOT_EVALUATED.name
        )
        val truck03 = VehicleEntity(
            id = "veh-truck-03",
            displayCode = "03",
            plateNumber = "72-3456 กทม.",
            vehicleType = VehicleType.SIX_WHEEL.name,
            isSpare = false,
            authStatus = VehicleAuthStatus.NOT_EVALUATED.name
        )
        val spare01 = VehicleEntity(
            id = "veh-spare-01",
            displayCode = "SP-01",
            plateNumber = "79-9999 กทม.",
            vehicleType = VehicleType.TEN_WHEEL.name,
            isSpare = true,
            authStatus = VehicleAuthStatus.NOT_EVALUATED.name
        )

        dao.insertVehicles(listOf(mother01, trailer01, truck02, truck03, spare01))

        // 3. Vehicle Pair
        val pair01 = VehiclePairEntity(
            id = "pair-01",
            motherVehicleId = mother01.id,
            trailerVehicleId = trailer01.id,
            pairCode = "ชุดรถ 01 (หัว 70-1234 / หาง 70-5678)",
            isActive = true
        )
        dao.insertPairs(listOf(pair01))

        // 4. Initial Assignments
        val assign1 = AssignmentEntity(
            id = "assign-01",
            driverId = driver1.id,
            vehicleId = mother01.id,
            pairId = pair01.id,
            isSpareTruck = false
        )
        val assign2 = AssignmentEntity(
            id = "assign-02",
            driverId = driver2.id,
            vehicleId = truck02.id,
            pairId = null,
            isSpareTruck = false
        )
        val assign3 = AssignmentEntity(
            id = "assign-03",
            driverId = driver3.id,
            vehicleId = truck03.id,
            pairId = null,
            isSpareTruck = false
        )
        dao.insertAssignment(assign1)
        dao.insertAssignment(assign2)
        dao.insertAssignment(assign3)

        // 5. System Configurations
        val configs = listOf(
            SystemConfigEntity("mechanic_phone", "081-234-5678", "เบอร์โทรศัพท์ช่างเต็ม (ช่างสมหมาย)"),
            SystemConfigEntity("owner_phone", "089-876-5432", "เบอร์โทรศัพท์เสี่ยสมศักดิ์ (เจ้าของ)"),
            SystemConfigEntity("external_garage_default", "อู่ซ่อมรถบรรทุก สหยนต์ นครปฐม", "อู่ซ่อมภายนอกหลัก"),
            SystemConfigEntity("sync_status", "STANDALONE_DEMO", "สถานะการเชื่อมระบบเดิม: เฟส 1 Standalone Adapter"),
            SystemConfigEntity("template_version", "v1.0-trial", "เวอร์ชันแบบตรวจ 9 กลุ่ม")
        )
        dao.insertConfigs(configs)

        // 6. Initial Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                entityType = "SYSTEM",
                entityId = "INIT",
                action = "INITIALIZE_DATABASE",
                fromStatus = null,
                toStatus = "READY",
                actorId = "system",
                actorName = "ระบบตั้งต้น",
                reason = "โหลดข้อมูลทดสอบบริษัททองอยู่สำเร็จ"
            )
        )
    }
}
