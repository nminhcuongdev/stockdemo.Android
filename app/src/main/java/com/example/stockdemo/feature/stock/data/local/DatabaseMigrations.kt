package com.example.stockdemo.feature.stock.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Explicit Room migrations for [StockDatabase].
 *
 * This app is offline-first: the `pending_stock_ins` / `pending_stock_outs`
 * tables hold transactions that have not yet been synced to the server. A
 * destructive fallback would silently wipe that queue on every schema change,
 * losing real user data. Instead, whenever [StockDatabase.version] is bumped,
 * add a `Migration(oldVersion, newVersion)` object below and include it in [ALL].
 *
 * Example:
 * ```
 * val MIGRATION_4_5 = object : Migration(4, 5) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE stocks ADD COLUMN note TEXT")
 *     }
 * }
 * ```
 * Exported schema JSON (see `app/schemas`) lets these migrations be unit-tested
 * with `MigrationTestHelper`.
 */
object DatabaseMigrations {
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `epc_mappings` (
                    `epc` TEXT NOT NULL,
                    `qrCode` TEXT NOT NULL,
                    `mappedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`epc`)
                )
                """.trimIndent()
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_4_5)
}
