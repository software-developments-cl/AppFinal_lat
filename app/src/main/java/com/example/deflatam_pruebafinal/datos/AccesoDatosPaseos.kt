package com.example.deflatam_pruebafinal.datos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


/** DAO = Data Access Object
* Aquí definimos cómo vamos a acceder a los datos de la base de datos*/
@Dao
interface AccesoDatosPaseos {

    /** Obtener todos los paseos ordenados por fecha (más recientes primero)*/
    @Query("SELECT * FROM paseos_mascotas ORDER BY fecha DESC")
    fun getTodosLosPaseos(): Flow<List<EntidadPaseoMascota>>

    /** Obtenemos solo los paseos que no han sido pagados*/
    @Query("SELECT * FROM paseos_mascotas WHERE estaPagado = 0 ORDER BY fecha DESC")
    fun getPaseosPendientes(): Flow<List<EntidadPaseoMascota>>

    /** Obtenemos solo los paseos que ya fueron pagados*/
    @Query("SELECT * FROM paseos_mascotas WHERE estaPagado = 1 ORDER BY fecha DESC")
    fun getPaseosPagados(): Flow<List<EntidadPaseoMascota>>

    /** Se Agrega un nuevo paseo a la base de datos*/
    @Insert
    suspend fun insertarPaseo(paseo: EntidadPaseoMascota)

    /** Se Actualiza un paseo existente (por ejemplo, marcarlo como pagado)*/
    @Update
    suspend fun actualizarPaseo(paseo: EntidadPaseoMascota)

    /** Elimina un paseo de la base de datos*/
    @Delete
    suspend fun eliminarPaseo(paseo: EntidadPaseoMascota)

    /** Calcula total de dinero ganado (solo paseos ya pagados)*/
    @Query("SELECT SUM(montoTotal) FROM paseos_mascotas WHERE estaPagado = 1")
    fun getTotalGanado(): Flow<Double?>

    /** Calcular el dinero pendiente de cobro (paseos no pagados)*/
    @Query("SELECT SUM(montoTotal) FROM paseos_mascotas WHERE estaPagado = 0")
    fun getTotalPendiente(): Flow<Double?>

    /** Buscar clientes por nombre (puede ser parte del nombre)*/
    @Query("SELECT * FROM paseos_mascotas WHERE nombreCliente LIKE '%' || :nombreCliente || '%'")
    fun buscarPaseosPorNombreCliente(nombreCliente: String): Flow<List<EntidadPaseoMascota>>

}