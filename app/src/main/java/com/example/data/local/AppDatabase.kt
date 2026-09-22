package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        UserEntity::class,
        VehicleEntity::class,
        VehiclePairEntity::class,
        AssignmentEntity::class,
        InspectionEntity::class,
        InspectionItemEntity::class,
        BreakdownReportEntity::class,
        WorkOrderEntity::class,
        AuditLogEntity::class,
        DispatchReleaseEntity::class,
        SystemConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fleetDao(): FleetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "thongyoo_fleet.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
