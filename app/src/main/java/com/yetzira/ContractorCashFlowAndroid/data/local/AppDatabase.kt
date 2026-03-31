package com.yetzira.ContractorCashFlowAndroid.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ClientDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ExpenseDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.InvoiceDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.LaborDetailsDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity

@Database(
    entities = [
        ProjectEntity::class,
        ExpenseEntity::class,
        InvoiceEntity::class,
        ClientEntity::class,
        LaborDetailsEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun clientDao(): ClientDao
    abstract fun laborDetailsDao(): LaborDetailsDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE projects ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE expenses ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE invoices ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE clients ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE labor_details ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No-op. Version 3 keeps schema stable and reserves room for future sync metadata.
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN notes TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE expenses ADD COLUMN receiptImageUri TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE projects ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kablan_pro_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

