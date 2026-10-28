package com.rpay.v2.data.datasource.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.rpay.v2.data.model.entity.TransactionEntity
import java.io.File

@Database(
    entities = [TransactionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        private const val DB_NAME = "app_transactions.db"
        private const val TAG = "AppDatabase"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColIfMissing(db, "transactions", "slotCode", "TEXT")
                addColIfMissing(db, "transactions", "bankCard", "TEXT")
                addColIfMissing(db, "transactions", "quantity", "INTEGER")
                addColIfMissing(db, "transactions", "tid", "TEXT")
                addColIfMissing(db, "transactions", "info", "TEXT")
            }
        }

        private fun addColIfMissing(
            db: SupportSQLiteDatabase,
            table: String,
            column: String,
            type: String
        ) {
            db.query("PRAGMA table_info(`$table`)").use { c ->
                val nameIdx = c.getColumnIndex("name")
                while (c.moveToNext()) {
                    if (nameIdx >= 0 && c.getString(nameIdx).equals(column, ignoreCase = true)) {
                        return
                    }
                }
            }
            db.execSQL("ALTER TABLE `$table` ADD COLUMN `$column` $type")
            Log.w(TAG, "Added missing column `$column` to `$table`")
        }

        fun buildDatabase(context: Context): AppDatabase {
            val correctFile = normalizeLegacyDbIfNeeded(context)
            Log.d(TAG, "Opening DB at: ${correctFile.absolutePath}")

            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DB_NAME
            )
                .openHelperFactory(FrameworkSQLiteOpenHelperFactory())
                .addMigrations(MIGRATION_1_2)
                .build()
        }

        private fun normalizeLegacyDbIfNeeded(context: Context): File {
            val correct = context.getDatabasePath(DB_NAME)
            val legacyNameAsName = context.getDatabasePath(correct.absolutePath)
            try {
                if (!correct.exists() && legacyNameAsName.exists()) {
                    val renamed = legacyNameAsName.renameTo(correct)
                    if (!renamed) {
                        legacyNameAsName.copyTo(target = correct, overwrite = true)
                        legacyNameAsName.delete()
                    }
                    Log.w(TAG, "Moved legacy DB to ${correct.absolutePath}")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "normalizeLegacyDbIfNeeded failed: ${t.message}", t)
            }
            return correct
        }
    }
}
