package org.template.core.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Entidad de Room — pertenece exclusivamente a la capa de Datos.
 *
 * Referencia normativa: Manual de Arquitectura, sección 8.1 — "Las
 * entidades de Room (marcadas con @Entity) son objetos de la capa de datos
 * y no deben usarse directamente en la Domain Layer ni en la UI." Un mapper
 * hacia [org.template.domain.model.UserProfile] vive en el :data que
 * consuma este módulo (pendiente, ver TRACEABILITY.md).
 */
@Entity(tableName = "cached_user")
data class CachedUserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String = "",
)

@Dao
interface CachedUserDao {
    // Nota de corrección (confirmado por sincronización real con Room
    // 2.8.4): @Upsert con retorno Unit produce "unexpected jvm signature V".
    // El retorno debe ser el rowId (Long) de la fila insertada/actualizada.
    @Upsert
    suspend fun upsert(user: CachedUserEntity): Long

    @Query("SELECT * FROM cached_user WHERE id = :id")
    fun observeById(id: String): Flow<CachedUserEntity?>
}
