package com.example.deflatam_pruebafinal.datos

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date // Importamos Date

@Entity(tableName = "paseos_mascotas")
data class EntidadPaseoMascota(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nombre_mascota")
    val nombreMascota: String,

    @ColumnInfo(name = "raza_mascota")
    val razaMascota: String?,

    @ColumnInfo(name = "nombre_dueño")
    val nombreDueño: String,

    @ColumnInfo(name = "contacto_dueño")
    val contactoDueño: String?,

    @ColumnInfo(name = "duracion_paseo_horas")
    val duracionPaseoHoras: Double,

    @ColumnInfo(name = "tarifa_por_hora")
    val tarifaPorHora: Double,

    @ColumnInfo(name = "fecha_hora") // Mantuvimos este campo según lo acordado
    val fechaHora: Long,

    @ColumnInfo(name = "Fecha") // Nuevo campo Fecha
    val Fecha: Date, // Necesitará un TypeConverter

    @ColumnInfo(name = "EstaPagado") // Nuevo campo EstaPagado
    val EstaPagado: Boolean,

    @ColumnInfo(name = "MontoTotal") // Nuevo campo MontoTotal
    val MontoTotal: Double,

    @ColumnInfo(name = "notas_adicionales")
    val notasAdicionales: String?
) {
    // La propiedad calculada para costoTotal (anteriormente sugerida con @Ignore)
    // ahora se almacena directamente como MontoTotal en la base de datos.
    // El cálculo de MontoTotal (duracionPaseoHoras * tarifaPorHora)
    // se deberá realizar antes de guardar o actualizar la entidad.
}
