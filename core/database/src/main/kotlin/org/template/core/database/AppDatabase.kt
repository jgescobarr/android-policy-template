package org.template.core.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

private const val DATABASE_NAME = "template_database"

/**
 * Base de datos Room de referencia.
 *
 * Referencia normativa: Manual de Arquitectura, sección 8.2 (Migrations
 * Documentadas) — "Cada modificación al esquema de la base de datos debe
 * acompañarse de una Migration explícita ... Está prohibido el uso de
 * fallbackToDestructiveMigration() en entornos de producción sin
 * aprobación explícita del arquitecto responsable."
 *
 * TRACEABILITY.md > :core:database > "Room configurado con migrations
 * documentadas; sin fallbackToDestructiveMigration en producción" (Crítico · PR)
 */
@Database(entities = [CachedUserEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedUserDao(): CachedUserDao

    companion object {
        /**
         * Migración v1 → v2: se agrega la columna `email` (antes no existía).
         * Documentada aquí en vez de dejar que Room recree la tabla
         * (lo que perdería los datos existentes del usuario).
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cached_user ADD COLUMN email TEXT NOT NULL DEFAULT ''")
            }
        }

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)
                // Deliberadamente SIN .fallbackToDestructiveMigration() — ver KDoc.
                .build()
    }
}
