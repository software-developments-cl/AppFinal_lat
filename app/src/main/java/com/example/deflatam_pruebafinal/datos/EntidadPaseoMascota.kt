package com.example.deflatam_pruebafinal.datos

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

    @ColumnInfo(name = "fecha_hora")
    val fechaHora: Long, // Consider using TypeConverters for Date objects

    @ColumnInfo(name = "notas_adicionales")
    val notasAdicionales: String?
) {
    // Puedes añadir una propiedad calculada para costoTotal si lo necesitas aquí,
    // asegurándote de anotarla con @Ignore para que Room no intente mapearla a una columna.
    // Ejemplo:
    // @Ignore
    // val costoTotal: Double
    //     get() = duracionPaseoHoras * tarifaPorHora
}
